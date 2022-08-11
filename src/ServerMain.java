import common.Config;
import server.ServerCommon;
import server.core.LinkCore;
import server.core.RegisterCore;
import server.core.UserCore;
import utils.LogUtils;
import utils.SocketUtils;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        // procedure1: start server
        ServerCommon.userServerSocket = SocketUtils.getServerSocket(Config.USER_PORT);
        LogUtils.info("userServerSocket start success");
        ServerCommon.registerServerSocket = SocketUtils.getServerSocket(Config.REGISTER_PORT);
        LogUtils.info("registerServerSocket start success");
        ServerCommon.linkServerSocket = SocketUtils.getServerSocket(Config.LINK_PORT);
        LogUtils.info("linkServerSocket start success");

        LogUtils.info("server start success");

        ServerCommon.userServerSocketThread = new Thread(new UserCore());
        ServerCommon.registerServerSocketThread = new Thread(new RegisterCore());
        ServerCommon.linkServerSocketThread = new Thread(new LinkCore());

        ServerCommon.userServerSocketThread.start();
        ServerCommon.registerServerSocketThread.start();
        ServerCommon.linkServerSocketThread.start();

        LogUtils.info("server threads start success");
    }

}
