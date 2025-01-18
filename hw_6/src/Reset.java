import com.oocourse.elevator2.ResetRequest;

public class Reset implements Task {
    private final Integer elevatorId;
    private final Integer capacity;
    private final double speed;

    public Reset(ResetRequest resetRequest) {
        this.elevatorId = resetRequest.getElevatorId();
        this.capacity = resetRequest.getCapacity();
        this.speed = resetRequest.getSpeed();
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public double getSpeed() {
        return this.speed;
    }
}
