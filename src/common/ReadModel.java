package common;

import java.util.Arrays;

public class ReadModel {

    public MessageType messageType;

    public byte[] content;

    public boolean last = false;

    @Override
    public String toString() {
        return "ReadModel{" +
                "messageType=" + messageType +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
