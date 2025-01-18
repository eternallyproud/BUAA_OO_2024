import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class MyNetwork implements Network {
    private final HashMap<Integer, Person> persons;
    private final ArrayList<HashMap<Integer, Person>> communities;//图的强分支(最好不要用HashMap套HashSet的结构)
    private int tripleCount;
    private int coupleSum;

    public MyNetwork() {
        persons = new HashMap<>();
        communities = new ArrayList<>();
        tripleCount = 0;
        coupleSum = 0;
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
            final int oldCp1 = ((MyPerson) getPerson(id1)).getBestId();
            final int oldCp2 = ((MyPerson) getPerson(id2)).getBestId();
            ((MyPerson) getPerson(id1)).addRelation(getPerson(id2), value);
            ((MyPerson) getPerson(id2)).addRelation(getPerson(id1), value);
            addRelationToCommunities(id1, id2);
            addRelationToTripleCount(id1, id2);
            manageCoupleSum(id1, id2, oldCp1, oldCp2);
            manageValueSum(id1, id2, value);
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
            int v = queryValue(id1, id2);
            v = (v + value) > 0 ? value : -v;
            final int oldCp1 = ((MyPerson) getPerson(id1)).getBestId();
            final int oldCp2 = ((MyPerson) getPerson(id2)).getBestId();
            ((MyPerson) getPerson(id1)).resetRelation(getPerson(id2), value);
            ((MyPerson) getPerson(id2)).resetRelation(getPerson(id1), value);
            manageCoupleSum(id1, id2, oldCp1, oldCp2);
            manageValueSum(id1, id2, v);
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

    @Override
    public void addTag(int personId, Tag tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (containsPerson(personId) && !getPerson(personId).containsTag((tag.getId()))) {
            getPerson(personId).addTag(tag);
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyEqualTagIdException(tag.getId());
        }
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (containsPerson(personId1) && containsPerson(personId2) && personId1 != personId2
                && getPerson(personId2).isLinked(getPerson(personId1))
                && getPerson(personId2).containsTag(tagId)
                && !getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            if (getPerson(personId2).getTag(tagId).getSize() <= 1111) {
                getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
            }
        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new MyEqualPersonIdException(personId1);
        } else if (!getPerson(personId2).isLinked(getPerson(personId1))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            throw new MyEqualPersonIdException(personId1);
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getValueSum();
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getAgeVar();
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId1) && containsPerson(personId2) &&
                getPerson(personId2).containsTag(tagId) &&
                getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            throw new MyPersonIdNotFoundException(personId1);
        }
    }

    @Override
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            getPerson(personId).delTag(tagId);
        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (containsPerson(id) && ((MyPerson) getPerson(id)).getAcquaintanceNum() != 0) {
            return ((MyPerson) getPerson(id)).getBestId();
        } else if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        } else {
            throw new MyAcquaintanceNotFoundException(id);
        }
    }

    @Override
    public int queryCoupleSum() {
        return coupleSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && isCircle(id1, id2)) {
            if (id1 == id2) {
                return 0;
            }
            HashMap<Integer, Person> startAdded = new HashMap<>();
            startAdded.put(id1, getPerson(id1));
            HashMap<Integer, Person> endAdded = new HashMap<>();
            endAdded.put(id2, getPerson(id2));
            return biBfs(0, new HashMap<>(), new HashMap<>(), startAdded, endAdded);
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyPathNotFoundException(id1, id2);
        }
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

    private void manageCoupleSum(int id1, int id2, int oldCp1, int oldCp2) {
        int newCp1 = ((MyPerson) getPerson(id1)).getBestId();
        int newCp2 = ((MyPerson) getPerson(id2)).getBestId();
        final MyPerson oldPerson1 = (MyPerson) getPerson(oldCp1);
        final MyPerson oldPerson2 = (MyPerson) getPerson(oldCp2);
        final MyPerson newPerson1 = (MyPerson) getPerson(newCp1);
        final MyPerson newPerson2 = (MyPerson) getPerson(newCp2);
        if (oldCp1 == id2 && oldCp2 == id1) {
            coupleSum--;
        }
        if (oldPerson1 != null && id2 != oldCp1 && id1 == oldPerson1.getBestId()) {
            coupleSum--;
        }
        if (oldPerson2 != null && id1 != oldCp2 && id2 == oldPerson2.getBestId()) {
            coupleSum--;
        }
        if (newCp1 == id2 && newCp2 == id1) {
            coupleSum++;
        }
        if (newPerson1 != null && id2 != newCp1 && id1 == newPerson1.getBestId()) {
            coupleSum++;
        }
        if (newPerson2 != null && id1 != newCp2 && id2 == newPerson2.getBestId()) {
            coupleSum++;
        }
    }

    private int biBfs(int depth, HashMap<Integer, Person> start, HashMap<Integer, Person> end,
                      HashMap<Integer, Person> startAdded, HashMap<Integer, Person> endAdded) {
        HashMap<Integer, Person> newAdded = new HashMap<>();
        HashMap<Integer, Person> added = depth % 2 == 0 ? startAdded : endAdded;
        HashMap<Integer, Person> des = depth % 2 == 0 ? endAdded : startAdded;
        HashMap<Integer, Person> reached = depth % 2 == 0 ? start : end;
        for (Person p : added.values()) {
            for (Person person : ((MyPerson) p).getAcquaintance().values()) {
                if (des.containsKey(person.getId())) {
                    return depth;
                }
                if (!reached.containsValue(person) && !added.containsValue(person)) {
                    newAdded.put(person.getId(), person);
                }
            }
        }
        reached.putAll(added);
        return biBfs(depth + 1, start, end,
                depth % 2 == 0 ? newAdded : startAdded, depth % 2 == 0 ? endAdded : newAdded);
    }

    public Person[] getPersons() {
        return persons.values().toArray(new Person[0]);
    }

    private void manageValueSum(int id1, int id2, int v) {
        MyPerson myPerson1 = (MyPerson) getPerson(id1);
        for (Person p : myPerson1.getAcquaintance().values()) {
            if (p.isLinked(getPerson(id2))) {
                for (Tag t : ((MyPerson) p).getTags().values()) {
                    ((MyTag) t).adjustValueSum(getPerson(id1), getPerson(id2), v);
                }
            }
        }
    }
}
