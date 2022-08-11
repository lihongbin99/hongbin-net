package server.core;

import common.MessageType;
import common.ReadModel;
import common.SocketModel;
import server.ServerCommon;
import utils.IoUtils;
import utils.LogUtils;
import utils.PasswordUtils;
import utils.ThreadUtils;

import java.net.Socket;

public class LinkCore implements Runnable {

    @Override
    public void run() {
        while (!Thread.interrupted() && !ServerCommon.globalError) {
            Socket socket;
            try {
                socket = IoUtils.accept(ServerCommon.linkServerSocket);
            } catch (Exception e) {
                ServerCommon.globalError = true;
                throw new RuntimeException(e);
            }

            if (null != socket) {
                ThreadUtils.THREAD_POOL.execute(() -> {
                    SocketModel socketModel = new SocketModel(socket);
                    try {
                        if (PasswordUtils.checkPassword(socketModel)) {
                            LogUtils.detail("server notify check password success");
                            // procedure11: save new client connect
                            ReadModel readModel;
                            if ((readModel = IoUtils.read(socketModel)).messageType != MessageType.NOTIFY) {
                                socketModel.close();
                                LogUtils.error("notify server type error, type: " + readModel.messageType);
                                return;
                            }

                            Integer id = IoUtils.byteArrayToInt(readModel.content);
                            LogUtils.detail("notify success, id: ", id);
                            UserCore.SUCCESS_IDS.put(id, socketModel);

                            LogUtils.detail("start notify, id: ", id);
                            synchronized (UserCore.class) {
                                // procedure12: notify all wait thread
                                UserCore.class.notifyAll();
                            }
                        }
                    } catch (Exception e) {
                        socketModel.close();
                        LogUtils.error("receive client notify error", e);
                    }
                });
            }
        }
    }

}
