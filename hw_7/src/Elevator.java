import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator implements Runnable {
    private final int id;
    private int curFloor;
    private int minFloor;
    private int maxFloor;
    private int maxNumber;
    private long moveTime;
    private boolean direction;
    private long lastAction;
    private Thread elevatorThread;
    private final AtomicInteger curNumber = new AtomicInteger(Assistant.INIT_NUM);
    private final QuestTable questTable;
    private final UndoneTask undoneTask;
    private final ArrayList<Quest> curQuest = new ArrayList<>();
    private final ElevatorScheduler elevatorScheduler;
    private final AtomicBoolean isResetting = new AtomicBoolean(false);
    private TransferFloor transferFloor = null;

    public Elevator(int id, QuestTable questTable, UndoneTask undoneTask) {
        this.id = id;
        this.questTable = questTable;
        this.undoneTask = undoneTask;
        curFloor = Assistant.BASE_FLOOR;
        minFloor = Assistant.BASE_FLOOR;
        maxFloor = Assistant.MAX_FLOOR;
        direction = Assistant.INIT_DIRECTION;
        moveTime = Assistant.INIT_MOVE_TIME;
        maxNumber = Assistant.INIT_CAPACITY;
        elevatorScheduler = new ElevatorScheduler(questTable);
    }

    public Elevator(int id, QuestTable questTable, UndoneTask undoneTask, DoubleCarReset carReset) {
        this.id = id;
        this.questTable = questTable;
        this.undoneTask = undoneTask;
        curFloor = carReset.getTransferFloor() + 1;
        minFloor = carReset.getTransferFloor();
        maxFloor = Assistant.MAX_FLOOR;
        direction = Assistant.INIT_DIRECTION;
        moveTime = (long) (carReset.getSpeed() * 1000);
        maxNumber = carReset.getCapacity();
        elevatorScheduler = new ElevatorScheduler(questTable);
        isResetting.set(true);
        transferFloor = carReset.getTransFloor();
    }

    public void start() {
        if (elevatorThread == null) {
            elevatorThread = new Thread(this, "ElevatorThread-" + id);
            elevatorThread.start();
        }
    }

    @Override
    public void run() {
        if (isResetting.get()) {
            transferFloor.waitSignal();
            synchronized (undoneTask) {
                isResetting.set(false);
                questTable.receive(id, "-B");
            }
            undoneTask.notifyScheduler();
        }
        lastAction = System.currentTimeMillis();
        while (true) {
            Advice advice = elevatorScheduler.getAdvice(direction, curFloor, maxFloor, minFloor,
                    curNumber.get(), maxNumber, curQuest, transferFloor);
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
        lastAction = TimableOutput.println("OPEN-" + curFloor + "-" + id + getStr());
        out();
        if (elevatorScheduler.getAdvice(direction, curFloor, maxFloor, minFloor,
                curNumber.get(), maxNumber, curQuest, transferFloor)
                == Advice.REVERSE) {
            reverse();
        }
        long curTime = System.currentTimeMillis();
        if ((curTime - lastAction) < Assistant.OPEN_CLOSE_TIME) {
            try {
                Thread.sleep(Assistant.OPEN_CLOSE_TIME + lastAction - curTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        in();
        lastAction = TimableOutput.println("CLOSE-" + curFloor + "-" + id + getStr());
    }

    private void move() {
        while (true) {
            if (elevatorScheduler.getAdvice(direction, curFloor, maxFloor, minFloor,
                    curNumber.get(), maxNumber, curQuest, transferFloor) == Advice.OPEN) {
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
        if (transferFloor == null) {
            curFloor = direction ? (curFloor + 1) : (curFloor - 1);
            lastAction = TimableOutput.println("ARRIVE-" + curFloor + "-" + id);
        } else {
            int nextFloor = direction ? (curFloor + 1) : (curFloor - 1);
            int transFloor = transferFloor.getTransferFloor();
            if (nextFloor == transFloor) {
                synchronized (transferFloor) {
                    while (transferFloor.isUsing()) {
                        transferFloor.waitSignal();
                    }
                    transferFloor.use();
                }
            }
            lastAction = TimableOutput.println("ARRIVE-" + nextFloor + "-" + id + getStr());
            if (curFloor == transFloor) {
                transferFloor.finish();
            }
            curFloor = nextFloor;
        }
    }

    private void in() {
        Quest inQuest;
        while (questTable.questOnFloor(direction, curFloor) && curNumber.get() < maxNumber) {
            inQuest = questTable.get(direction, curFloor);
            curQuest.add(inQuest);
            curNumber.incrementAndGet();
            TimableOutput.println("IN-" + inQuest.getPersonId() +
                    "-" + curFloor + "-" + id + getStr());
        }
    }

    private void out() {
        Iterator<Quest> it = curQuest.iterator();
        while (it.hasNext()) {
            Quest outQuest = it.next();
            if (outQuest.getToFloor() == curFloor) {
                it.remove();
                curNumber.decrementAndGet();
                TimableOutput.println("OUT-" + outQuest.getPersonId() +
                        "-" + curFloor + "-" + id + getStr());
            }
        }
        if (transferFloor != null && transferFloor.getTransferFloor() == curFloor) {
            it = curQuest.iterator();
            while (it.hasNext()) {
                Quest outQuest = it.next();
                curNumber.decrementAndGet();
                it.remove();
                TimableOutput.println("OUT-" + outQuest.getPersonId() +
                        "-" + curFloor + "-" + id + getStr());
                outQuest.setFromFloor(curFloor);
                synchronized (undoneTask) {
                    undoneTask.addToFront(outQuest);
                }
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
            if (reset instanceof DoubleCarReset) {
                DoubleCarReset doubleCarReset = (DoubleCarReset) reset;
                transferFloor = doubleCarReset.getTransFloor();
                maxFloor = transferFloor.getTransferFloor();
                curFloor = maxFloor - 1;
            }
        }
        try {
            Thread.sleep(Assistant.RESET_TIME + lastAction - System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (undoneTask) {
            lastAction = TimableOutput.println("RESET_END-" + id);
            isResetting.set(false);
            questTable.receive(id, getStr());
            undoneTask.notifyScheduler();
        }
        if (transferFloor != null) {
            transferFloor.notifyReset();
        }
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
        int from = quest.getFromFloor();
        int questAlongSameDir = questTable.questAlongDirection(direction);
        int questAlongOppositeDir = questTable.questAlongDirection(!direction);
        if ((questDir && (maxFloor <= from || minFloor > from)) ||
                (!questDir && (minFloor >= from) || maxFloor < from)) {
            return Integer.MAX_VALUE;
        }
        if ((questAlongSameDir + questAlongOppositeDir + curNumber.get()) == 0) {
            return Math.abs(curFloor - quest.getFromFloor());
        }
        if (questDir == direction) {
            if (curNumber.get() + questAlongSameDir < maxNumber) {
                if (((curFloor > quest.getFromFloor()) ^ questDir)) {
                    return Math.abs(curFloor - quest.getFromFloor());
                } else {
                    return 2 * (maxFloor - minFloor)
                            - Math.abs(curFloor - quest.getFromFloor());
                }
            } else {
                int questLeft = questTable.questLeft(direction, curFloor);
                if ((curNumber.get() + questLeft < maxNumber)
                        && ((curFloor > quest.getFromFloor()) ^ questDir)) {
                    return Math.abs(curFloor - quest.getFromFloor());
                } else {
                    return 2 * ((questAlongSameDir + curNumber.get()) / (maxNumber)) *
                            (maxFloor - minFloor) - Math.abs(curFloor - quest.getFromFloor());
                }
            }
        } else {
            int baseFloor = direction ? maxFloor : minFloor;
            return 2 * (questAlongOppositeDir / (maxNumber + 1)) * (maxFloor - minFloor)
                    + Math.abs(baseFloor - curFloor) + Math.abs(baseFloor - quest.getFromFloor());
        }
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

    public String getStr() {
        return transferFloor == null ? "" :
                (transferFloor.getTransferFloor() > minFloor ? "-A" : "-B");
    }

}
