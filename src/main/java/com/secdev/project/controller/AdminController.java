package com.secdev.project.controller;

import com.secdev.project.model.Asset;
import com.secdev.project.model.LoginAttempt;
import com.secdev.project.model.Role;
import com.secdev.project.model.User;
import com.secdev.project.service.AssetService;
import com.secdev.project.service.UserService;
import com.secdev.project.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private UserService userService;

    @Autowired
    private AssetService assetService;

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {
        auditLogger.info("ADMIN ACTION admin={} action=VIEW_ALL_USERS", LoggingUtil.sanitizeForLog(auth.getName()));
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            userService.unlockUser(id);
            auditLogger.info("ADMIN ACTION admin={} action=UNLOCK_USER targetUserId={}", LoggingUtil.sanitizeForLog(auth.getName()), id);
            redirectAttributes.addFlashAttribute("success", "User unlocked successfully");
        } catch (Exception e) {
            logger.error("Error unlocking user {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error unlocking user");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User targetUser = userService.getUserById(id);
            // Prevent admin from deleting themselves
            if (targetUser.getEmail().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete your own account");
                return "redirect:/admin/users";
            }
            userService.deleteUser(id);
            auditLogger.info("ADMIN ACTION admin={} action=DELETE_USER targetUserId={}", LoggingUtil.sanitizeForLog(auth.getName()), id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting user {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting user");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/logs")
    public String viewLogs(Model model) {
        auditLogger.info("ADMIN ACTION action=VIEW_LOGS");
        try {
            List<String> logLines = Files.readAllLines(Paths.get("logs/audit.log"));
            // Get last 200 lines
            int start = Math.max(0, logLines.size() - 200);
            List<String> recentLogs = logLines.subList(start, logLines.size());
            model.addAttribute("logs", recentLogs);
        } catch (IOException e) {
            logger.error("Error reading log file", e);
            model.addAttribute("logs", List.of("Error reading log file: " + e.getMessage()));
        }
        return "admin-logs";
    }

    // Toggle user enabled/disabled
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User targetUser = userService.getUserById(id);
            
            if (targetUser.getEmail().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "Cannot disable your own account");
                return "redirect:/admin/users";
            }
            userService.toggleUserEnabled(id);
            auditLogger.info("ADMIN ACTION admin={} action=TOGGLE_STATUS targetUserId={}", LoggingUtil.sanitizeForLog(auth.getName()), id);
            redirectAttributes.addFlashAttribute("success", "User status updated successfully");
        } catch (Exception e) {
            logger.error("Error toggling user status {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error updating user status");
        }
        return "redirect:/admin/users";
    }

    // Change user role
    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role, 
                                  Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            User targetUser = userService.getUserById(id);
            if (targetUser.getEmail().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "Cannot change your own role");
                return "redirect:/admin/users";
            }
            Role newRole = Role.valueOf(role.toUpperCase());
            userService.changeUserRole(id, newRole);
            auditLogger.info("ADMIN ACTION admin={} action=CHANGE_ROLE targetUserId={} newRole={}", 
                    LoggingUtil.sanitizeForLog(auth.getName()), id, newRole);
            redirectAttributes.addFlashAttribute("success", "User role updated to " + newRole);
        } catch (Exception e) {
            logger.error("Error changing user role {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error updating user role");
        }
        return "redirect:/admin/users";
    }

    // View login attempts
    @GetMapping("/login-attempts")
    public String viewLoginAttempts(Model model) {
        auditLogger.info("ADMIN ACTION action=VIEW_LOGIN_ATTEMPTS");
        List<LoginAttempt> attempts = userService.getRecentLoginAttempts();
        model.addAttribute("attempts", attempts);
        return "admin-login-attempts";
    }

    // View login attempts for specific user
    @GetMapping("/users/{id}/login-attempts")
    public String viewUserLoginAttempts(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        auditLogger.info("ADMIN ACTION action=VIEW_USER_LOGIN_ATTEMPTS targetUser={}", LoggingUtil.sanitizeForLog(user.getEmail()));
        List<LoginAttempt> attempts = userService.getLoginAttemptsByEmail(user.getEmail());
        model.addAttribute("attempts", attempts);
        model.addAttribute("targetUser", user);
        return "admin-login-attempts";
    }

    // View all assets (Global Asset Delete)
    @GetMapping("/assets")
    public String viewAllAssets(Model model, Authentication auth) {
        auditLogger.info("ADMIN ACTION admin={} action=VIEW_ALL_ASSETS", LoggingUtil.sanitizeForLog(auth.getName()));
        List<Asset> assets = assetService.getAllAssets();
        model.addAttribute("assets", assets);
        return "admin-assets";
    }

    // Delete any asset (Administrative override)
    @PostMapping("/assets/{id}/delete")
    public String deleteAnyAsset(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            assetService.adminDeleteAsset(id, auth.getName());
            redirectAttributes.addFlashAttribute("success", "Asset deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting asset {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting asset");
        }
        return "redirect:/admin/assets";
    }
}