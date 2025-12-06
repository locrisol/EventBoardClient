# EventBoard Client (Java TCP Console Application)

![Java](https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white)
![Sockets](https://img.shields.io/badge/Networking-TCP%20Client-blue)
![HTTP](https://img.shields.io/badge/HTTP-URL%20Import-orange)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

A Java console application that connects to the **EventBoard TCP Server** and allows users to manage events interactively.  
Includes a fully implemented **import-from-URL** feature for bulk event loading with detailed validation.

---

## Features

- TCP client for the EventBoard Server (port **5550** by default).
- Clean console interface with a simple command menu.
- Supports the full text-based event protocol:
  - `add; date; time; description`
  - `remove; date; time; description`
  - `list; date; -; -`
  - `STOP`
- Extra feature: **HTTP import** from a remote `.txt` file using:

      import; https://example.com/events.txt

- Performs validation on each line before sending to the server:
  - Wrong number of fields.
  - Empty fields (`date`, `time`, `description`).
  - Time not matching the expected format (`6 pm` or `7.30 pm`).
  - Description containing a semicolon.
  - Lines ending with a trailing `;`.
- Prints **per-line reasons** for skipped events.
- Ensures only **valid, sanitised events** are sent to the server.

---

## Tech Stack

| Area       | Technology                               |
|-----------|-------------------------------------------|
| Language  | Java 17+                                  |
| Networking| TCP client using `Socket`                 |
| I/O       | `BufferedReader`, `PrintWriter`           |
| HTTP      | `URL`, `URLConnection`                    |
| Validation| Custom time checks and field validation   |

---

## Project Structure

    eventboard-client/
    └─ EventClientMain.java   # Console UI, TCP logic and URL import

---

## Running the Client

Compile (from the folder that contains the `eventboard` package):

    javac eventboard/*.java

Run the client:

    java eventboard.EventClientMain

Make sure the **EventBoard Server** is already running on `localhost:5550`.

---

## Command Menu

When the client starts, it displays a small help menu:

- `add; date; time; description`
- `remove; date; time; description`
- `list; date; -; -`
- `import; http://.../events.txt`
- `STOP`

Examples:

    add; 12 December 2025; 6 pm; Winter Choir Event
    remove; 5 January 2026; 1.30 pm; Paper Lantern Colouring
    list; 3 March 2026; -; -
    import; https://example.com/events.txt
    STOP

Every command is sent over TCP to the server, and the client prints the server’s reply.

---

## Importing Events from a URL

The most interesting feature of this client is the **import** command:

    import; https://example.com/events.txt

Internally, the client:

1. Extracts the URL from the command.
2. Opens an HTTP connection using `URL` + `URLConnection`.
3. Reads the file **line by line** via a `BufferedReader`.
4. For each non-empty line:
   - Splits by `;` and expects exactly 3 fields:
     - `date`
     - `time`
     - `description`
   - Trims all fields.
   - Rejects lines where any field is empty.
   - Rejects lines where the description **contains** a semicolon.
   - Rejects lines where the original line **ends with `;`**.
   - Uses `looksLikeTime(time)` to validate format (`6 pm`, `7.30 pm`, etc.).
5. If a line passes all checks:
   - Builds a valid server command:

         add; date; time; description

   - Sends it to the server over TCP.
   - Prints the server response.
   - Increments the `imported` counter.
6. If a line is invalid:
   - Prints a message like:

         Skipped line 112: wrong number of fields
         Skipped line 114: invalid time format

   - Increments the `skipped` counter.

At the end, the client prints a summary such as:

    Imported: 100; Skipped: 12

---

## Time Validation

The helper method `looksLikeTime(String time)` checks:

- The time string is composed of **two parts** separated by whitespace:
  - `numberPart` (e.g. `6`, `7.30`)
  - `ampm` (`am` or `pm`)
- `ampm` must be exactly `am` or `pm`.
- `numberPart` may contain a dot:
  - If it does, it is split into `hour` and `minute` parts.
- Both `hour` and `minute` must be valid integers.
- Hours and minutes must be within valid ranges.

This ensures the client does not send obviously invalid times to the server.

---

## Demonstrated Skills

- Building a TCP client that communicates using a custom protocol.
- Implementing a console-based UI with clear commands.
- Reading from remote HTTP resources in Java.
- Performing robust input validation before pushing data to the backend.
- Handling error scenarios gracefully and reporting skipped lines.
- Interoperating cleanly with a multi-threaded Java TCP server.

---

## Possible Improvements

- Allow server host/port to be set via command-line arguments.
- Add support for local file import (e.g. `import-file; events.txt`).
- Improve console UX:
  - Command history.
  - Coloring for errors vs success.
- Add basic unit tests for `looksLikeTime` and the import logic.
- Wrap the client in a simple shell script or batch file for easier launching.

---

## Author

**Leandro Crisol — Student No. 23156503**  
BSc (Honours) in Computing — National College of Ireland

