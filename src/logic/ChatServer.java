package logic;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ChatServer {

    private static final int PORT = 9001;
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    public static void main(String[] args) throws Exception {
        FileHandler fileHandler = new FileHandler("server.log");
        fileHandler.setFormatter(new CustomLogFormatter());
        logger.addHandler(fileHandler);

        logger.info("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                Socket socket = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
                logger.info("New client connected");
            }
        } finally {
            listener.close();
        }
    }
}
