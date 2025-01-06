package Version_1B;

import common.ListeAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AuthServerTCP {
    private static final int TCP_PORT = 28415;
    private ListeAuth auth;

    public AuthServerTCP(String authFile) {
        this.auth = new ListeAuth(authFile);
        startTCPServer();
    }

    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP Server started on port " + TCP_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error in TCP Server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            String[] parts = request.split(" ");
            if (parts.length >= 3 && "CHK".equals(parts[0])) {
                boolean valid = auth.checkAuth(parts[1], parts[2]);
                String response = valid ? "GOOD" : "BAD";
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServerTCP <auth-file>");
            return;
        }
        new AuthServerTCP(args[0]);
    }
}