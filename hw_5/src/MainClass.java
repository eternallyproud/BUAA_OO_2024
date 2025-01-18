import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        ArrayList<QuestTable> questTableList = new ArrayList<>();
        for (int i = 1; i <= Assistant.MAXELEVATORNUM; i++) {
            QuestTable questTable = new QuestTable();
            Elevator elevator = new Elevator(i,questTable);
            questTableList.add(questTable);
            elevator.start();
        }
        QuestScheduler questScheduler = new QuestScheduler(questTableList);

        InputThread inputThread = new InputThread(questScheduler);
        inputThread.start();
    }
}
