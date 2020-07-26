package pro.mikey.fabric.xray;

public class ScanTask implements Runnable {
    public ScanTask() {
    }

    @Override
    public void run() {
        System.out.println("Running Job");
    }
}
