package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {

    public static void main(String[] args) throws Exception {
        // File here
        Path currentPath = Paths.get("problems", args[0]).toAbsolutePath();
        System.out.println(currentPath.toString());
        File myObj = new File(currentPath.toString());
        Scanner in = null;
        try {
            in = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int player_num = in.nextInt(), project_num = in.nextInt();
        System.out.println(player_num + " " + project_num);

        // com.company.Player
        //HashMap<String, HashMap> player_static = new HashMap<>();
        ArrayList<Player> player_static = new ArrayList<>();
        for (int i = 0; i < player_num; i++) {
            String player_name = in.next();

            int skill_num = in.nextInt();
            HashMap<String, Integer> skills = new HashMap<>();
            for (int j = 0; j < skill_num; j++) {
                String skill_name = in.next();
                int skill_value = in.nextInt();
                skills.put(skill_name, skill_value);
            }
            Player newPlayer = new Player(player_name, skills);
            player_static.add(newPlayer);
        }

        // Project
        ArrayList<Project> project_static = new ArrayList<>();
        //HashMap<String, String[]> project_static = new HashMap<>();
        for (int i = 0; i < project_num; i++) {
            String[] project_info = new String[4];
            String project_name = in.next(); // project name
            int numOfDays = in.nextInt(); // number of days it takes to complete the project
            int score = in.nextInt(); // score awarded for project’s completion
            int bestBeforeDay = in.nextInt(); // best before
            int numOfRoles = in.nextInt(); // number of roles
            Project project = new Project(project_name, numOfDays, score, bestBeforeDay, numOfRoles);
            project.roles = new ArrayList<>();
            for (int j = 0; j < project.numOfRoles; j++) {
                String role_name = in.next();
                int skill_required = in.nextInt();
                Role role = new Role(role_name, skill_required);
                project.roles.add(role);
            }
            System.out.println(project_name + " " + numOfDays + " " + score + " " + bestBeforeDay + " " + numOfRoles);
            project_static.add(project);
        }

        //Sort project by best before date
        Collections.sort(project_static);

        for (Project project : project_static) {
            if (project.name == "CastOSLitev5") {
                System.out.println(project.roles);
            }
        }

        //Main loop
        int day = 0;
        int last_active_day = 0;
        LinkedHashMap<String, ArrayList<Player>> projectAssignment = new LinkedHashMap<>();
        while (true) {
            setPlayerNoToWorking(player_static, day);
            while (true) {
                Project next_project = findNextDoableProject(player_static, project_static, day);
                if (next_project == null) {
                    //no any project can be done today
                    break;
                }
                ArrayList<Player> assignment = chooseBestPlayer(player_static, next_project.roles);
                setPlayerToWorking(player_static, assignment, next_project.numOfDays + day);
                projectAssignment.put(next_project.name, assignment);
                project_static.remove(next_project);
                last_active_day = day;
            }
            if (project_static.size() == 0) {
                break;
            }
            if (day - last_active_day > 1000) {
                break;
            }
            day++;
        }
        //todo call write file
        printResultToFile(projectAssignment.size(), projectAssignment, args[0]);

        return;
    }

    public static Boolean checkProjectDoAble(HashMap<String, Integer> roles, ArrayList<Player> player_static) {
        ArrayList<Boolean> bool_check = new ArrayList<>();
        for (String role : roles.keySet()) {
            for (Player player : player_static) {
                if (!player.skills.containsKey(role)) {
                    continue;
                }
                int player_skill = (int) player.skills.get(role);
                int project_skill = roles.get(role);
                if (player_skill >= project_skill && !player.isWorking) {
                    bool_check.add(true);
                    break;
                }
            }
        }
        return bool_check.size() == roles.size();
    }

    public static ArrayList<Player> chooseBestPlayer(ArrayList<Player> player_static, HashMap<String, Integer> roles) throws Exception {
        HashMap<String, Integer> deep_copy_roles = new HashMap<>();
        for (String role : roles.keySet()) {
            int skills_nums = (int) Integer.valueOf(roles.get(role).intValue());
            deep_copy_roles.put(role, skills_nums);
        }

        ArrayList<Player> best_players = new ArrayList<>();

        ArrayList<String> currentKeys = new ArrayList<>();
        // Level 1 filter, skill level -1
        for (String key : deep_copy_roles.keySet()) {
            boolean hasMentor = false;
            boolean hasPlayer = false;
            Player chosenPlayer = null;
            for (Player player : player_static) {
                if (!player.skills.containsKey(key)) {
                    player.skills.put(key, 0);
                }
                if (player.skills.get(key) >= deep_copy_roles.get(key)) {
                    hasMentor = true;
                }
                if ((player.skills.get(key) == deep_copy_roles.get(key) - 1) && (!player.isWorking) && (!hasPlayer) && !(best_players.contains(player))) {
                    chosenPlayer = player;
                    hasPlayer = true;
                }
                if (hasMentor && hasPlayer) {
                    chosenPlayer.workingSkills = key;
                    best_players.add(chosenPlayer);
                    currentKeys.add(key);
                    break;
                }
            }
        }

        for (String key : currentKeys) {
            deep_copy_roles.remove(key);
        }
        currentKeys.clear();


        // Level 2 filter, skill level ==
        for (String key : deep_copy_roles.keySet()) {
            for (Player player : player_static) {
                if (!player.skills.containsKey(key)) {
                    continue;
                }
                if ((player.skills.get(key) == (int) deep_copy_roles.get(key)) && (!player.isWorking) && !(best_players.contains(player))) {
                    best_players.add(player);
                    player.workingSkills = key;
                    currentKeys.add(key);
                    break;
                }
            }
        }

        for (String key : currentKeys) {
            deep_copy_roles.remove(key);
        }
        currentKeys.clear();

        // Level 3 filter,skill level >
        for (String key : deep_copy_roles.keySet()) {
            Player currentPlayer = null;
            int currentLevel = 0;
            int totalCount = 0;
            int currentPlayerCount = 0;
            for (Player player : player_static) {
                if (!player.skills.containsKey(key)) {
                    continue;
                }
                if (player.skills.get(key) > (int) deep_copy_roles.get(key) && (!player.isWorking) && !(best_players.contains(player))) {
                    if (currentLevel == 0) {
                        currentPlayer = player;
                        currentLevel = player.skills.get(key);
                        currentPlayerCount = totalCount;
                    } else {
                        if (player.skills.get(key) < currentLevel) {
                            currentPlayer = player;
                            currentLevel = player.skills.get(key);
                            currentPlayerCount = totalCount;
                        }
                    }
                }
                totalCount++;
            }
            best_players.add(currentPlayer);
            player_static.get(currentPlayerCount).workingSkills = key;
            currentKeys.add(key);
        }

        for (String key : currentKeys) {
            deep_copy_roles.remove(key);
        }
        currentKeys.clear();

        if (deep_copy_roles.isEmpty()) {
            return best_players;
        } else {
            throw new Exception("Failed to assign works");
        }
    }

    public static Project findNextDoableProject(ArrayList<Player> player_static, ArrayList<Project> project_static, int current_day) {
        for (Project project : project_static) {
            HashMap<String, Integer> roles = project.roles;
            if (checkProjectDoAble(roles, player_static)) {
                return project;
            }
        }
        return null;
    }

    public static void setPlayerToWorking(ArrayList<Player> player_static, ArrayList<Player> assigned_player, int working_until_day) {
        for (Player player : player_static) {
            if (assigned_player.contains(player)) {
                player.isWorking = true;
                player.workingUntilDay = working_until_day;
            }
        }
    }

    public static void setPlayerNoToWorking(ArrayList<Player> player_static, int current_day) {
        for (Player player : player_static) {
            if (player.isWorking && player.workingUntilDay == current_day) {
                player.isWorking = false;
                int skillNumber = player.skills.get(player.workingSkills) + 1;
                player.skills.put(player.workingSkills, skillNumber);
                player.workingSkills = "";
            }
        }
    }

    public static void printResultToFile(int totalCount, LinkedHashMap<String, ArrayList<Player>> resultList, String inputFileName) {
        try {
            FileWriter myWriter = new FileWriter(inputFileName + "_result.txt");
            myWriter.write(totalCount + "\n");
            List<String> l = new ArrayList<>(resultList.keySet());
            for (String key : l) {
                myWriter.write(key + "\n");
                boolean isFirst = true;
                List<Player> l2 = new ArrayList<>(resultList.get(key));
                for (Player player : l2) {
                    if (player == null) {
                        continue;
                    }
                    if (!isFirst) {
                        myWriter.write(" ");
                    }
                    myWriter.write(player.name);
                    isFirst = false;
                }
                myWriter.write("\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}