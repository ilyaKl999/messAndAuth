package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Controller
public class DialogController {
    public static HashMap<String, Dialog> dialogs = new HashMap<>();

    // Метод для отображения страницы с диалогами
    @GetMapping("/dialogs")
    public String showDialogsPage(Model model, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            // Получение пользователя из сессии
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                // Получение списка диалогов пользователя
                List<Dialog> userDialogs = getDialogsForUser(currentUser);
                model.addAttribute("dialogs", userDialogs);
                Logger.successfully("Dialogs page displayed successfully", System.currentTimeMillis() - startTime);
                return "dialogs";
            } else {
                Logger.danger("User not found in session", System.currentTimeMillis() - startTime);
                return "redirect:/login";
            }
        } catch (Exception e) {
            Logger.danger("Error displaying dialogs page: " + e.getMessage(), System.currentTimeMillis() - startTime);
            return "redirect:/dialogs?error=internalError";
        }
    }

    // Метод для отображения конкретного диалога
    @GetMapping("/dialogs/{id}")
    public String showDialog(@PathVariable String id, Model model, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            // Получение пользователя из сессии
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                // Получение диалога по ID
                Dialog dialog = dialogs.get(id);
                if (dialog != null) {
                    model.addAttribute("dialog", dialog);
                    Logger.successfully("Dialog displayed successfully", System.currentTimeMillis() - startTime);
                    return "dialog";
                } else {
                    Logger.danger("Dialog not found", System.currentTimeMillis() - startTime);
                    return "redirect:/dialogs?error=dialogNotFound";
                }
            } else {
                Logger.danger("User not found in session", System.currentTimeMillis() - startTime);
                return "redirect:/login";
            }
        } catch (Exception e) {
            Logger.danger("Error displaying dialog: " + e.getMessage(), System.currentTimeMillis() - startTime);
            return "redirect:/dialogs?error=internalError";
        }
    }

    // Метод для отправки сообщения в диалог
    @PostMapping("/dialogs/{id}/send")
    public String sendMessage(@PathVariable String id, @RequestParam String text, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            // Получение пользователя из сессии
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                // Получение диалога по ID
                Dialog dialog = dialogs.get(id);
                if (dialog != null) {
                    // Создание нового сообщения
                    Message newMessage = new Message(UUID.randomUUID().toString(), currentUser.getName(), text);
                    dialog.getMessages().add(newMessage);
                    Logger.successfully("Message sent successfully", System.currentTimeMillis() - startTime);
                    return "redirect:/dialogs/" + id;
                } else {
                    Logger.danger("Dialog not found", System.currentTimeMillis() - startTime);
                    return "redirect:/dialogs?error=dialogNotFound";
                }
            } else {
                Logger.danger("User not found in session", System.currentTimeMillis() - startTime);
                return "redirect:/login";
            }
        } catch (Exception e) {
            Logger.danger("Error sending message: " + e.getMessage(), System.currentTimeMillis() - startTime);
            return "redirect:/dialogs?error=internalError";
        }
    }

    // Метод для создания нового диалога
    @PostMapping("/dialogs/create")
    public String createDialog(@RequestParam String login, HttpSession session) {
        long startTime = System.currentTimeMillis();
        try {
            // Получение пользователя из сессии
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                // Поиск пользователя по логину
                User otherUser = User.find(AuthorizationController.users, login);
                if (otherUser != null) {
                    List<User> users = new ArrayList<>();
                    users.add(currentUser);
                    users.add(otherUser);

                    String dialogId = UUID.randomUUID().toString();
                    Dialog newDialog = new Dialog(dialogId, users);
                    currentUser.getDialogs().add(UUID.fromString(dialogId));
                    otherUser.getDialogs().add(UUID.fromString(dialogId));
                    dialogs.put(dialogId, newDialog);

                    Logger.successfully("Dialog created successfully", System.currentTimeMillis() - startTime);
                    return "redirect:/dialogs";
                } else {
                    Logger.danger("User not found", System.currentTimeMillis() - startTime);
                    return "redirect:/dialogs?error=userNotFound";
                }
            } else {
                Logger.danger("User not found in session", System.currentTimeMillis() - startTime);
                return "redirect:/login";
            }
        } catch (Exception e) {
            Logger.danger("Error creating dialog: " + e.getMessage(), System.currentTimeMillis() - startTime);
            return "redirect:/dialogs?error=internalError";
        }
    }

    // Метод для получения списка диалогов для пользователя
    private List<Dialog> getDialogsForUser(User user) {
        long startTime = System.currentTimeMillis();
        try {
            List<Dialog> userDialogs = new ArrayList<>();
            for (UUID dialogId : user.getDialogs()) {
                Dialog dialog = dialogs.get(dialogId.toString());
                if (dialog != null) {
                    userDialogs.add(dialog);
                }
            }
            Logger.successfully("Dialogs for user retrieved successfully", System.currentTimeMillis() - startTime);
            return userDialogs;
        } catch (Exception e) {
            Logger.danger("Error retrieving dialogs for user: " + e.getMessage(), System.currentTimeMillis() - startTime);
            return new ArrayList<>();
        }
    }
}