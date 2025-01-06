package common;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ListeAuth {
    private Map<String, String> authPairs;
    private String filename;

    public ListeAuth(String filename) {
        this.filename = filename;
        this.authPairs = new HashMap<>();
        loadFromFile();
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    authPairs.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading auth file: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, String> entry : authPairs.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving auth file: " + e.getMessage());
        }
    }

    public boolean checkAuth(String login, String password) {
        return authPairs.getOrDefault(login, "").equals(password);
    }

    public boolean addAuth(String login, String password) {
        if (!authPairs.containsKey(login)) {
            authPairs.put(login, password);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean deleteAuth(String login, String password) {
        if (checkAuth(login, password)) {
            authPairs.remove(login);
            saveToFile();
            return true;
        }
        return false;
    }

    public boolean modifyAuth(String login, String newPassword) {
        if (authPairs.containsKey(login)) {
            authPairs.put(login, newPassword);
            saveToFile();
            return true;
        }
        return false;
    }
}
