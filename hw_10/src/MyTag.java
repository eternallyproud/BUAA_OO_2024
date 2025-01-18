import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.Collection;
import java.util.HashMap;

public class MyTag implements Tag {
    private final int id;
    private int valueSum;
    private int ageSum;
    private int agePowSum;
    private final HashMap<Integer, Person> persons;

    public MyTag(int id) {
        this.id = id;
        valueSum = 0;
        ageSum = 0;
        agePowSum = 0;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return ((Tag) obj).getId() == id;
        }
        return false;
    }

    @Override
    public void addPerson(Person person) {
        if (!hasPerson(person)) {
            persons.put(person.getId(), person);
            manageData(person, true);
        }
    }

    @Override
    public boolean hasPerson(Person person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return valueSum;
    }

    @Override
    public int getAgeMean() {
        return persons.isEmpty() ? 0 : ageSum / getSize();
    }

    @Override
    public int getAgeVar() {
        int ageMean = getAgeMean();
        return getSize() == 0 ? 0 :
                (agePowSum - 2 * ageSum * ageMean + getSize() * ageMean * ageMean) / getSize();
    }

    @Override
    public void delPerson(Person person) {
        if (hasPerson(person)) {
            persons.remove(person.getId());
            manageData(person, false);
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    private void manageData(Person person, boolean add) {
        int age = person.getAge();
        Collection<Person> acquaintances = ((MyPerson) person).getAcquaintance().values();
        if (add) {
            ageSum += age;
            agePowSum += age * age;
            for (Person p : acquaintances) {
                if (hasPerson(p)) {
                    valueSum += 2 * person.queryValue(p);
                }
            }
        } else {
            ageSum -= age;
            agePowSum -= age * age;
            for (Person p : acquaintances) {
                if (hasPerson(p)) {
                    valueSum -= 2 * person.queryValue(p);
                }
            }
        }
    }

    public void adjustValueSum(Person person1, Person person2, int v) {
        if (hasPerson(person1) && hasPerson(person2)) {
            valueSum += 2 * v;
        }
    }
}
