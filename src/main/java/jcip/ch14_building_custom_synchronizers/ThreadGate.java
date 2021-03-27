package jcip.ch14_building_custom_synchronizers;

public interface ThreadGate {
    void close();

    void open();

    // BLOCKS-UNTIL: opened-since(generation on entry)
    void await() throws InterruptedException;

    int getProceeded();
}
