import com.oocourse.spec2.exceptions.TagIdNotFoundException;

import java.util.HashMap;

public class MyTagIdNotFoundException extends TagIdNotFoundException {
    private final int id;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyTagIdNotFoundException(int id) {
        this.id = id;
        totalCount++;
        if (!idCount.containsKey(id)) {
            idCount.put(id, 0);
        }
        idCount.put(id, idCount.get(id) + 1);
    }

    @Override
    public void print() {
        System.out.println("tinf-" + totalCount + ", " + id + "-" + idCount.get(id));
    }
}
