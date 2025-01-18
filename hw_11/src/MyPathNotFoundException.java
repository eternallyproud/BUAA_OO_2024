import com.oocourse.spec3.exceptions.PathNotFoundException;

import java.util.HashMap;

public class MyPathNotFoundException extends PathNotFoundException {
    private final int id1;
    private final int id2;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyPathNotFoundException(int id1, int id2) {
        if (id1 <= id2) {
            this.id1 = id1;
            this.id2 = id2;
        } else {
            this.id1 = id2;
            this.id2 = id1;
        }
        totalCount++;
        if (!idCount.containsKey(id1)) {
            idCount.put(id1, 0);
        }
        if (!idCount.containsKey(id2)) {
            idCount.put(id2, 0);
        }
        if (id1 == id2) {
            idCount.put(id1, idCount.get(id1) + 1);
        } else {
            idCount.put(id1, idCount.get(id1) + 1);
            idCount.put(id2, idCount.get(id2) + 1);
        }
    }

    @Override
    public void print() {
        System.out.println("pnf-" + totalCount + ", "
                + id1 + "-" + idCount.get(id1) + ", " + id2 + "-" + idCount.get(id2));
    }
}
