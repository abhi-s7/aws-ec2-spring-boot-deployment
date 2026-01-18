package com.abhi.aws.ec2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String welcome(HttpServletRequest request, Model model) {
        model.addAttribute("name", "Abhishek");
        model.addAttribute("clientIp", request.getRemoteAddr());
        model.addAttribute("userAgent", request.getHeader("User-Agent"));
        return "welcome";
    }
}
