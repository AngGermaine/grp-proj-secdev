package com.secdev.project.controller;

import com.secdev.project.dto.RegisterRequest;
import com.secdev.project.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @ModelAttribute RegisterRequest request, // Automatically binds HTML fields
            @RequestParam("profilePhoto") MultipartFile photo,
            Model model) {
        try {
            // This calls your Tika detection, Hashing, and SQL save
            userService.register(request, photo); 
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            // Shows the error message on the "ugly" UI (e.g., "Invalid file type")
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}