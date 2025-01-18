import java.util.ArrayList;

public class QuestScheduler {
    private final ArrayList<QuestTable> questTableList;

    public QuestScheduler(ArrayList<QuestTable> questTableList) {
        this.questTableList = questTableList;
    }

    //hw5
    public void addQuestToSpecifiedTable(int elevatorId, Quest quest) {
        questTableList.get(elevatorId - 1).add(quest);
    }

    public void setEnd() {
        for (int i = 0; i < Assistant.MAXELEVATORNUM; i++) {
            questTableList.get(i).setEnd();
        }
    }
}
