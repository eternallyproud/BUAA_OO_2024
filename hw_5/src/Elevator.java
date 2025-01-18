import com.oocourse.elevator1.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;

public class Elevator implements Runnable {
    private int id;
    private int curFloor;
    private int curNumber;
    private long waitTime;
    private boolean direction;
    private Thread elevatorThread;
    private final QuestTable questTable;
    private final ArrayList<Quest> curQuest;
    private final ElevatorScheduler elevatorScheduler;

    public Elevator(int id, QuestTable questTable) {
        this.id = id;
        this.questTable = questTable;
        curQuest = new ArrayList<>();
        curNumber = Assistant.INITNUM;
        curFloor = Assistant.BASEFLOOR;
        direction = Assistant.INITDIRCTION;
        elevatorScheduler = new ElevatorScheduler(questTable);
    }

    public void start() {
        if (elevatorThread == null) {
            elevatorThread = new Thread(this, "ElevatorThread-" + id);
            elevatorThread.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = elevatorScheduler.getAdvice(direction, curFloor, curNumber, curQuest);
            if (advice == Advice.END) {
                break;
            }
            if (advice == Advice.OPEN) {
                open();
            }
            if (advice == Advice.WAIT) {
                long oldTime = System.currentTimeMillis();
                questTable.waitForQuest();
                waitTime = waitTime + System.currentTimeMillis() - oldTime;
            }
            if (advice == Advice.CONTINUE) {
                move();
            }
            if (advice == Advice.REVERSE) {
                reverse();
            }
        }
    }

    private void open() {
        clearWaitTime();
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        out();
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        in();
        if (elevatorScheduler.getAdvice(direction, curFloor, curNumber, curQuest)
                == Advice.REVERSE) {
            reverse();
            in();
        }
        TimableOutput.println("CLOSE-" + curFloor + "-" + id);
    }

    private void move() {
        curFloor = direction ? (curFloor + 1) : (curFloor - 1);
        try {
            if (waitTime > 400) {
                waitTime = 400;
            }
            Thread.sleep(400 - waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TimableOutput.println("ARRIVE-" + curFloor + "-" + id);
        clearWaitTime();
    }

    private void in() {
        Quest inQuest;
        while (questTable.questOnFloor(direction, curFloor) && curNumber < Assistant.MAXCAPASITY) {
            inQuest = questTable.get(direction, curFloor);
            curQuest.add(inQuest);
            curNumber++;
            TimableOutput.println("IN-" + inQuest.getPersonId() + "-" + curFloor + "-" + id);
        }
    }

    private void out() {
        Iterator<Quest> it = curQuest.iterator();
        while (it.hasNext()) {
            Quest outQuest = it.next();
            if (outQuest.getToFloor() == curFloor) {
                it.remove();
                curNumber--;
                TimableOutput.println("OUT-" + outQuest.getPersonId() + "-" + curFloor + "-" + id);
            }
        }
    }

    private void reverse() {
        direction = !direction;
    }

    private void clearWaitTime() {
        waitTime = 0;
    }
}
