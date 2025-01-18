import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator implements Runnable {
    private final int id;
    private int curFloor;
    private final AtomicInteger curNumber;
    private int maxNumber;
    private long moveTime;
    private boolean direction;
    private long lastAction;
    private Thread elevatorThread;
    private final QuestTable questTable;
    private final UndoneTask undoneTask;
    private final ArrayList<Quest> curQuest;
    private final ElevatorScheduler elevatorScheduler;
    private final AtomicBoolean isResetting = new AtomicBoolean(false);

    public Elevator(int id, QuestTable questTable, UndoneTask undoneTask) {
        this.id = id;
        this.questTable = questTable;
        this.undoneTask = undoneTask;
        curQuest = new ArrayList<>();
        curFloor = Assistant.BASE_FLOOR;
        direction = Assistant.INIT_DIRECTION;
        moveTime = Assistant.INIT_MOVE_TIME;
        maxNumber = Assistant.INIT_CAPACITY;
        curNumber = new AtomicInteger(Assistant.INIT_NUM);
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
        lastAction = System.currentTimeMillis();
        while (true) {
            Advice advice = elevatorScheduler.getAdvice(direction, curFloor,
                    curNumber.get(), maxNumber, curQuest);
            if (advice == Advice.END) {
                break;
            }
            if (advice == Advice.OPEN) {
                open();
            }
            if (advice == Advice.WAIT) {
                questTable.waitForQuest();
            }
            if (advice == Advice.CONTINUE) {
                move();
            }
            if (advice == Advice.REVERSE) {
                reverse();
            }
            if (advice == Advice.RESET) {
                reset();
            }
        }
    }

    private void open() {
        TimableOutput.println("OPEN-" + curFloor + "-" + id);
        out();
        try {
            Thread.sleep(Assistant.OPEN_CLOSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        in();
        if (elevatorScheduler.getAdvice(direction, curFloor, curNumber.get(), maxNumber, curQuest)
                == Advice.REVERSE) {
            reverse();
            in();
        }
        lastAction = TimableOutput.println("CLOSE-" + curFloor + "-" + id);
    }

    private void move() {
        while (true) {
            if (elevatorScheduler.getAdvice(direction, curFloor,
                    curNumber.get(), maxNumber, curQuest) == Advice.OPEN) {
                open();
            }
            long currentTime = System.currentTimeMillis();
            synchronized (questTable) {
                if (currentTime - lastAction < moveTime) {
                    try {
                        questTable.wait(moveTime - currentTime + lastAction);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
        curFloor = direction ? (curFloor + 1) : (curFloor - 1);
        lastAction = TimableOutput.println("ARRIVE-" + curFloor + "-" + id);
    }

    private void in() {
        Quest inQuest;
        while (questTable.questOnFloor(direction, curFloor) && curNumber.get() < maxNumber) {
            inQuest = questTable.get(direction, curFloor);
            curQuest.add(inQuest);
            curNumber.incrementAndGet();
            TimableOutput.println("IN-" + inQuest.getPersonId() + "-" + curFloor + "-" + id);
        }
    }

    private void out() {
        Iterator<Quest> it = curQuest.iterator();
        while (it.hasNext()) {
            Quest outQuest = it.next();
            if (outQuest.getToFloor() == curFloor) {
                it.remove();
                curNumber.decrementAndGet();
                TimableOutput.println("OUT-" + outQuest.getPersonId() + "-" + curFloor + "-" + id);
            }
        }
        if (curNumber.get() == 0 && questTable.isEmpty()) {
            undoneTask.notifyScheduler();
        }
    }

    private void reset() {
        if (curNumber.get() != 0) {
            lastAction = TimableOutput.println("OPEN-" + curFloor + "-" + id);
            out();
            for (Quest quest : curQuest) {
                TimableOutput.println("OUT-" + quest.getPersonId() + "-" + curFloor + "-" + id);
            }
            try {
                Thread.sleep(Assistant.OPEN_CLOSE_TIME + lastAction - System.currentTimeMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TimableOutput.println("CLOSE-" + curFloor + "-" + id);
        }
        synchronized (undoneTask) {
            isResetting.set(true);
            lastAction = TimableOutput.println("RESET_BEGIN-" + id);
            Reset reset = questTable.getReset();
            questTable.setReset(null);
            transfer();
            maxNumber = reset.getCapacity();
            moveTime = (long) (reset.getSpeed() * 1000);
        }
        try {
            Thread.sleep(Assistant.RESET_TIME + lastAction - System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lastAction = TimableOutput.println("RESET_END-" + id);
        synchronized (undoneTask) {
            isResetting.set(false);
            questTable.receive(id);
        }
        undoneTask.notifyScheduler();
    }

    private void reverse() {
        direction = !direction;
    }

    public int getCurNumber() {
        return curNumber.get();
    }

    public QuestTable getQuestTable() {
        return questTable; // 不会死锁
    }

    public int getId() {
        return id;
    }

    public int getScore(Quest quest) { // return how much floors away from quest
        boolean questDir = quest.getToFloor() > quest.getFromFloor();
        int questAlongSameDir = questTable.questAlongDirection(direction);
        if (questDir == direction) {
            if (curNumber.get() + questAlongSameDir < maxNumber) {
                if (((curFloor > quest.getFromFloor()) ^ questDir)) {
                    return Math.abs(curFloor - quest.getFromFloor());
                } else {
                    return 2 * Assistant.ROUTE_EXPECTATION
                            - Math.abs(curFloor - quest.getFromFloor());
                }
            } else {
                int questLeft = questTable.questLeft(direction, curFloor);
                if (curNumber.get() + questLeft < maxNumber
                        && ((curFloor > quest.getFromFloor()) ^ questDir)) {
                    return Math.abs(curFloor - quest.getFromFloor());
                } else {
                    return 2 * ((questAlongSameDir + curNumber.get()) / (maxNumber)) *
                            Assistant.ROUTE_EXPECTATION - Math.abs(curFloor - quest.getFromFloor());
                }
            }
        } else {
            int questAlongOppositeDir = questTable.questAlongDirection(!direction);
            if ((questAlongSameDir + questAlongOppositeDir + curNumber.get()) == 0) {
                return Math.abs(curFloor - quest.getFromFloor());
            }
            int baseFloor = direction ? Assistant.MAX_FLOOR : Assistant.BASE_FLOOR;
            return 2 * (questAlongOppositeDir / (maxNumber + 1)) * Assistant.ROUTE_EXPECTATION
                    + Math.abs(baseFloor - curFloor) + Math.abs(baseFloor - quest.getFromFloor());
        }
    }

    public int getRandScore() {
        if (isResetting.get()) {
            return Integer.MAX_VALUE;
        }
        return new Random().nextInt();
    }

    private void transfer() {
        Quest quest;
        for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
            while ((quest = questTable.get(true, i)) != null) {
                undoneTask.addToFront(quest);
            }
            while ((quest = questTable.get(false, i)) != null) {
                undoneTask.addToFront(quest);
            }
        }
        Iterator<Quest> it = curQuest.iterator();
        while (it.hasNext()) {
            Quest q = it.next();
            it.remove();
            q.setFromFloor(curFloor);
            curNumber.decrementAndGet();
            undoneTask.addToFront(q);
        }
        undoneTask.notifyScheduler();
    }

    public boolean isResetting() {
        return isResetting.get();
    }

}
