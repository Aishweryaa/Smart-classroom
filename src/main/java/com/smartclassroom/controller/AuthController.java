package com.smartclassroom.controller;

import com.smartclassroom.entity.User;
import com.smartclassroom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AuthController - handles login page, registration, and logout redirect.
 * All endpoints here are publicly accessible (configured in SecurityConfig).
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /** Display the login page */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("errorMsg",  "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");
        return "auth/login";
    }

    /** Display the registration form */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", new User.Role[]{ User.Role.STUDENT, User.Role.FACULTY });
        return "auth/register";
    }

    /** Handle registration form submission */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        // If validation annotations failed
        if (result.hasErrors()) {
            model.addAttribute("roles", new User.Role[]{ User.Role.STUDENT, User.Role.FACULTY });
            return "auth/register";
        }

        try {
            // Default role = STUDENT if somehow not set
            if (user.getRole() == null) user.setRole(User.Role.STUDENT);
            userService.register(user);
            redirectAttributes.addFlashAttribute("successMsg",
                "Registration successful! Please log in.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("roles", new User.Role[]{ User.Role.STUDENT, User.Role.FACULTY });
            return "auth/register";
        }
    }

    /** Root URL → redirect to login */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}