import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;

public class InputThread implements Runnable {
    private Thread inputThread;
    private final QuestScheduler questScheduler;

    public InputThread(QuestScheduler questScheduler) {
        this.questScheduler = questScheduler;
    }

    public void start() {
        if (inputThread == null) {
            inputThread = new Thread(this, "InputThread");
            inputThread.start();
        }
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) { // Scanner actually doesn't consume CPU time
            PersonRequest request = elevatorInput.nextPersonRequest();
            if (request == null) {
                // no more quest
                try {
                    elevatorInput.close();
                    questScheduler.setEnd();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                int toFloor = request.getToFloor();
                int fromFloor = request.getFromFloor();
                int personId = request.getPersonId();
                Quest quest = new Quest(toFloor, fromFloor, personId);
                int elevatorId = request.getElevatorId();
                questScheduler.addQuestToSpecifiedTable(elevatorId, quest);
            }
        }
    }
}
