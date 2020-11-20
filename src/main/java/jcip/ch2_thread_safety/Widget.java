package jcip.ch2_thread_safety;

/*
Demonstrates reentrant locking: subclass holds the lock
it's trying to acquire when calling super.doSomething
 */
public class Widget {
    public synchronized void doSomething() {
    }
}

class LoggingWidget extends Widget {
    public synchronized void doSomething() {
        System.out.println(toString() + ": calling doSomething");
        super.doSomething();
    }
}
