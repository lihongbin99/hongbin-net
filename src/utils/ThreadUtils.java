package utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

    public final static ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(
            25,
            50,
            3L,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(100)
    );

}
