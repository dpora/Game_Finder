package com.example.demo.controller;
import com.example.demo.util.CookieUtil;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, Model model) {
        Long userId = userService.getUserIdIfValid(username, password);

        if (userId != null) {
            // Create a cookie for the user ID
            CookieUtil.createCookie(response, "userId", userId.toString(), 60 * 60 * 24, true); // 1 day expiry
            return "redirect:/search";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "home";
        }
    }


    @PostMapping("/auth/register")
    public String register(@RequestParam String username, @RequestParam String password, HttpServletResponse response, Model model) {
        boolean usernameExists = userService.checkIfUsernameExists(username);

        if (usernameExists) {
            model.addAttribute("error", "Username already exists. Please choose a different username.");
            return "register";
        } else {
            Long userId = userService.createUserAndGetId(username, password);

            if (userId != null) {
                // Store the new user ID in a cookie
                CookieUtil.createCookie(response, "userId", userId.toString(), 60 * 60 * 24, true); // 1-day expiry

                return "redirect:/search"; // Redirect to the dashboard after registration
            } else {
                model.addAttribute("error", "An error occurred while creating the account. Please try again.");
                return "register";
            }
        }
    }

    @GetMapping("/guest")
    public String continueAsGuest(HttpServletResponse response) {
        // Create a cookie with userId set to -1
        CookieUtil.createCookie(response, "userId", "-1", 60 * 60 * 24, true);
        // Redirect to the search page
        return "redirect:/search";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "register";
    }
}