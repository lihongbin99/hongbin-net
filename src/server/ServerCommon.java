package server;

import common.SocketModel;

import java.net.ServerSocket;

public class ServerCommon {

    public static boolean globalError = false;

    public static ServerSocket userServerSocket;
    public static ServerSocket registerServerSocket;
    public static ServerSocket linkServerSocket;

    public static Thread userServerSocketThread;
    public static Thread registerServerSocketThread;
    public static Thread linkServerSocketThread;

    public static SocketModel registerSocket;

}
