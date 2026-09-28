package com.phuoc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Buoc 10 (Render in Ajax): tra ve 2 trang Thymeleaf login va profile */
@Controller
@RequestMapping("/")
public class AuthController {

    @GetMapping
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("login")
    public String index() {
        return "login";
    }

    @GetMapping("user/profile")
    public String profile() {
        return "profile";
    }
}
