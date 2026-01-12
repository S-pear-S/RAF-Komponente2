package com.gaming.clientapp.controller;

import com.gaming.clientapp.service.ApiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UiController {

    private final ApiService apiService;

    @GetMapping("/")
    public String index() { return "login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        String token = apiService.login(email, password);
        if (token != null) {
            session.setAttribute("token", token);
            session.setAttribute("myId", apiService.getMyId(token));
            return "redirect:/dashboard";
        }
        return "redirect:/?error";
    }

    @GetMapping("/register")
    public String showRegister() { return "register"; }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String username, @RequestParam String password,
            @RequestParam String email, @RequestParam String name,
            @RequestParam String surname, @RequestParam String dob,
            Model model) {

        try {
            apiService.register(username, password, email, name, surname, dob);
            return "redirect:/?registered=true";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Registration Failed! (Username/Email might exist)");
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required=false) String keyword, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        List<Map> sessions = apiService.getSessions(token, keyword);
        model.addAttribute("sessions", sessions);
        model.addAttribute("myId", session.getAttribute("myId")); // Pass ID to view
        return "dashboard";
    }

    @GetMapping("/create-session")
    public String showCreate() { return "create_session"; }

    @PostMapping("/create-session")
    public String doCreate(@RequestParam String name, @RequestParam Long gameId,
                           @RequestParam int maxPlayers, @RequestParam String startTime,
                           @RequestParam(required=false) boolean isClosed,
                           @RequestParam String description, HttpSession session) {
        String token = (String) session.getAttribute("token");
        apiService.createSession(token, name, gameId, maxPlayers, isClosed, description, startTime);
        return "redirect:/dashboard";
    }

    @PostMapping("/join/{id}")
    public String join(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");
        try {
            apiService.joinSession(token, id);
        } catch (Exception e) { /* handle error */ }
        return "redirect:/dashboard";
    }

    @GetMapping("/manage/{id}")
    public String manage(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        Map sessionData = apiService.getSessions(token, null).stream()
                .filter(s -> Long.valueOf(s.get("id").toString()).equals(id))
                .findFirst().orElseThrow();

        model.addAttribute("session", sessionData);
        return "manage_session";
    }

    @PostMapping("/manage/{id}/invite")
    public String invite(@PathVariable Long id, @RequestParam String email, HttpSession session) {
        apiService.invite((String) session.getAttribute("token"), id, email);
        return "redirect:/manage/" + id;
    }

    @PostMapping("/manage/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session) {
        apiService.cancel((String) session.getAttribute("token"), id);
        return "redirect:/dashboard";
    }

    @PostMapping("/manage/{id}/finish")
    public String finish(@PathVariable Long id, @RequestParam(required=false) List<Long> absentIds, HttpSession session) {
        apiService.finish((String) session.getAttribute("token"), id, absentIds == null ? List.of() : absentIds);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) { session.invalidate(); return "redirect:/"; }
}