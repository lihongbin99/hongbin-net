package utils;

import common.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PasswordUtils {
    private final static byte[] PASSWORD = Config.PASSWORD.getBytes(StandardCharsets.UTF_8);

    public static void sendPassword(SocketModel socketModel) throws IOException {
        IoUtils.write(socketModel, MessageType.PASSWORD, PASSWORD);
//        IoUtils.write(socketModel, MessageType.PASSWORD, (Config.PASSWORD + "ErrorTest").getBytes(StandardCharsets.UTF_8));
        ReadModel readModel = IoUtils.read(socketModel);
        if (readModel.messageType != MessageType.PASSWORD) {
            socketModel.close();
            throw new StopException("password error");
        }
    }

    public static boolean checkPassword(SocketModel socketModel) throws IOException {
        ReadModel readModel = IoUtils.read(socketModel);
        boolean result = readModel.messageType == MessageType.PASSWORD &&
                null != readModel.content &&
                Arrays.equals(PASSWORD, readModel.content);
        if (result) {
            IoUtils.write(socketModel, MessageType.PASSWORD, null);
        } else {
            socketModel.close();
        }
        return result;
    }

}
