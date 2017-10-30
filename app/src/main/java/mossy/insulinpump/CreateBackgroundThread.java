package mossy.insulinpump;

public class CreateBackgroundThread {
    static void run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
