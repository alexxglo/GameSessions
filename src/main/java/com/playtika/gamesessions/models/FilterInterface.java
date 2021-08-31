package com.playtika.gamesessions.models;

import java.util.List;

public interface FilterInterface {
    public List<GameSession> getListFromFilterQuery(String query);
}
