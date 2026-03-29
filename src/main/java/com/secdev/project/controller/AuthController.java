package com.secdev.project.controller;

import com.secdev.project.dto.RegisterRequest;
import com.secdev.project.service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String handleRegistration(
            @ModelAttribute RegisterRequest request, 
            @RequestParam("profilePhoto") MultipartFile photo) throws Exception {
        userService.register(request, photo);
        return "redirect:/login?registered=true";
    }

   @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); 
        
        boolean isAdmin = auth.getAuthorities().stream()
                             .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        model.addAttribute("username", username);
        model.addAttribute("isAdmin", isAdmin);

        // 2 add code here to fetch the "Assets" from the database
        // model.addAttribute("assets", assetService.findAllForUser(username)); for example

        return "dashboard";
    }
}