import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Test {

    static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws InterruptedException, ParseException {

        Timer timer = new Timer();

        for (int i = 0; i < 10000000; i++) {
            add(timer);
        }
        System.out.println("add success");
    }

    private static void add(Timer timer) throws ParseException {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("start");
            }
        }, sf.parse("2021-06-23 23:14:00"));
    }

}
