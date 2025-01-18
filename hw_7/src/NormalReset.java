import com.oocourse.elevator3.NormalResetRequest;

public class NormalReset extends Reset {
    private final Integer elevatorId;
    private final Integer capacity;
    private final double speed;

    public NormalReset(NormalResetRequest resetRequest) {
        elevatorId = resetRequest.getElevatorId();
        capacity = resetRequest.getCapacity();
        speed = resetRequest.getSpeed();
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
