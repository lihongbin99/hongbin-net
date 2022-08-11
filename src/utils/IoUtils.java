package utils;

import common.MessageType;
import common.ReadModel;
import common.SocketModel;
import common.StopException;
import server.ServerCommon;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class IoUtils {

    private final static int LENGTH_LENGTH = Integer.SIZE / Byte.SIZE;
    public final static int READ_LENGTH = 64 * 1024 - 1 - LENGTH_LENGTH;

    public static Socket accept(ServerSocket serverSocket) throws IOException {
        while (!Thread.interrupted() && !ServerCommon.globalError) {
            try {
                return serverSocket.accept();
            } catch (SocketTimeoutException ignored) { }
        }
        return null;
    }

    public static ReadModel read(SocketModel socketModel) throws IOException {
        ReadModel readModel;
        while ((readModel = doRead(socketModel)).messageType == MessageType.PING) {
            socketModel.lastPingTime = System.currentTimeMillis();
        }
        return readModel;
    }

    private static ReadModel doRead(SocketModel socketModel) throws IOException {
        ReadModel readModel = new ReadModel();
        InputStream inputStream = socketModel.socket.getInputStream();
        // read type
        byte[] typeBytes = doRead(inputStream, 1, readModel);
        if (!readModel.last) {
            readModel.messageType = MessageType.CACHE.get(typeBytes[0]);
            // read length
            if (!readModel.last) {
                byte[] contentLengthBytes = doRead(inputStream, 4, readModel);
                // check contentLength
                if (!readModel.last) {
                    int contentLength = byteArrayToInt(contentLengthBytes);
                    if (contentLength > IoUtils.READ_LENGTH) {
                        throw new StopException("read length error, id: " + socketModel.id + ", length: " + contentLength);
                    }
                    // read content
                    readModel.content = doRead(inputStream, contentLength, readModel);
                }
            }
        }
        return readModel;
    }

    private static byte[] doRead(InputStream inputStream, int maxLen, ReadModel readModel) throws IOException {
        byte[] result = new byte[maxLen];
        int len;
        int readLenSum = 0;
        while (!Thread.interrupted() && readLenSum < maxLen) {
            try {
                if ((len = inputStream.read(result, readLenSum, maxLen - readLenSum)) == -1) {
                    readModel.last = true;
                    break;
                }
                readLenSum += len;
            } catch (SocketTimeoutException ignored) { }
        }
        return result;
    }

    public static void writePing(SocketModel socketModel) throws IOException {
        write(socketModel, MessageType.PING, null);
    }

    public static void write(SocketModel socketModel, MessageType messageType, byte[] content) throws IOException {
        write(socketModel, messageType, content, null == content ? 0 : content.length);
    }

    public static void write(SocketModel socketModel, MessageType messageType, byte[] content, int len) throws IOException {
        OutputStream outputStream = socketModel.socket.getOutputStream();
        byte typeCode = messageType.type;
        // add outputStream lock
        synchronized (socketModel.LOCK) {
            outputStream.write(typeCode);
            outputStream.write(intToByteArray(len));
            if (null != content && content.length >= len) {
                outputStream.write(content, 0, len);
            }
            outputStream.flush();
        }
    }

    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(LENGTH_LENGTH).putInt(i).array();
    }

    public static int byteArrayToInt(byte[] byteArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(LENGTH_LENGTH).put(byteArray);
        byteBuffer.flip();
        return byteBuffer.getInt();
    }

    public static void close(Closeable ... closeables) {
        if (null != closeables && closeables.length > 0) {
            for (Closeable closeable : closeables) {
                if (null != closeable) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
