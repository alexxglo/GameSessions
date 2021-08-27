package com.playtika.gamesessions.controllers;

import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.services.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("/add")
    @PostMapping
    public GameSession add(@RequestBody GameSession gameSession) {
        return null;
    }
}
