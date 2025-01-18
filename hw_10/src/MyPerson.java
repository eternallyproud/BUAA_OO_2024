import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.HashMap;
import java.util.Objects;

public class MyPerson implements Person {
    private int bestId;
    private final int id;
    private final int age;
    private final String name;
    private final HashMap<Integer, Tag> tags;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Person> acquaintance;

    public MyPerson(int id, String name, int age) {
        bestId = 0;
        this.id = id;
        this.name = name;
        this.age = age;
        tags = new HashMap<>();
        value = new HashMap<>();
        acquaintance = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        if (containsTag(id)) {
            return tags.get(id);
        }
        return null;
    }

    @Override
    public void addTag(Tag tag) {
        if (!containsTag(tag.getId())) {
            tags.put(tag.getId(), tag);
        }
    }

    @Override
    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            return ((Person) obj).getId() == id;
        }
        return false;
    }

    @Override
    public boolean isLinked(Person person) {
        return acquaintance.containsKey(person.getId()) || person.getId() == id;
    }

    @Override
    public int queryValue(Person person) {
        if (acquaintance.containsKey(person.getId())) {
            return value.get(person.getId());
        }
        return 0;
    }

    public void addRelation(Person person, int v) {
        acquaintance.put(person.getId(), person);
        value.put(person.getId(), v);
        manageBestId(person.getId(), false, false);
    }

    public void resetRelation(Person person, int v) {
        int newValue = queryValue(person) + v;
        if (newValue > 0) {
            value.put(person.getId(), newValue);
        } else {
            acquaintance.remove(person.getId());
            value.remove(person.getId());
            for (Tag t : tags.values()) {
                t.delPerson(person);
            }
        }
        manageBestId(person.getId(), true, v < 0);
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public int getAcquaintanceNum() {
        return acquaintance.size();
    }

    public boolean strictEquals(Person p) {
        return p.getId() == id && p.getAge() == age && p.getName().equals(name);
    }

    public int getBestId() {
        return bestId;
    }

    private void manageBestId(int personId, boolean delete, boolean reduce) {
        if (delete) {
            if (personId == bestId) {
                if (reduce) {
                    findBestId();
                    return;
                }
            }
        }
        if (value.containsKey(bestId) && value.containsKey(personId)) {
            if (value.get(personId) > value.get(bestId) ||
                    (Objects.equals(value.get(personId), value.get(bestId)) && personId < bestId)) {
                bestId = personId;
            }
        } else if (!value.containsKey(bestId)) {
            findBestId();
        }
    }

    private void findBestId() {
        bestId = 0;
        for (int i : value.keySet()) {
            if (bestId == 0) {
                bestId = i;
            } else {
                if (value.get(i) > value.get(bestId)
                        || (Objects.equals(value.get(i), value.get(bestId)) && i < bestId)) {
                    bestId = i;
                }
            }
        }
    }

    public HashMap<Integer, Tag> getTags() {
        return tags;
    }
}