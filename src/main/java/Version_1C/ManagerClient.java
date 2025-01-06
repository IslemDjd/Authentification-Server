package Version_1C;

import java.io.*;
import java.net.*;

public class ManagerClient {
    private static final String SERVER_HOST = "localhost";
    private static final int TCP_PORT = 28415;

    public String sendRequest(String request) {
        try (
                Socket socket = new Socket(SERVER_HOST, TCP_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(request);
            return in.readLine();
        } catch (IOException e) {
            return "ERROR " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ManagerClient <command> <params...>");
            System.out.println("Commands: CHK <login> <password>");
            System.out.println("          ADD <login> <password>");
            System.out.println("          DEL <login> <password>");
            System.out.println("          MOD <login> <newPassword>");
            return;
        }

        ManagerClient client = new ManagerClient();
        String request = String.join(" ", args);
        String result = client.sendRequest(request);
        System.out.println("Result: " + result);
    }
}
