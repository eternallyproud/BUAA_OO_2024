import java.util.ArrayList;
import java.util.HashMap;

public class QuestTable {
    private final HashMap<Integer, ArrayList<Quest>> upQuestMap; // should be locked
    private final HashMap<Integer, ArrayList<Quest>> downQuestMap; // should be locked
    private boolean isEnd = false; // should be locked

    public QuestTable() {
        upQuestMap = new HashMap<>();
        downQuestMap = new HashMap<>();
        for (int i = 1; i <= Assistant.MAXFLOOR; i++) {
            upQuestMap.put(i, new ArrayList<>());
            downQuestMap.put(i, new ArrayList<>());
        }
    }

    public synchronized void add(Quest quest) {
        ArrayList<Quest> questList;
        if (quest.getToFloor() >= quest.getFromFloor()) {
            questList = upQuestMap.get(quest.getFromFloor());
        } else {
            questList = downQuestMap.get(quest.getFromFloor());
        }
        questList.add(quest);
        notifyAll();
    }

    public synchronized Quest get(boolean dir, int floor) { //Get and delete. if failed, return null
        ArrayList<Quest> quests = dir ? upQuestMap.get(floor) : downQuestMap.get(floor);
        Quest quest = null;
        if (!quests.isEmpty()) {
            quest = quests.get(0);
            quests.remove(0);
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

    public synchronized boolean isEmpty() {
        for (int i = 1; i <= Assistant.MAXFLOOR; i++) {
            if (!upQuestMap.get(i).isEmpty()) {
                return false;
            }
            if (!downQuestMap.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean questOnFloor(boolean dir, int floor) {
        ArrayList<Quest> quests = dir ? upQuestMap.get(floor) : downQuestMap.get(floor);
        return !quests.isEmpty();
    }

    public synchronized boolean questAvailable(int floor) {
        return !(upQuestMap.get(floor).isEmpty() && downQuestMap.get(floor).isEmpty());
    }

    public synchronized void setEnd() {
        this.isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }
}
