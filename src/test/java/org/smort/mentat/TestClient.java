package org.smort.mentat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {
    private Socket socket;
    private PrintWriter out;

    public TestClient() throws IOException {
        socket = new Socket("localhost", 4000);
        // not using try-with-resources so easier to test...
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        out.println(msg);
    }

    // Hacky way to see if Server closed socket
    // isClosed/Connected won't reflect server hanging up
    // So send a message and see if there is an error
    public boolean socketClosed() {
        out.println("123456789\n");
        return out.checkError();
    }
}
