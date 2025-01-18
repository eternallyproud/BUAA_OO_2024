import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private final int id;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyAcquaintanceNotFoundException(int id) {
        this.id = id;
        totalCount++;
        if (!idCount.containsKey(id)) {
            idCount.put(id, 0);
        }
        idCount.put(id, idCount.get(id) + 1);
    }

    @Override
    public void print() {
        System.out.println("anf-" + totalCount + ", " + id + "-" + idCount.get(id));
    }
}
