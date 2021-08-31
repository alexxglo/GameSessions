package com.playtika.gamesessions.services;

import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class EntityManagerService {

    @Autowired
    GameSessionRepository gameSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterService filterService;

    @PersistenceContext
    private EntityManager em;

    public List<GameSession> executeSqlCommand(String sqlQuery) {

        return em.createNativeQuery(sqlQuery).getResultList();
    }
}
