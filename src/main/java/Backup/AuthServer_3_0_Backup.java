package Backup;
import common.ListeAuth;

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class AuthServer_3_0_Backup {
    private static final int UDP_PORT = 28414;
    private static final int TCP_PORT = 28415;
    private static final int LOG_PORT = 3244;
    private ListeAuth auth;
    private ExecutorService executor;

    public AuthServer_3_0_Backup(String authFile) {
        this.auth = new ListeAuth(authFile);
        this.executor = Executors.newFixedThreadPool(10);
        startUDPServer();
        startTCPServer();
    }

    private void startUDPServer() {
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                System.out.println("UDP Server started on port " + UDP_PORT);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    handleUDPRequest(socket, packet);
                }
            } catch (IOException e) {
                System.err.println("Error in UDP Server: " + e.getMessage());
            }
        });
    }

    private void startTCPServer() {
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                System.out.println("TCP Server started on port " + TCP_PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleTCPClient(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Error in TCP Server: " + e.getMessage());
            }
        });
    }

    private void handleUDPRequest(DatagramSocket socket, DatagramPacket packet) {
        String request = new String(packet.getData(), 0, packet.getLength());
        String response = processRequest(request);
        try {
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
            logOperation("UDP", packet.getAddress().getHostAddress(), packet.getPort(), request, response);
        } catch (IOException e) {
            System.err.println("Error sending UDP response: " + e.getMessage());
        }
    }

    private void handleTCPClient(Socket clientSocket) {
        executor.submit(() -> {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                String response = processRequest(request);
                out.println(response);
                logOperation("TCP", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), request, response);
            } catch (IOException e) {
                System.err.println("Error handling TCP client: " + e.getMessage());
            }
        });
    }

    private String processRequest(String request) {
        String[] parts = request.split(" ");
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

    private void logOperation(String protocol, String address, int port, String request, String response) {
        try (Socket logSocket = new Socket("localhost", LOG_PORT);
             PrintWriter out = new PrintWriter(logSocket.getOutputStream(), true)) {
            String logMessage = String.format(
                    "{\"timestamp\":\"%d\",\"clientInfo\":{\"protocol\":\"%s\",\"address\":\"%s\",\"port\":%d},\"request\":\"%s\",\"response\":\"%s\"}",
                    System.currentTimeMillis(), protocol, address, port, request, response
            );
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Error logging to Log server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServer_3_0 <auth-file>");
            return;
        }
        new AuthServer_3_0_Backup(args[0]);
    }
}