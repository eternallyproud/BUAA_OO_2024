import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class MyNetwork implements Network {
    private final HashMap<Integer, Person> persons;
    private final ArrayList<HashMap<Integer, Person>> communities;//图的强分支(最好不要用HashMap套HashSet的结构)
    private int tripleCount;

    public MyNetwork() {
        persons = new HashMap<>();
        communities = new ArrayList<>();
        tripleCount = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        if (containsPerson(id)) {
            return persons.get(id);
        }
        return null;
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (!containsPerson(person.getId())) {
            persons.put(person.getId(), person);
            addCommunity(person);
        } else {
            throw new MyEqualPersonIdException(person.getId());
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        if (containsPerson(id1) && containsPerson(id2)
                && !getPerson(id1).isLinked(getPerson(id2))) {
            ((MyPerson) getPerson(id1)).addRelation(getPerson(id2), value);
            ((MyPerson) getPerson(id2)).addRelation(getPerson(id1), value);
            addRelationToCommunities(id1, id2);
            addRelationToTripleCount(id1, id2);
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyEqualRelationException(id1, id2);
        }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && id1 != id2 &&
                getPerson(id1).isLinked(getPerson(id2))) {
            ((MyPerson) getPerson(id1)).resetRelation(getPerson(id2), value);
            ((MyPerson) getPerson(id2)).resetRelation(getPerson(id1), value);
            if (!getPerson(id1).isLinked(getPerson(id2))) {
                removeRelationFromCommunities(id1, id2);
                removeRelationFromTripleCount(id1, id2);
            }
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else {
            throw new MyRelationNotFoundException(id1, id2);
        }
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && getPerson(id1).isLinked(getPerson(id2))) {
            return getPerson(id1).queryValue(getPerson(id2));
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyRelationNotFoundException(id1, id2);
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2)) {
            for (HashMap<Integer, Person> community : communities) {
                if (community.containsKey(id1) && community.containsKey(id2)) {
                    return true;
                }
            }
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else {
            throw new MyPersonIdNotFoundException(id2);
        }
        return false;
    }

    @Override
    public int queryBlockSum() {
        return communities.size();
    }

    @Override
    public int queryTripleSum() {
        return tripleCount;
    }

    public void addCommunity(Person person) {
        HashMap<Integer, Person> community = new HashMap<>();
        community.put(person.getId(), person);
        communities.add(community);
    }

    public void addRelationToCommunities(int id1, int id2) {
        HashMap<Integer, Person> community = new HashMap<>();
        Iterator<HashMap<Integer, Person>> it = communities.iterator();
        while (it.hasNext()) {
            HashMap<Integer, Person> com = it.next();
            if (com.containsKey(id1)) {
                it.remove();
                community.putAll(com);
            } else if (com.containsKey(id2)) {
                it.remove();
                community.putAll(com);
            }
        }
        communities.add(community);
    }

    private void removeRelationFromCommunities(int id1, int id2) {
        HashMap<Integer, Person> community = null;
        for (HashMap<Integer, Person> com : communities) {
            if (com.containsKey(id1)) {
                community = com;
                break;
            }
        }
        assert community != null;
        HashMap<Integer, Person> newCommunity = new HashMap<>();
        getRelatedPersons(id1, newCommunity);
        if (!newCommunity.containsKey(id2)) { //1、2之间已经不再联通
            community.keySet().removeAll(newCommunity.keySet());
            communities.add(newCommunity);
        }
    }

    private void getRelatedPersons(int id, HashMap<Integer, Person> newCommunity) {
        newCommunity.put(id, getPerson(id));
        HashMap<Integer, Person> acquaintance = ((MyPerson) getPerson(id)).getAcquaintance();
        for (Person p : acquaintance.values()) {
            if (!newCommunity.containsKey(p.getId())) {
                getRelatedPersons(p.getId(), newCommunity);
            }
        }
    }

    private void addRelationToTripleCount(int id1, int id2) {
        Collection<Person> person = ((MyPerson) getPerson(id1)).getAcquaintance().values();
        for (Person p : person) {
            if (p.getId() != id2 && p.isLinked(getPerson(id2))) {
                tripleCount++;
            }
        }
    }

    private void removeRelationFromTripleCount(int id1, int id2) {
        Collection<Person> person = ((MyPerson) getPerson(id1)).getAcquaintance().values();
        for (Person p : person) {
            if (p.getId() != id2 && p.isLinked(getPerson(id2))) {
                tripleCount--;
            }
        }
    }

    public Person[] getPersons() {
        return persons.values().toArray(new Person[0]);
    }
}
