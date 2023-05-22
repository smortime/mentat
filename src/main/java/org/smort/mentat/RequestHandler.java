package org.smort.mentat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/*
 * RequestHandler listens on a Socket and will parse the input and either enque it for
 * the Reporter, close connection with Client due to invalid input, or trigger shutdown
 * of the server due to "terminate" request.
 * 
 * This is basically a producer.
 */
final class RequestHandler implements Runnable {
    private static final String TERMINATE_APP_REQUEST = "terminate";

    private final Socket socket;
    private final int socketKey;
    private final ExecutorService executorService;
    private final BlockingQueue<Integer> workQueue;
    private final ConcurrentHashMap<Integer, Socket> clientSockets;

    RequestHandler(int socketKey, ExecutorService executorService, BlockingQueue<Integer> workQueue,
            ConcurrentHashMap<Integer, Socket> clientSockets) {
        this.socketKey = socketKey;
        this.clientSockets = clientSockets;
        this.socket = clientSockets.get(socketKey);
        this.executorService = executorService;
        this.workQueue = workQueue;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String input = "";
            // null indicates client has closed the connection
            while (input != null) {
                // Read until \n or \r
                input = reader.readLine();

                // Trigger connections termination and clean shutdown
                if (TERMINATE_APP_REQUEST.equals(input)) {
                    executorService.shutdownNow();
                    return;
                }

                int parsedInput;
                try {
                    parsedInput = parseInput(input);
                } catch (NumberFormatException e) {
                    // invalid input, hangup on the client
                    return;
                }
                workQueue.add(parsedInput);
            }
            System.out.println("THEY HUNG UP ON US");
        } catch (IOException e) {
            // SocketException most likely means the server is shutting
            // down and closing sockets thus interrupted readLine
            if (e instanceof SocketException se) {
                System.out.printf("RequestHandler terminating - %s\n", se);
            } else {
                // Helpful to see if debugging since this is unexpected
                e.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientSockets.remove(socketKey);
        }
    }

    // Would usually @VisibleForTesting but didn't want to bring in guava just for
    // documentation purposes...
    static int parseInput(String input) throws NumberFormatException {
        // If input isn't 9 characters we know its invalid
        // terminate is also 9 characters... neat
        if (input.length() != 9) {
            // Can create our own exception... Effective Java states to prefer existing
            // For our use case this is a NumberFormatException plus Integer.parseInt
            // already
            // throws this type of exception aswell
            throw new NumberFormatException("Inputs required to be 9 characters");
        }
        return Integer.parseInt(input);
    }
}
