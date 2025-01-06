package Version_3_0;
import java.net.*;
import java.io.*;
import org.json.JSONObject; // Include JSON library

public class LogServer {
    private static final int PORT = 3244;
    private final String logFile;
    public LogServer(String logFile) {
        this.logFile = logFile;
        ensureJsonArray(); // Initialize the log file as a JSON array if empty
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Log Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Log Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                RandomAccessFile writer = new RandomAccessFile(logFile, "rw")
        ) {
            String logEntry = in.readLine();
            if (logEntry != null) {
                JSONObject jsonLog = new JSONObject(logEntry); // Parse the log entry as JSON
                appendToJsonArray(writer, jsonLog);
            }
        } catch (IOException e) {
            System.err.println("Error handling log client: " + e.getMessage());
        } catch (org.json.JSONException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
        }
    }

    private void ensureJsonArray() {
        try (RandomAccessFile writer = new RandomAccessFile(logFile, "rw")) {
            if (writer.length() == 0) {
                writer.writeBytes("[]"); // Initialize an empty JSON array
            }
        } catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
    }

    private void appendToJsonArray(RandomAccessFile writer, JSONObject jsonLog) throws IOException {
        long fileLength = writer.length();
        if (fileLength > 2) { // Check if the file already contains elements (beyond the empty array brackets)
            writer.seek(fileLength - 1); // Move to the position before the closing bracket
            writer.writeBytes(",\n");
        } else {
            writer.seek(fileLength - 1); // Move to overwrite the closing bracket
        }
        writer.writeBytes(jsonLog.toString(4)); // Add the new JSON object with indentation
        writer.writeBytes("\n]"); // Close the JSON array
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LogServer <log-file>");
            return;
        }
        new LogServer(args[0]).start();
    }
}