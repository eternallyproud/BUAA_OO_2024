import java.util.ArrayList;

public class ElevatorScheduler {
    private final QuestTable questTable;

    public ElevatorScheduler(QuestTable questTable) {
        this.questTable = questTable;
    }

    public Advice getAdvice(boolean dir, int curFloor,
                            int curNum, int maxNum, ArrayList<Quest> curQuest) {
        if (questTable.getReset() != null) {
            return Advice.RESET;
        }
        if (openForIn(dir, curFloor, curNum, maxNum) || openForOut(curFloor, curQuest)) {
            return Advice.OPEN;
        }
        if (curNum != 0) {
            return Advice.CONTINUE;
        }
        if (questTable.isEmpty()) {
            if (questTable.isEnd()) {
                return Advice.END;
            } else {
                return Advice.WAIT;
            }
        }
        if (noFurtherQuest(dir, curFloor)) {
            return Advice.REVERSE;
        } else {
            return Advice.CONTINUE;
        }
    }

    private boolean openForOut(int curFloor, ArrayList<Quest> curQuest) {
        for (int i = 0; i < curQuest.size(); i++) {
            if (curQuest.get(i).getToFloor() == curFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean openForIn(boolean dir, int curFloor, int curNum, int maxNum) {
        if (curNum == maxNum) {
            return false;
        }
        if (questTable.questOnFloor(dir, curFloor)) {
            return true;
        }
        return false;
    }

    private boolean noFurtherQuest(boolean dir, int curFloor) {
        if (dir) {
            for (int i = Assistant.MAX_FLOOR; i > curFloor; i--) {
                if (questTable.questAvailable(i)) {
                    return false;
                }
            }
        } else {
            for (int i = 1; i < curFloor; i++) {
                if (questTable.questAvailable(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}
