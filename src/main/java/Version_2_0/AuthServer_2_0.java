package Version_2_0;
import common.ListeAuth;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthServer_2_0 {
    private static final int UDP_PORT = 28414;
    private static final int TCP_PORT = 28415;
    private final ListeAuth auth;
    private final ExecutorService executor;

    public AuthServer_2_0(String authFile) {
        this.auth = new ListeAuth(authFile);
        this.executor = Executors.newFixedThreadPool(10); // Thread pool for better concurrency
        startUDPServer();
        startTCPServer();
    }

    private void startUDPServer() {
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                System.out.println("UDP Server started on port " + UDP_PORT);
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    executor.submit(new UDPRequestHandler(socket, packet, auth));
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
                    executor.submit(new TCPRequestHandler(clientSocket, auth));
                }
            } catch (IOException e) {
                System.err.println("Error in TCP Server: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServer_2_0 <auth-file>");
            return;
        }
        new AuthServer_2_0(args[0]);
    }
}

class UDPRequestHandler implements Runnable {
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final ListeAuth auth;

    public UDPRequestHandler(DatagramSocket socket, DatagramPacket packet, ListeAuth auth) {
        this.socket = socket;
        this.packet = packet;
        this.auth = auth;
    }

    @Override
    public void run() {
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
}

class TCPRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final ListeAuth auth;

    public TCPRequestHandler(Socket clientSocket, ListeAuth auth) {
        this.clientSocket = clientSocket;
        this.auth = auth;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            String response = processRequest(request);
            out.println(response);
        } catch (IOException e) {
            System.err.println("Error handling TCP client: " + e.getMessage());
        }
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
}
