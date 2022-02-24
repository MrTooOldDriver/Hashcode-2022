package com.company;

import java.util.HashMap;

public class Player {
    String name;
    HashMap<String, Integer> skills;
    boolean isWorking;
    int workingUntilDay;
    String workingSkills;
    public Player(String playerName, HashMap<String, Integer> skills){
        this.name = playerName;
        this.skills = skills;
        this.isWorking = false;
        this.workingUntilDay = 0;
        this.workingSkills = null;
    }
}
