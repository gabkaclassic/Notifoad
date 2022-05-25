package com.gab.notifoad.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@Data
@NoArgsConstructor
public class Toad {
    
    private int bugs;
    private int medKits;
    private int gangToads;
    private int satiety;
    private int candies;
    private int tapes;
    private boolean married;
    private boolean gangExists;
    private int target;
    private int level;
    private int happy;
    private boolean alive;
    private boolean party;
    private boolean work;
    
    public boolean feed() {
        
        return addSatiety(1);
    }
    
    public boolean fatten() {
    
        return addSatiety(5);
    }
    
    public void levelUp() {
        
        level++;
    }
    
    public void useCandy() {
        
        candies--;
    }
    
    public void heal() {
        
        if(medKits > 0)
            medKits--;
    }
    
    public void wedding() {
        
        married = true;
    }
    
    public void divorce() {
        
        married = false;
    }
    
    public void recruit() {
        
        if(gangToads < 11)
            gangToads++;
    }
    
    public void decayGang() {
        
        gangToads = 1;
        gangExists = false;
    }
    
    public void getGang() {
        
        gangExists = true;
    }

    public void changeBugs(int amount) {
        
        if(bugs + amount >= 0)
            bugs += amount;
        else
            bugs = 0;
    }
    
    public void changeHappy(int amount) {
        
        if(happy + amount >= 0)
            happy += amount;
        else
            happy = 0;
    }
    
    public boolean addSatiety(int amount) {
        
        int previousSatiety = satiety;
        satiety += amount;
        satiety = target % satiety;
    
        if(previousSatiety < satiety) {
            levelUp();
            return true;
        }
        
        return false;
    }
}
