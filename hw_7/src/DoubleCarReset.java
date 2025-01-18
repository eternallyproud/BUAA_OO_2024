import com.oocourse.elevator3.DoubleCarResetRequest;

public class DoubleCarReset extends Reset {
    private final Integer elevatorId;
    private final Integer transferFloor;
    private final Integer capacity;
    private final double speed;
    private final TransferFloor transFloor;

    public DoubleCarReset(DoubleCarResetRequest doubleCarResetRequest) {
        elevatorId = doubleCarResetRequest.getElevatorId();
        transferFloor = doubleCarResetRequest.getTransferFloor();
        capacity = doubleCarResetRequest.getCapacity();
        speed = doubleCarResetRequest.getSpeed();
        transFloor = new TransferFloor(transferFloor);
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getTransferFloor() {
        return this.transferFloor;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public double getSpeed() {
        return this.speed;
    }

    public TransferFloor getTransFloor() {
        return transFloor;
    }

}
