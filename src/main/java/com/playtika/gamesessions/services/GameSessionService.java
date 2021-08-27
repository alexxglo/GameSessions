package com.playtika.gamesessions.services;

import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameSessionService {

    @Autowired
    GameSessionRepository gameSessionRepository;

    public List<GameSession> getAll() {
        return gameSessionRepository.findAll();
    }
}
