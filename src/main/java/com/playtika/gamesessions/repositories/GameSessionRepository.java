package com.playtika.gamesessions.repositories;

import com.playtika.gamesessions.models.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    @Query(value = "SELECT * FROM game_sessions g WHERE g.end_date IS NULL AND g.user_id IS :idUser", nativeQuery = true)
    List<GameSession> getOngoingGameSessions(long idUser);

}
