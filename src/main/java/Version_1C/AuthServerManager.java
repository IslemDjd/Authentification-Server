package Version_1C;
import common.ListeAuth;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class AuthServerManager {
    private static final int TCP_PORT = 28415;
    private ListeAuth auth;
    private ExecutorService executor;
    public AuthServerManager(String authFile) {
        this.auth = new ListeAuth(authFile);
        this.executor = Executors.newFixedThreadPool(10);
        startTCPServer();
    }

    private void startTCPServer() {
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                System.out.println("Manager TCP Server started on port " + TCP_PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Error in Manager TCP Server: " + e.getMessage());
            }
        });
    }

    private void handleClient(Socket clientSocket) {
        executor.submit(() -> {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                String[] parts = request.split(" ");
                if (parts.length >= 3) {
                    String response = handleManagerRequest(parts);
                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Error handling Manager client: " + e.getMessage());
            }
        });
    }

    private String handleManagerRequest(String[] parts) {
        if (parts.length >= 3) {
            switch (parts[0]) {
                case "CHK":
                    return auth.checkAuth(parts[1], parts[2]) ? "GOOD" : "BAD";
                case "ADD":
                    return auth.addAuth(parts[1], parts[2]) ? "DONE" : "ERROR exists";
                case "DEL":
                    return auth.deleteAuth(parts[1], parts[2]) ? "DONE" : "ERROR invalid";
                case "MOD":
                    return auth.modifyAuth(parts[1], parts[2]) ? "DONE" : "ERROR not_found";
                default:
                    return "ERROR invalid_command";
            }
        }
        return "ERROR malformed_request";
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServerManager <auth-file>");
            return;
        }
        new AuthServerManager(args[0]);
    }
}