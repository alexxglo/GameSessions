package com.playtika.gamesessions.controllers;

import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.services.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session")
public class GameSessionController {

    @Autowired
    GameSessionService gameSessionService;

    @GetMapping
    public List<GameSession> getAll() {
        return gameSessionService.getAll();
    }

    @PostMapping
    public GameSession startSession(@RequestBody SessionDTO sessionDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.isAuthenticated()) {
            return gameSessionService.startGame(sessionDTO, auth.getName());
        }
        return null;
    }

    @GetMapping("/end")
    public GameSession endSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.isAuthenticated()) {
            return gameSessionService.endGame(auth.getName());
        }
        return null;
    }

    @GetMapping("/info")
    public String getCurrentSessionDuration() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.isAuthenticated()) {
            return gameSessionService.currentDuration(auth.getName());
        }
        return "No current session available";
    }
}
