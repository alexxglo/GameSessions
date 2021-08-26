package com.playtika.gamesessions.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayersController {

    @GetMapping
    public String getInfo(){
        return "Restricted info about players";
    }

    @GetMapping
    @RequestMapping("/public")
    public String getPublicInfo(){
        return "Public info about players";
    }
}
