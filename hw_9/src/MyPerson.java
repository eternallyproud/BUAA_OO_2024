import com.oocourse.spec1.main.Person;

import java.util.HashMap;

public class MyPerson implements Person {
    private final int id;
    private final int age;
    private final String name;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Person> acquaintance;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
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
    }

    public void resetRelation(Person person, int v) {
        int newValue = queryValue(person) + v;
        if (newValue > 0) {
            value.put(person.getId(), newValue);
        } else {
            acquaintance.remove(person.getId());
            value.remove(person.getId());
        }
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public boolean strictEquals(Person p) {
        return p.getId() == id && p.getAge() == age && p.getName().equals(name);
    }
}
