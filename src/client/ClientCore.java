package client;

import common.*;
import utils.*;

import java.io.IOException;
import java.net.Socket;

public class ClientCore {

    private static SocketModel oldSocketModel;

    public static void register() throws IOException {
        SocketModel socketModel;
        synchronized (ClientCore.class) {
            LogUtils.detail("start link to server");
            // procedure2: link to server
            Socket registerSocket = SocketUtils.getSocket(Config.SERVER_IP, Config.REGISTER_PORT);

            // procedure3: check password
            socketModel = new SocketModel(registerSocket);
            PasswordUtils.sendPassword(socketModel);
            LogUtils.detail("link to server send password success");

            // procedure4: register client
            IoUtils.write(socketModel, MessageType.REGISTER, null);
            ReadModel registerReadModel = IoUtils.read(socketModel);
            if (MessageType.REGISTER != registerReadModel.messageType) {
                throw new StopException("register error");
            }
            LogUtils.detail("link to server register success");

            // reset
            if (null != oldSocketModel && SocketUtils.check(socketModel.socket)) {
                LogUtils.detail("start close old register");
                oldSocketModel.close();
                oldSocketModel = null;
                LogUtils.detail("close old register success");
            }
            oldSocketModel = socketModel;
        }

        LogUtils.info("register success");

        socketModel.pingErrorFunction = () -> {
            while (true) {
                try {
                    LogUtils.detail("start reset register");
                    ClientCore.register();
                    LogUtils.detail("reset register success");
                    break;
                } catch (Exception e) {
                    LogUtils.error("reset register", e);
                    try {
                        Thread.sleep(5_000);
                    } catch (InterruptedException ignored) { }
                }
            }
        };
        start(socketModel);
    }

    private static void start(final SocketModel socketModel) {
        while (!Thread.interrupted() && SocketUtils.check(socketModel.socket)) {
            ReadModel readModel;
            try {
                // procedure8: listener new user connect
                readModel = IoUtils.read(socketModel);
            } catch (Exception e) {
                LogUtils.error("listener new user connect", e);
                socketModel.restart();
                return;
            }
            if (readModel.messageType == MessageType.NOTIFY) {
                ThreadUtils.THREAD_POOL.execute(() -> {
                    Integer id = IoUtils.byteArrayToInt(readModel.content);
                    LogUtils.detail("new user connect, id: ", id);

                    boolean flag = true;

                    Socket proxySocket = null;
                    try {
                        // procedure9: link to proxy server
                        LogUtils.detail("start link proxy server, id: ", id);
                        proxySocket = new Socket(Config.PROXY_IP, Config.PROXY_PORT);
                        LogUtils.detail("link proxy server success, id: ", id);
                    } catch (IOException e) {
                        flag = false;
                        LogUtils.error("link proxy error", e);
                    }


                    Socket serverSocket = null;
                    if (flag) {
                        try {
                            LogUtils.detail("start link server notify, id: ", id);
                            serverSocket = new Socket(Config.SERVER_IP, Config.LINK_PORT);
                            LogUtils.detail("link server notify success, id: ", id);
                        } catch (IOException e) {
                            flag = false;
                            IoUtils.close(proxySocket);
                            LogUtils.error("link server notify error", e);
                        }
                    }

                    if (flag) {
                        final SocketModel proxySocketModel = new SocketModel(proxySocket, false, false);
                        final SocketModel serverSocketModel = new SocketModel(serverSocket);
                        proxySocketModel.id = id;
                        serverSocketModel.id = id;

                        try {
                            PasswordUtils.sendPassword(serverSocketModel);
                            LogUtils.detail("server notify send password success, id: ", id);
                            // procedure10: notify server
                            LogUtils.detail("start notify server, id: ", id);
                            IoUtils.write(serverSocketModel, MessageType.NOTIFY, readModel.content);

                            // procedure13: confirm notify success
                            if ((IoUtils.read(serverSocketModel)).messageType != MessageType.NOTIFY) {
                                IoUtils.close(proxySocketModel, serverSocketModel);
                                LogUtils.error("notify server error", serverSocketModel);
                                return;
                            }
                            LogUtils.detail("notify server success, id: ", id);

                            // procedure14: transfer data
                            LogUtils.detail("start transfer, id", id);
                            TransferData.transfer(proxySocketModel, serverSocketModel, id);
                        } catch (IOException e) {
                            IoUtils.close(proxySocketModel, serverSocketModel);
                            LogUtils.error("notify server error", e);
                        }
                    }
                });
            }
        }
    }

}
