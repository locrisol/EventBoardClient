/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: EventClientMain
 * 
 * Entry point for the client-side application. Connects to the TCP server,
 * reads user commands from the console, sends them to the server, and
 * displays responses. Also implements the 'import' command using HTTP to
 * retrieve and validate an events file.
 */
package eventboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.Socket;

public class EventClientMain {

    public static final String HOST = "localhost";
    public static final int PORT = 5550;

    public static void main(String[] args) {
        System.out.println("Connecting to server " + HOST + ":" + PORT + "...");

        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Options menu
            System.out.println("Connected to Event Server.");
            System.out.println("Commands:");
            System.out.println("  add; date; time; description");
            System.out.println("  remove; date; time; description");
            System.out.println("  list; date; -; -");
            System.out.println("  import; http://.../events.txt");
            System.out.println("  STOP");
            System.out.println();

            while (true) {
                System.out.print("> "); // simulates console input line when connected to server
                String line = console.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // keeps asking for an option if line is empty
                }

                if (line.equalsIgnoreCase("STOP")) {
                    out.println("STOP");
                    String reply = in.readLine();
                    if (reply != null) {
                        System.out.println("Server: " + reply);
                    }
                    break;
                }

                // Handle import command locally: import; URL
                if (line.toLowerCase().startsWith("import;")) {
                    String[] parts = line.split(";", 2);
                    // Check if the import command is not correct, otherwise calls import method
                    if (parts.length < 2 || parts[1].trim().isEmpty()) {
                        System.out.println("Usage: import; [URL-to-events.txt]");
                    } else {
                        String url = parts[1].trim();
                        importEvents(url, out, in);
                    }
                    continue;
                }

                // Normal commands go directly to the TCP server
                out.println(line);
                String reply = in.readLine();
                if (reply == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }
                System.out.println("Server: " + reply);
            }

            in.close();
            out.close();
            console.close();
            socket.close();

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Client terminated.");
    }

    // HTTP GET to retrieve the file, then send add; ... to server for each valid line
    private static void importEvents(String urlString, PrintWriter out, BufferedReader in) {
        int imported = 0;
        int skipped = 0;

        System.out.println("Importing from URL: " + urlString);

        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    // ignore empty lines
                    continue;
                }

                // Expected format: "date; time; description"
                String[] parts = line.split(";"); // Divides line into 3 sections after each ;
                if (parts.length != 3) {
                    System.out.println("Skipped line " + lineNumber + ": wrong number of fields");
                    skipped++;
                    continue;
                }

                String date = parts[0].trim();
                String time = parts[1].trim();
                String description = parts[2].trim();

                // If a field is missing
                if (date.isEmpty() || time.isEmpty() || description.isEmpty()) {
                    System.out.println("Skipped line " + lineNumber + ": empty field");
                    skipped++;
                    continue;
                }

                // description cannot contain semicolon
                if (description.contains(";")) {
                    System.out.println("Skipped line " + lineNumber + ": extra ';' not allowed in description");
                    skipped++;
                    continue;
                }
                
                // description cannot end with semicolon
                if (line.endsWith(";")) {
                    System.out.println("Skipped line " + lineNumber + ": must not end with ';'");
                    skipped++;
                    continue;
                }

                // If time format is not as expected
                if (!looksLikeTime(time)) {
                    System.out.println("Skipped line " + lineNumber + ": invalid time format");
                    skipped++;
                    continue;
                }

                // Creates a string command to send to the server
                String command = "add; " + date + "; " + time + "; " + description;
                System.out.println("Sending to server: " + command);
                out.println(command);

                String reply = in.readLine();
                if (reply == null) {
                    System.out.println("Server closed connection during import.");
                    break;
                }
                System.out.println("Server: " + reply);

                imported++;
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("I/O error during import: " + e.getMessage());
            return;
        }

        System.out.println("Imported: " + imported + "; Skipped: " + skipped);
    }

    // Checks if time is as expected with format "6 pm" or "7.30 pm"
    private static boolean looksLikeTime(String time) {
        String t = time.trim().toLowerCase();
        String[] parts = t.split("\\s+"); // Splits in two by space
        if (parts.length != 2) {
            return false;
        }

        String numberPart = parts[0]; // "6" or "7.30"
        String ampm = parts[1];       // "am" or "pm"

        if (!ampm.equals("am") && !ampm.equals("pm")) {
            return false;
        }

        try {
            // Checks if its a single digit time or a combined time (6 or 6.30)
            if (numberPart.contains(".")) {
                String[] hm = numberPart.split("\\.");
                if (hm.length != 2) {
                    return false;
                }
                // After two parts divided by "." are detected, check if they are numbers
                // If not, it will throw a NumberFormatException error
                Integer.parseInt(hm[0]);
                Integer.parseInt(hm[1]);
            } else {
                Integer.parseInt(numberPart);
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
