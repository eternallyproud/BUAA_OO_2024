import com.oocourse.spec3.exceptions.EqualEmojiIdException;

import java.util.HashMap;

public class MyEqualEmojiIdException extends EqualEmojiIdException {
    private final int id;
    private static int totalCount = 0;
    private static final HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyEqualEmojiIdException(int id) {
        this.id = id;
        totalCount++;
        if (!idCount.containsKey(id)) {
            idCount.put(id, 0);
        }
        idCount.put(id, idCount.get(id) + 1);
    }

    @Override
    public void print() {
        System.out.println("eei-" + totalCount + ", " + id + "-" + idCount.get(id));
    }
}
