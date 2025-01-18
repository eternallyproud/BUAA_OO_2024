import java.util.concurrent.atomic.AtomicBoolean;

public class TransferFloor {
    private final int floor;
    private final AtomicBoolean using = new AtomicBoolean(false);

    public TransferFloor(int transferFloor) {
        floor = transferFloor;
    }

    public int getTransferFloor() {
        return floor;
    }

    public synchronized boolean isUsing() {
        return using.get();
    }

    public synchronized void use() {
        using.set(true);
    }

    public synchronized void finish() {
        using.set(false);
        notifyAll();
    }

    public synchronized void notifyReset() {
        notifyAll();
    }

    public synchronized void waitSignal() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
