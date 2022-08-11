package utils;

import common.MessageType;
import common.ReadModel;
import common.SocketModel;

import java.io.InputStream;
import java.io.OutputStream;

public class TransferData {

    public static void transfer(SocketModel clientModel, SocketModel serverSocketModel, Integer id) {
        clientModel.readThread = new Thread(() -> {
            byte[] bytes = new byte[IoUtils.READ_LENGTH];
            int len;
            try {
                InputStream inputStream = clientModel.socket.getInputStream();
                while (!Thread.interrupted() && SocketUtils.check(clientModel.socket)) {
                    LogUtils.detail("start read clientModel content, id: ", id);
                    len = inputStream.read(bytes);
                    LogUtils.detail("read clientModel content length: " + len + ", id: ", id);
                    if (len == -1) {
                        break;
                    }
                    LogUtils.detail("start write clientModel content, id: ", id);
                    IoUtils.write(serverSocketModel, MessageType.TRANSFER, bytes, len);
                    LogUtils.detail("write clientModel content success, id: ", id);
                }
            } catch (Exception e) {
                if (print(e.getMessage())) {
                    e.printStackTrace();
                }
            }
            LogUtils.detail("transfer clientModel complete, id: ", id);
            serverSocketModel.readThread.interrupt();
            IoUtils.close(clientModel, serverSocketModel);
        });

        serverSocketModel.readThread = new Thread(() -> {
            try {
                OutputStream outputStream = clientModel.socket.getOutputStream();
                while (!Thread.interrupted() && SocketUtils.check(serverSocketModel.socket)) {
                    LogUtils.detail("start read serverSocketModel content, id: ", id);
                    ReadModel transfer = IoUtils.read(serverSocketModel);
                    if (null == transfer.messageType) {
                        LogUtils.detail("transfer success, id: ", id);
                        break;
                    }
                    LogUtils.detail("read serverSocketModel content length: " + transfer.content.length + ", id: ", id);
                    if (transfer.messageType == MessageType.TRANSFER && null != transfer.content && transfer.content.length > 0) {
                        LogUtils.detail("start write serverSocketModel content, id: ", id);
                        outputStream.write(transfer.content);
                        outputStream.flush();
                        LogUtils.detail("write serverSocketModel content success, id: ", id);
                    }
                }
            } catch (Exception e) {
                if (print(e.getMessage())) {
                    e.printStackTrace();
                }
            }
            LogUtils.detail("transfer serverSocketModel complete, id: ", id);
            clientModel.readThread.interrupt();
            IoUtils.close(clientModel, serverSocketModel);
        });

        clientModel.readThread.start();
        serverSocketModel.readThread.start();
    }

    public static boolean print(String message) {
        return !(message.contains("Broken pipe") || message.contains("Socket closed"));
    }

}
