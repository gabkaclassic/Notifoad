package com.gab.notifoad.controllers;

import com.gab.notifoad.services.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CommandController {
    
    @Autowired
    @Qualifier("manager")
    private Manager manager;
    
    @GetMapping("/command/{command}")
    public String command(@PathVariable("command") String command) {
    
        manager.sendPhrase(command);
        
        return String.format("Your command < %s > was been successful start", command);
    }
    
}
