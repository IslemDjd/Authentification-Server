package Backup;

import common.ListeAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthServer_1_0_Backup {
    private static final int UDP_PORT = 28414;
    private static final int TCP_PORT = 28420;
    private ListeAuth auth;
    private ExecutorService executor;

    public AuthServer_1_0_Backup(String authFile) {
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
        String[] parts = request.split(" ");
        if (parts.length >= 3 && "CHK".equals(parts[0])) {
            boolean valid = auth.checkAuth(parts[1], parts[2]);
            String response = valid ? "GOOD" : "BAD";
            try {
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            } catch (IOException e) {
                System.err.println("Error sending UDP response: " + e.getMessage());
            }
        }
    }

    private void handleTCPClient(Socket clientSocket) {
        executor.submit(() -> {
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
                System.err.println("Error handling TCP client: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServer_1_0 <auth-file>");
            return;
        }
        new AuthServer_1_0_Backup(args[0]);
    }
}