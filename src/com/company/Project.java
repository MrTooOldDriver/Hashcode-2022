package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Project implements Comparable<Project>{
    String name;
    int numOfDays;
    int score;
    int bestBefore;
    int numOfRoles;
    public ArrayList<Role> roles;

    public Project(String name, int numOfDays, int score, int bestBefore, int numOfRoles){
        this.name = name;
        this.numOfDays = numOfDays;
        this.score = score;
        this.bestBefore = bestBefore;
        this.numOfRoles = numOfRoles;
    }


    @Override
    public int compareTo(Project o) {
        return this.bestBefore - o.bestBefore;
//        return new Integer(this.bestBefore).compareTo(new Integer(o.bestBefore))
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return bestBefore == project.bestBefore;
    }
}