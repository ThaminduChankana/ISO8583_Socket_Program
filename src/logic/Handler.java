package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class Handler implements Runnable {
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<String> passedNames = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static HashMap<String, PrintWriter> nameWithWriters = new HashMap<String, PrintWriter>();
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ISO8583VisaParser iso8583VisaParser = new ISO8583VisaParser();

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                out.println("SUBMITNAME");
                name = in.readLine();
                if (name == null) {
                    return;
                }

                synchronized (names) {
                    if (!names.contains(name)) {
                        names.add(name);
                        break;
                    }
                }
            }

            out.println("NAMEACCEPTED");
            logger.info(this.name + " Connected");
            writers.add(out);

            nameWithWriters.put(name, out);

            for (String key : nameWithWriters.keySet()) {
                if (key.equals(name)) {
                    for (String name : names) {
                        nameWithWriters.get(key).println("NEWNAME" + name);
                    }
                } else {
                    nameWithWriters.get(key).println("NAME" + name);
                }
            }

            while (true) {
                String input = in.readLine();
                String value = "";
                String sender = "";
                String iso8583VisaMsg = "";

                if (input.startsWith("LIST")) {
                    value = input.substring(5);
                    passedNames.add(value);
                }

                if (input.startsWith("SENDER")) {
                    sender = input.substring(6);
                }

                passedNames.add(sender);

                if (input.startsWith("MSGiso8085visa")) {
                    if (input == null) {
                        return;
                    }
                    for (String n : passedNames) {
                        for (String key : nameWithWriters.keySet()) {
                            if (n.equals(key)) {
                                iso8583VisaMsg = iso8583VisaParser.iso8583VisaMessage(input.substring(14));
                                nameWithWriters.get(key).println("MESSAGE " + name + " : " + iso8583VisaMsg);
                            }
                        }
                    }
                    passedNames.clear();
                } else if (input.startsWith("MSG")) {
                    for (String n : passedNames) {
                        for (String key : nameWithWriters.keySet()) {
                            if (n.equals(key)) {
                                nameWithWriters.get(key).println("MESSAGE " + name + " : " + input.substring(3));
                            }
                        }
                    }
                    passedNames.clear();
                } else if (input.startsWith("CHECKiso8085visa")) {
                    for (PrintWriter writer : writers) {
                        iso8583VisaMsg = iso8583VisaParser.iso8583VisaMessage(input.substring(16));
                        writer.println("MESSAGE " + name + " : " + iso8583VisaMsg);
                    }
                } else if (input.startsWith("CHECK")) {
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " : " + input.substring(5));
                    }
                }
            }
        } catch (java.net.SocketException e) {
            logger.warning(this.name + " client has left");
        } catch (IOException e) {
            System.out.println(e);
            logger.severe(e.toString());
        } finally {
            if (name != null) {
                names.remove(name);
                nameWithWriters.remove(name);

                for (String key : nameWithWriters.keySet()) {
                    if (!key.equals(name)) {
                        nameWithWriters.get(key).println("REMOVE" + name);
                    }
                }

                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
