import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class QuestScheduler implements Runnable {
    private Thread schedulerThread;
    private final UndoneTask undoneTask;
    private final ArrayList<Elevator> elevatorList;
    private final ArrayList<QuestTable> questTableList;

    public QuestScheduler(ArrayList<QuestTable> questTableList, UndoneTask undoneTask,
                          ArrayList<Elevator> elevatorList) {
        this.questTableList = questTableList;
        this.undoneTask = undoneTask;
        this.elevatorList = elevatorList;
    }

    public void start() {
        if (schedulerThread == null) {
            schedulerThread = new Thread(this, "SchedulerThread");
            schedulerThread.start();
        }
    }

    private boolean isOver() { // undoneTask ---> questTable, questTable -x-> undoneTask
        synchronized (this) {
            if (!undoneTask.isEmpty() || !undoneTask.isEnd()) {
                return false;
            }
            for (Elevator elevator : elevatorList) {
                if (elevator.getCurNumber() != 0 || !elevator.getQuestTable().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (undoneTask) {
                if (isOver()) {
                    setEnd();
                    break;
                } else {
                    Reset reset;
                    if (undoneTask.isEmpty()) {
                        undoneTask.waitForTask();// notify by waitForTask to end
                    } else if ((reset = undoneTask.getReset()) != null) {
                        questTableList.get(reset.getElevatorId() - 1).setReset(reset);
                    } else {
                        Task task = undoneTask.get();
                        if (task instanceof Quest) {
                            Quest quest = (Quest) task;
                            if (!allocate(quest)) {
                                undoneTask.addToFront(quest);
                            }
                        } else {
                            System.out.println("There's a null pointer in your undoneTask");
                        }
                    }
                }
            }
        }
    }

    private boolean allocate(Quest quest) {
        int min = Integer.MAX_VALUE;
        Elevator allocated = null;
        for (Elevator elevator : elevatorList) {
            //int score = elevator.getRandScore();
            int score = elevator.getScore(quest);
            if (min > score) {
                allocated = elevator;
                min = score;
            }
        }
        if (allocated == null) {
            return false;
        }
        if (!allocated.isResetting()) {
            TimableOutput.println("RECEIVE-" + quest.getPersonId() + "-" + allocated.getId());
        }
        allocated.getQuestTable().add(quest); // get lock of questTable
        return true;
    }

    public void setEnd() {
        for (int i = 0; i < Assistant.MAX_ELEVATOR_NUM; i++) {
            questTableList.get(i).setEnd(); // questTableList will notify every elevator
        }
    }
}
