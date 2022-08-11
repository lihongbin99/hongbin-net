package server.core;

import common.MessageType;
import common.SocketModel;
import server.ServerCommon;
import utils.*;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserCore implements Runnable {

    private final static AtomicInteger ID = new AtomicInteger();

    public final static Map<Integer, SocketModel> SUCCESS_IDS = new ConcurrentHashMap<>();

    @Override
    public void run() {
        while (!Thread.interrupted() && !ServerCommon.globalError) {
            // procedure5: user link server
            Socket socket;
            try {
                if (null == (socket = IoUtils.accept(ServerCommon.userServerSocket))) {
                    continue;
                }
            } catch (Exception e) {
                ServerCommon.globalError = true;
                throw new RuntimeException(e);
            }

            ThreadUtils.THREAD_POOL.execute(() -> {
                Integer id = ID.incrementAndGet();
                byte[] idBytes = IoUtils.intToByteArray(id);
                LogUtils.detail("new user connect, id: ", id);

                synchronized (UserCore.class) {
                    // check registerSocket
                    if (null != ServerCommon.registerSocket && SocketUtils.check(ServerCommon.registerSocket.socket)) {
                        try {
                            // procedure6: notify client
                            LogUtils.detail("start notify client, id: ", id);
                            IoUtils.write(ServerCommon.registerSocket, MessageType.NOTIFY, idBytes);
                            LogUtils.detail("notify client success, id: ", id);
                        } catch (Exception e) {LogUtils.error("notify client", null, e);
                            IoUtils.close(ServerCommon.registerSocket, socket);
                            ServerCommon.registerSocket = null;
                            return;
                        }
                    } else {
                        IoUtils.close(socket);
                        LogUtils.error("new user connect but no client register server");
                        return;
                    }
                    while (!SUCCESS_IDS.containsKey(id)) {
                        try {
                            // procedure7: sleep
                            LogUtils.detail("thread start wait, id: ", id);
                            UserCore.class.wait();
                        } catch (InterruptedException ignored) { }
                    }
                }
                LogUtils.detail("thread notify success, id: ", id);

                // procedure12: wake
                final SocketModel projectSocketModel = SUCCESS_IDS.remove(id);
                final SocketModel userSocketModel = new SocketModel(socket, false, false);
                projectSocketModel.id = id;
                userSocketModel.id = id;

                // procedure13: confirm notify success
                try {
                    LogUtils.detail("start notify client notify success, id: ", id);
                    IoUtils.write(projectSocketModel, MessageType.NOTIFY, null);
                    LogUtils.detail("notify client notify success success, id: ", id);
                } catch (Exception e) {
                    IoUtils.close(userSocketModel, projectSocketModel);
                    LogUtils.error("notify client notify success", e);
                    return;
                }

                // procedure14: transfer data
                LogUtils.detail("start transfer, id", id);
                TransferData.transfer(userSocketModel, projectSocketModel, id);
            });
        }
    }

}
