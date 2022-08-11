package common;

import utils.IoUtils;
import utils.LogUtils;
import utils.SocketUtils;

import java.io.Closeable;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class SocketModel implements Closeable {

    public Integer id;
    public final String LOCK = UUID.randomUUID().toString();

    private final static Map<String, SocketModel> PING_CACHE_MAP = new ConcurrentHashMap<>();

    public Socket socket;

    public Thread readThread;

    private boolean pingRead = true;
    private boolean pingWrite = true;

    public long lastPingTime = System.currentTimeMillis();
    private boolean restart = false;
    public Runnable pingErrorFunction;

    public SocketModel(Socket socket) {
        this.socket = socket;
        // add ping
        PING_CACHE_MAP.put(LOCK, this);
    }

    public SocketModel(Socket socket, boolean pingRead, boolean pingWrite) {
        this(socket);
        this.pingRead = pingRead;
        this.pingWrite = pingWrite;
    }

    private final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    static {
        EXECUTOR.scheduleAtFixedRate(() -> {
            Long currentTimeMillis = System.currentTimeMillis();
            PING_CACHE_MAP.forEach((k, v) -> {
                if (SocketUtils.check(v.socket)) {
                    // read ping
                    if (v.pingRead) {
                        if (currentTimeMillis - v.lastPingTime > 10_000) {
                            LogUtils.error("read ping", v);
                            v.close();
                            v.restart();
                        }
                    }
                    // write ping
                    if (v.pingWrite) {
                        try {
                            LogUtils.detail("start ping, id: ", v.id);
                            IoUtils.writePing(v);
                            LogUtils.detail("ping success, id: ", v.id);
                        } catch (Exception e) {
                            LogUtils.error("write ping", v, e);
                            v.close();
                            v.restart();
                        }
                    }
                } else {
                    v.close();
                    v.restart();
                }
            });
        }, 0, 3_000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void close() {
        LogUtils.detail("start close socketModel, id: ", id);
        PING_CACHE_MAP.remove(this.LOCK);
        if (PING_CACHE_MAP.isEmpty()) {
            LogUtils.detail("start shutdown executor");
            EXECUTOR.shutdown();// TODO Test
            LogUtils.detail("shutdown executor success");
        }
        if (null != this.readThread) {
            this.readThread.interrupt();
        }
        IoUtils.close(this.socket);
        LogUtils.detail("close socketModel success, id: ", id);
    }

    public void restart() {
        synchronized (this) {
            if (null != pingErrorFunction && !restart) {
                restart = true;
                new Thread(() -> pingErrorFunction.run()).start();
            }
        }
    }
}
