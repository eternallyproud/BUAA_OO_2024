import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        UndoneTask undoneTask = new UndoneTask();
        ArrayList<Elevator> elevatorList = new ArrayList<>();
        ArrayList<QuestTable> questTableList = new ArrayList<>();
        for (int i = 1; i <= Assistant.MAX_ELEVATOR_NUM; i++) {
            QuestTable questTable = new QuestTable();
            Elevator elevator = new Elevator(i, questTable, undoneTask);
            questTableList.add(questTable);
            elevatorList.add(elevator);
            elevator.start();
        }
        QuestScheduler scheduler = new QuestScheduler(questTableList, undoneTask, elevatorList);
        scheduler.start();
        InputThread inputThread = new InputThread(undoneTask);
        inputThread.start();
    }
}
