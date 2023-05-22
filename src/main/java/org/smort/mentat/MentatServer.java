package org.smort.mentat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public final class MentatServer {
    // 5 threads for hadnling client connections + 1 for Server + 1 for Reporter
    private final ExecutorService executorService = Executors.newFixedThreadPool(7);

    void start(int port) {
        // Kick off Server to listen on socket/process requests
        Server server = new Server(port, executorService);
        executorService.submit(server);
    }

    // bring it down
    void stop() {
        executorService.shutdownNow();
    }

    private final class Server implements Runnable {
        private final ExecutorService executorService;
        private final int port;
        private final Random r = new Random();
        private final ConcurrentHashMap<Integer, Socket> clientSockets = new ConcurrentHashMap<>();

        public Server(int port, ExecutorService executorService) {
            this.port = port;
            this.executorService = executorService;
        }

        @Override
        public void run() {
            // Thread-safe / FIFO
            BlockingQueue<Integer> workQueue = new LinkedBlockingQueue<>();
            executorService.submit(new Reporter(workQueue));

            // Only allows 5 clients at a time
            try (ServerSocket socket = new ServerSocket(port, 5)) {
                socket.setSoTimeout(5_000);
                Socket incoming;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        incoming = socket.accept();
                    } catch (SocketTimeoutException e) {
                        // accpet is a blocking call, use timeout so we can check if we are shutting
                        // down the app. If we aren't we can continue waiting for requests.
                        continue;
                    }
                    int socketKey = r.nextInt(1001);
                    clientSockets.put(socketKey, incoming);
                    RequestHandler handler = new RequestHandler(socketKey, executorService, workQueue, clientSockets);
                    executorService.submit(handler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Closing the socket will cause RequestHandlers blocking on readLine
            // to get a SocketException (IOException) and exit
            for (Socket s : clientSockets.values()) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Mentat Server");
        MentatServer server = new MentatServer();
        server.start(4000);
    }
}
