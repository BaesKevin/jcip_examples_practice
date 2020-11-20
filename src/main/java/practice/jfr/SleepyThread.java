package practice.jfr;

// class to reproduce https://github.com/oracle/visualvm/issues/250
public class SleepyThread {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10000);
    }

}
