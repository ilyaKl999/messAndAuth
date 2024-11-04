package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.UUID;

@Controller
public class AuthorizationController {
    public static final HashMap <String,Entity> users = new HashMap<>();

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        long a = System.currentTimeMillis();
        User user = Entity.find(users, username, password);
        if (user != null) {
            session.setAttribute("user", user); // Сохранение пользователя в сессии
            long b = System.currentTimeMillis();
            Logger.successfully("Авторизация прошла успешно", b - a);
            return "redirect:/home";
        } else {
            // Возврат на страницу входа с сообщением об ошибке
            model.addAttribute("message", "Invalid username or password.");
            long b = System.currentTimeMillis();
            Logger.danger("Попытка авторизации без успеха", b - a);
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, @RequestParam String name, @RequestParam int age, RedirectAttributes redirectAttributes) {
        long a = System.currentTimeMillis();
        User user = Entity.find(users,username);
        if (user == null) {
            // Если такого логина нет, регистрируем и редирект к логину и паролю с сообщением о успешной регистрации
            User newUser = new User(User.generateUUID().toString(), name, password, username,age);
            users.put(newUser.ID,newUser);
            long b = System.currentTimeMillis();
            Logger.successfully("Регистрация прошла успешно", b - a);
            redirectAttributes.addFlashAttribute("message", "You have successfully registered.");
            return "redirect:/login";
        } else {
            // Сообщение о том, что такой логин существует, в редиректе на регистрацию
            long b = System.currentTimeMillis();
            Logger.danger("Попытка регистрации с существующим логином", b - a);
            redirectAttributes.addFlashAttribute("message", "Username already exists.");
            return "redirect:/register";
        }
    }

    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        // Получение пользователя из сессии
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            model.addAttribute("username", currentUser.getName());
            return "home";
        } else {
            // Если пользователь не найден в сессии, перенаправляем на страницу входа с сообщением об ошибке
            return "redirect:/login";
        }
    }
}