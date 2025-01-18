import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.io.IOException;

public class InputThread implements Runnable {
    //InputThread will never wait, and will close the moment it can.
    private Thread inputThread;
    private final UndoneTask undoneTask;

    public InputThread(UndoneTask undoneTask) {
        this.undoneTask = undoneTask; // shared class 1
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
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                // no more quest
                try {
                    elevatorInput.close();
                    undoneTask.setEnd();
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Task task;
                if (request instanceof PersonRequest) {
                    task = new Quest((PersonRequest) request);
                } else {
                    task = new Reset((ResetRequest) request);
                }
                undoneTask.add(task);
            }
        }
    }
}
