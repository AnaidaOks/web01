package web;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        final int THREADPOOL_COUNT = 64;
        try {
            Server server = new Server();
            final ExecutorService threadPool = Executors.newFixedThreadPool(THREADPOOL_COUNT);
            Runnable myRunnable = () -> {
                while (true) {
                    server.start();
                }
            };
            //final Future task =
            threadPool.execute(myRunnable);
            threadPool.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}