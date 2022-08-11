package utils;

import common.SocketModel;

import java.time.LocalDateTime;

public class LogUtils {

    public static boolean printDetail = false;

    public static void info(String message) {
        System.out.println(
                LocalDateTime.now() +
                        "thread name: " + Thread.currentThread().getName() +
                        ", message: " + message
        );
    }

    public static void error(String message) {
        error(message, null, null);
    }
    public static void error(String site, SocketModel socketModel) {
        error(site, socketModel, null);
    }
    public static void error(String site, Exception e) {
        error(site, null, e);
    }
    public static void error(String site, SocketModel socketModel, Exception e) {
        System.err.println(
                LocalDateTime.now() +
                        "thread name: " + Thread.currentThread().getName() +
                        ", site: " + site +
                        ", id: " + (null == socketModel ? null : socketModel.id) +
                        ", lock: " + (null == socketModel ? null : socketModel.LOCK) +
                        ", error: " + e
        );
    }

    public static void detail(String message) {
        detail(message, null);
    }
    public static void detail(String message, Integer id) {
        if (printDetail) {
            info("detail ---> " + (id == null ? message : message + id));
        }
    }

}
