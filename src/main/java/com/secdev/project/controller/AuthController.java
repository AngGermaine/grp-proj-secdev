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
            @ModelAttribute RegisterRequest request, 
            @RequestParam("profilePhoto") MultipartFile photo) throws Exception {
        userService.register(request, photo);
        return "redirect:/login?registered=true";
    }
}