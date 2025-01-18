import com.oocourse.elevator3.PersonRequest;

public class Quest implements Task {
    private final int toFloor;
    private int fromFloor;
    private final int personId;

    public Quest(PersonRequest personRequest) {
        this.toFloor = personRequest.getToFloor();
        this.fromFloor = personRequest.getFromFloor();
        this.personId = personRequest.getPersonId();
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getPersonId() {
        return personId;
    }

    public void setFromFloor(int curFloor) {
        fromFloor = curFloor;
    }
}
