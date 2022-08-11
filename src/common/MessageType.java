package common;

import java.util.HashMap;

public enum MessageType {

    PING((byte) 0),
    PASSWORD((byte) 1),
    REGISTER((byte) 2),
    NOTIFY((byte) 3),
    TRANSFER((byte) 4),
    ;

    public byte type;

    MessageType(byte type) {
        this.type = type;
    }

    public final static HashMap<Byte, MessageType> CACHE = new HashMap<>();
    static {
        for (MessageType messageType : MessageType.values()) {
            CACHE.put(messageType.type, messageType);
        }
    }
}
