import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings({"CallToPrintStackTrace"})
public class QuestTable {
    private Reset reset = null;
    private final HashMap<Integer, ArrayList<Quest>> upQuestMap; // should be locked
    private final HashMap<Integer, ArrayList<Quest>> downQuestMap; // should be locked
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicBoolean isEnd = new AtomicBoolean(false);// should be locked

    public QuestTable() {
        upQuestMap = new HashMap<>();
        downQuestMap = new HashMap<>();
        for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
            upQuestMap.put(i, new ArrayList<>());
            downQuestMap.put(i, new ArrayList<>());
        }
    }

    public void add(Quest quest) {
        ArrayList<Quest> questList = quest.getToFloor() >= quest.getFromFloor() ?
                upQuestMap.get(quest.getFromFloor()) : downQuestMap.get(quest.getFromFloor());
        lock.writeLock().lock();
        try {
            questList.add(quest);
        } finally {
            lock.writeLock().unlock();
        }
        synchronized (this) {
            notifyAll();
        }
    }

    public Quest get(boolean dir, int floor) { //Get and delete. if failed, return null
        ArrayList<Quest> quests = dir ? upQuestMap.get(floor) : downQuestMap.get(floor);
        Quest quest = null;
        lock.writeLock().lock();
        try {
            if (!quests.isEmpty()) {
                quest = quests.get(0);
                quests.remove(0);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return quest;
    }

    public synchronized void waitForQuest() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
                if (!upQuestMap.get(i).isEmpty()) {
                    return false;
                }
                if (!downQuestMap.get(i).isEmpty()) {
                    return false;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return true;
    }

    public boolean questOnFloor(boolean dir, int floor) {
        ArrayList<Quest> quests = dir ? upQuestMap.get(floor) : downQuestMap.get(floor);
        lock.readLock().lock();
        try {
            return !quests.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int questAlongDirection(boolean dir) {
        int sum = 0;
        HashMap<Integer, ArrayList<Quest>> questMap = dir ? upQuestMap : downQuestMap;
        lock.readLock().lock();
        try {
            for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
                sum += questMap.get(i).size();
            }
        } finally {
            lock.readLock().unlock();
        }
        return sum;
    }

    public int totalQuest() {
        int sum = 0;
        lock.readLock().lock();
        try {
            for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
                sum += upQuestMap.get(i).size();
                sum += downQuestMap.get(i).size();
            }
        } finally {
            lock.readLock().unlock();
        }
        return sum;
    }

    public int questLeft(boolean dir, int curFloor) {
        int sum = 0;
        lock.readLock().lock();
        try {
            if (dir) {
                for (int i = curFloor; i <= Assistant.MAX_FLOOR; i++) {
                    sum += upQuestMap.get(i).size();
                }
            } else {
                for (int i = curFloor; i >= Assistant.BASE_FLOOR; i--) {
                    sum += downQuestMap.get(i).size();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return sum;
    }

    public boolean questAvailable(int floor) {
        lock.readLock().lock();
        try {
            return !(upQuestMap.get(floor).isEmpty() && downQuestMap.get(floor).isEmpty());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setEnd() {
        this.isEnd.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    public boolean isEnd() {
        return isEnd.get();
    }

    public void setReset(Reset reset) {
        lock.writeLock().lock();
        try {
            this.reset = reset;
        } finally {
            lock.writeLock().unlock();
        }
        if (reset != null) {
            wake();
        }
    }

    public Reset getReset() {
        lock.readLock().lock();
        try {
            return reset;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void wake() {
        synchronized (this) {
            notifyAll();
        }
    }

    public void receive(int id, String str) {
        lock.readLock().lock();
        try {
            for (int i = 1; i <= Assistant.MAX_FLOOR; i++) {
                ArrayList<Quest> quests = upQuestMap.get(i);
                for (Quest quest : quests) {
                    TimableOutput.println("RECEIVE-" + quest.getPersonId() + "-" + id + str);
                }
                quests = downQuestMap.get(i);
                for (Quest quest : quests) {
                    TimableOutput.println("RECEIVE-" + quest.getPersonId() + "-" + id + str);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}
