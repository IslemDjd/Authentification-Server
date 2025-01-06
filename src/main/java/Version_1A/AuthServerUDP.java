package Version_1A;

import common.ListeAuth;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AuthServerUDP {
    private static final int UDP_PORT = 28414;
    private ListeAuth auth;

    public AuthServerUDP(String authFile) {
        this.auth = new ListeAuth(authFile);
        startUDPServer();
    }

    private void startUDPServer() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Server started on port " + UDP_PORT);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handleRequest(socket, packet);
            }
        } catch (IOException e) {
            System.err.println("Error in UDP Server: " + e.getMessage());
        }
    }

    private void handleRequest(DatagramSocket socket, DatagramPacket packet) {
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

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java AuthServerUDP <auth-file>");
            return;
        }
        new AuthServerUDP(args[0]);
    }
}