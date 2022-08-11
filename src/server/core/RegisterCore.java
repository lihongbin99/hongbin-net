package server.core;

import common.MessageType;
import common.ReadModel;
import common.SocketModel;
import server.ServerCommon;
import utils.IoUtils;
import utils.LogUtils;
import utils.PasswordUtils;
import utils.SocketUtils;

import java.net.Socket;

public class RegisterCore implements Runnable {

    @Override
    public void run() {
        while (!Thread.interrupted() && !ServerCommon.globalError) {
            // procedure2: listener client
            Socket socket;
            try {
                socket = IoUtils.accept(ServerCommon.registerServerSocket);
            } catch (Exception e) {
                ServerCommon.globalError = true;
                throw new RuntimeException(e);
            }

            if (null != socket) {
                LogUtils.detail("new client register to server");
                final SocketModel socketModel = new SocketModel(socket);
                try {
                    // procedure3: check password
                    if (PasswordUtils.checkPassword(socketModel)) {
                        LogUtils.detail("register check password success");

                        // procedure4: register client
                        ReadModel read = IoUtils.read(socketModel);
                        if (read.messageType == MessageType.REGISTER) {
                            IoUtils.write(socketModel, MessageType.REGISTER, null);

                            // reset client socket
                            if (null != ServerCommon.registerSocket && SocketUtils.check(ServerCommon.registerSocket.socket)) {
                                LogUtils.detail("close old register socket");
                                ServerCommon.registerSocket.readThread.interrupt();
                                ServerCommon.registerSocket = null;
                            }

                            ServerCommon.registerSocket = socketModel;
                            LogUtils.info("new register success");
                            ServerCommon.registerSocket.readThread = new Thread(() -> {
                                try {
                                    while (!Thread.interrupted() && SocketUtils.check(socketModel.socket)) {
                                        // refresh ping
                                        IoUtils.read(socketModel);
                                    }
                                } catch (Exception e) {
                                    LogUtils.error("client refresh ping", e);
                                    ServerCommon.registerSocket = null;// reset client socket
                                }
                            });
                            ServerCommon.registerSocket.readThread.start();
                        } else {
                            socketModel.close();
                            LogUtils.detail("register type is not REGISTER, type: " + read.messageType);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.error("register client", e);
                }
            }
        }
    }

}
