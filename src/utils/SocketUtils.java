package utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtils {

    public final static int TIME_OUT = 500;

    public static ServerSocket getServerSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(TIME_OUT);
        return serverSocket;
    }

    public static Socket getSocket(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        socket.setSoTimeout(TIME_OUT);
        return socket;
    }

    public static boolean check(Socket socket) {
        return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected()&& !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

}
