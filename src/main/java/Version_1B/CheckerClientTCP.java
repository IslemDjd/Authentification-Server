package Version_1B;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CheckerClientTCP {
    private static final String SERVER_HOST = "localhost";
    private static final int TCP_PORT = 28415;

    public String checkAuth(String login, String password) {
        try (
                Socket socket = new Socket(SERVER_HOST, TCP_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("CHK " + login + " " + password);
            return in.readLine();
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java CheckerClientTCP <login> <password>");
            return;
        }

        CheckerClientTCP client = new CheckerClientTCP();
        String result = client.checkAuth(args[0], args[1]);
        System.out.println("Result: " + result);
    }
}