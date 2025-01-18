import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

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
                } else if (request instanceof NormalResetRequest) {
                    task = new NormalReset((NormalResetRequest) request);
                } else if (request instanceof DoubleCarResetRequest) {
                    task = new DoubleCarReset((DoubleCarResetRequest) request);
                } else {
                    task = null;
                    System.out.println("There's an error in InputThread!");
                }
                undoneTask.add(task);
            }
        }
    }
}
