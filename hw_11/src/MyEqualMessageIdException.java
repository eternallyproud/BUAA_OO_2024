import com.oocourse.spec3.exceptions.EqualMessageIdException;

import java.util.HashMap;

public class MyEqualMessageIdException extends EqualMessageIdException {
    private final int id;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyEqualMessageIdException(int id) {
        this.id = id;
        totalCount++;
        if (!idCount.containsKey(id)) {
            idCount.put(id, 0);
        }
        idCount.put(id, idCount.get(id) + 1);
    }

    @Override
    public void print() {
        System.out.println("emi-" + totalCount + ", " + id + "-" + idCount.get(id));
    }
}
