package com.secdev.project.controller;

import com.secdev.project.model.User;
import com.secdev.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        logger.info("Administrative action: Viewing all users");
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unlockUser(id);
            logger.info("Administrative action: Unlocked user {}", id);
            redirectAttributes.addFlashAttribute("success", "User unlocked successfully");
        } catch (Exception e) {
            logger.error("Error unlocking user {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error unlocking user");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            logger.info("Administrative action: Deleted user {}", id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting user {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting user");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/logs")
    public String viewLogs(Model model) {
        logger.info("Administrative action: Viewing logs");
        model.addAttribute("logs", "Logs would be displayed here. Check log files.");
        return "admin-logs";
    }
}