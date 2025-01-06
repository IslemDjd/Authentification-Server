package Version_1A;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CheckerClientUDP {
    private static final String SERVER_HOST = "localhost";
    private static final int UDP_PORT = 28414;

    public String checkAuth(String login, String password) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String request = "CHK " + login + " " + password;
            byte[] sendData = request.getBytes();
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, UDP_PORT);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java CheckerClientUDP <login> <password>");
            return;
        }

        CheckerClientUDP client = new CheckerClientUDP();
        String result = client.checkAuth(args[0], args[1]);
        System.out.println("Result: " + result);
    }
}