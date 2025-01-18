import com.oocourse.spec3.exceptions.MessageIdNotFoundException;

import java.util.HashMap;

public class MyMessageIdNotFoundException extends MessageIdNotFoundException {
    private final int id;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyMessageIdNotFoundException(int id) {
        this.id = id;
        totalCount++;
        if (!idCount.containsKey(id)) {
            idCount.put(id, 0);
        }
        idCount.put(id, idCount.get(id) + 1);
    }

    @Override
    public void print() {
        System.out.println("minf-" + totalCount + ", " + id + "-" + idCount.get(id));
    }
}
