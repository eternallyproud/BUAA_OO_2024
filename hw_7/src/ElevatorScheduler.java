import java.util.ArrayList;

public class ElevatorScheduler {
    private final QuestTable questTable;

    public ElevatorScheduler(QuestTable questTable) {
        this.questTable = questTable;
    }

    public Advice getAdvice(boolean dir, int curFloor, int maxFloor, int minFloor, int curNum,
                            int maxNum, ArrayList<Quest> curQuest, TransferFloor transferFloor) {
        if (questTable.getReset() != null) {
            return Advice.RESET;
        }
        if (openForIn(dir, curFloor, curNum, maxNum)
                || openForOut(dir, curFloor, maxFloor, minFloor, curQuest, transferFloor)) {
            return Advice.OPEN;
        }
        if (curNum != 0) {
            return Advice.CONTINUE;
        }
        if (questTable.isEmpty()) {
            if (transferFloor != null && transferFloor.getTransferFloor() == curFloor) {
                int nextFloor = dir ? curFloor + 1 : curFloor - 1;
                if (nextFloor > maxFloor || nextFloor < minFloor) {
                    return Advice.REVERSE;
                } else {
                    return Advice.CONTINUE;
                }
            }
            if (questTable.isEnd()) {
                return Advice.END;
            } else {
                return Advice.WAIT;
            }
        }
        if (noFurtherQuest(dir, curFloor, maxFloor, minFloor)) {
            return Advice.REVERSE;
        } else {
            return Advice.CONTINUE;
        }
    }

    private boolean openForOut(boolean dir, int curFloor, int maxFloor, int minFloor,
                               ArrayList<Quest> curQuest, TransferFloor transFloor) {
        for (Quest quest : curQuest) {
            if (quest.getToFloor() == curFloor) {
                return true;
            }
        }
        int nextFloor = dir ? curFloor + 1 : curFloor - 1;
        if (transFloor != null && transFloor.getTransferFloor() == curFloor &&
                (nextFloor > maxFloor || nextFloor < minFloor) && !curQuest.isEmpty()) {
            return true;
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

    private boolean noFurtherQuest(boolean dir, int curFloor, int maxFloor, int minFloor) {
        if (dir) {
            for (int i = maxFloor; i > curFloor; i--) {
                if (questTable.questAvailable(i)) {
                    return false;
                }
            }
        } else {
            for (int i = minFloor; i < curFloor; i++) {
                if (questTable.questAvailable(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}
