package common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListeAuth {
    // Custom class to hold login-password pairs
    private static class AuthPair {
        private String login;
        private String password;

        public AuthPair(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private List<AuthPair> authPairs;
    private String filename;

    public ListeAuth(String filename) {
        this.filename = filename;
        this.authPairs = new ArrayList<>();
        loadFromFile();
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    authPairs.add(new AuthPair(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading auth file: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (AuthPair pair : authPairs) {
                writer.write(pair.getLogin() + ":" + pair.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving auth file: " + e.getMessage());
        }
    }

    public boolean checkAuth(String login, String password) {
        for (AuthPair pair : authPairs) {
            if (pair.getLogin().equals(login) && pair.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean addAuth(String login, String password) {
        for (AuthPair pair : authPairs) {
            if (pair.getLogin().equals(login)) {
                return false; // Login already exists
            }
        }
        authPairs.add(new AuthPair(login, password));
        saveToFile();
        return true;
    }

    public boolean deleteAuth(String login, String password) {
        for (AuthPair pair : authPairs) {
            if (pair.getLogin().equals(login) && pair.getPassword().equals(password)) {
                authPairs.remove(pair);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public boolean modifyAuth(String login, String newPassword) {
        for (AuthPair pair : authPairs) {
            if (pair.getLogin().equals(login)) {
                pair.setPassword(newPassword);
                saveToFile();
                return true;
            }
        }
        return false;
    }
}
