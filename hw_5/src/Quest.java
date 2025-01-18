public class Quest {
    private final int toFloor;
    private final int fromFloor;
    private final int personId;

    public Quest(int toFloor, int fromFloor, int personId) {
        this.toFloor = toFloor;
        this.fromFloor = fromFloor;
        this.personId = personId;
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
}
