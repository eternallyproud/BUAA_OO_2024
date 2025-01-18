import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyNetwork implements Network {
    private int coupleSum = 0;
    private int tripleCount = 0;
    private final HashMap<Integer, Person> persons = new HashMap<>();
    private final HashMap<Integer, Message> messages = new HashMap<>();
    private final HashMap<Integer, Integer> emojiList = new HashMap<>();
    private final ArrayList<HashMap<Integer, Person>> communities = new ArrayList<>();

    public MyNetwork() {}

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) {
            throw new MyEqualPersonIdException(person.getId());
        }
        persons.put(person.getId(), person);
        addCommunity(person);
    }

    @Override
    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualRelationException {
        PersonIdNotFound(id1);
        PersonIdNotFound(id2);
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        final int oldCp1 = ((MyPerson) getPerson(id1)).getBestId();
        final int oldCp2 = ((MyPerson) getPerson(id2)).getBestId();
        ((MyPerson) getPerson(id1)).addRelation(getPerson(id2), value);
        ((MyPerson) getPerson(id2)).addRelation(getPerson(id1), value);
        addRelationToCommunities(id1, id2);
        manageTripleCount(id1, id2, true);
        manageCoupleSum(id1, id2, oldCp1, oldCp2);
        manageValueSum(id1, id2, value);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        PersonIdNotFound(id1);
        PersonIdNotFound(id2);
        EqualPersonId(id1, id2);
        RelationNotFound(id1, id2);
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
            manageTripleCount(id1, id2, false);
        }
    }

    @Override
    public int queryValue(int id1, int id2)
            throws PersonIdNotFoundException, RelationNotFoundException {
        PersonIdNotFound(id1);
        PersonIdNotFound(id2);
        RelationNotFound(id1, id2);
        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        PersonIdNotFound(id1);
        PersonIdNotFound(id2);
        for (HashMap<Integer, Person> community : communities) {
            if (community.containsKey(id1) && community.containsKey(id2)) {
                return true;
            }
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
        PersonIdNotFound(personId);
        if (getPerson(personId).containsTag((tag.getId()))) {
            throw new MyEqualTagIdException(tag.getId());
        }
        getPerson(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        PersonIdNotFound(personId1);
        PersonIdNotFound(personId2);
        EqualPersonId(personId1, personId2);
        RelationNotFound(personId1, personId2);
        TagIdNotFound(personId2, tagId);
        if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new MyEqualPersonIdException(personId1);
        }
        if (getPerson(personId2).getTag(tagId).getSize() <= 1111) {
            getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        PersonIdNotFound(personId);
        TagIdNotFound(personId, tagId);
        return getPerson(personId).getTag(tagId).getValueSum();
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        PersonIdNotFound(personId);
        TagIdNotFound(personId, tagId);
        return getPerson(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        PersonIdNotFound(personId1);
        PersonIdNotFound(personId2);
        TagIdNotFound(personId2, tagId);
        PersonIdNotFound(personId1, personId2, tagId);
        getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
    }

    @Override
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        PersonIdNotFound(personId);
        TagIdNotFound(personId, tagId);
        getPerson(personId).delTag(tagId);
    }

    @Override
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public void addMessage(Message message) throws
            EqualMessageIdException, EmojiIdNotFoundException, EqualPersonIdException {
        if (!containsMessage(message.getId())) {
            if ((message instanceof EmojiMessage)
                    && !containsEmojiId(((EmojiMessage) message).getEmojiId())) {
                throw new MyEmojiIdNotFoundException(((EmojiMessage) message).getEmojiId());
            } else if (message.getType() == 0
                    && message.getPerson1().equals(message.getPerson2())) {
                throw new MyEqualPersonIdException(message.getPerson1().getId());
            } else {
                messages.put(message.getId(), message);
            }
        } else {
            throw new MyEqualMessageIdException(message.getId());
        }
    }

    @Override
    public Message getMessage(int id) {
        return messages.getOrDefault(id, null);
    }

    @Override
    public void sendMessage(int id) throws
            RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        Message message = getMessage(id);
        if (!containsMessage(id)) {
            throw new MyMessageIdNotFoundException(id);
        } else if (message.getType() == 0
                && !(message.getPerson1().isLinked(message.getPerson2()))) {
            throw new MyRelationNotFoundException(message.getPerson1().getId(),
                    message.getPerson2().getId());
        } else if (message.getType() == 1
                && !message.getPerson1().containsTag(message.getTag().getId())) {
            throw new MyTagIdNotFoundException(message.getTag().getId());
        }
        if (message.getType() == 0 && message.getPerson1() != message.getPerson2()) {
            messages.remove(id);
            message.getPerson1().addSocialValue(message.getSocialValue());
            message.getPerson2().addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessage) {
                message.getPerson1().addMoney(-((RedEnvelopeMessage) message).getMoney());
                message.getPerson2().addMoney(((RedEnvelopeMessage) message).getMoney());
            }
            if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emojiList.replace(emojiId, emojiList.get(emojiId) + 1);
            }
            message.getPerson2().getMessages().add(0, message);
        } else if (message.getType() == 1) {
            MyTag tag = (MyTag) message.getTag();
            messages.remove(id);
            message.getPerson1().addSocialValue(message.getSocialValue());
            tag.addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessage && tag.getSize() > 0) {
                int amount = ((RedEnvelopeMessage) message).getMoney() / tag.getSize();
                message.getPerson1().addMoney(-tag.getSize() * amount);
                tag.addMoney(amount);
            }
            if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emojiList.replace(emojiId, emojiList.get(emojiId) + 1);
            }
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        PersonIdNotFound(id);
        return getPerson(id).getSocialValue();
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        PersonIdNotFound(id);
        return getPerson(id).getReceivedMessages();
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojiList.containsKey(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (!containsEmojiId(id)) {
            emojiList.put(id, 0);
        } else {
            throw new MyEqualEmojiIdException(id);
        }
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        PersonIdNotFound(id);
        return getPerson(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (containsEmojiId(id)) {
            return emojiList.get(id);
        } else {
            throw new MyEmojiIdNotFoundException(id);
        }
    }

    @Override
    public int deleteColdEmoji(int limit) {
        emojiList.entrySet().removeIf(i -> i.getValue() < limit);
        Iterator<Map.Entry<Integer, Message>> iterator2 = messages.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<Integer, Message> i = iterator2.next();
            if (i.getValue() instanceof EmojiMessage) {
                if (!containsEmojiId(((EmojiMessage) (i.getValue())).getEmojiId())) {
                    iterator2.remove();
                }
            }
        }
        return emojiList.size();
    }

    @Override
    public void clearNotices(int personId) throws PersonIdNotFoundException {
        PersonIdNotFound(personId);
        List<Message> oldMessages = getPerson(personId).getMessages();
        List<Message> newMessages = new ArrayList<>();
        for (Message message : oldMessages) {
            if (!(message instanceof NoticeMessage)) {
                newMessages.add(message);
            }
        }
        oldMessages.clear();
        oldMessages.addAll(newMessages);
    }

    @Override
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        PersonIdNotFound(id);
        if (((MyPerson) getPerson(id)).getAcquaintanceNum() == 0) {
            throw new MyAcquaintanceNotFoundException(id);
        }
        return ((MyPerson) getPerson(id)).getBestId();
    }

    @Override
    public int queryCoupleSum() {
        return coupleSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        PersonIdNotFound(id1);
        PersonIdNotFound(id2);
        if (!isCircle(id1, id2)) {
            throw new MyPathNotFoundException(id1, id2);
        }
        if (id1 == id2) {
            return 0;
        }
        HashMap<Integer, Person> startAdded = new HashMap<>();
        startAdded.put(id1, getPerson(id1));
        HashMap<Integer, Person> endAdded = new HashMap<>();
        endAdded.put(id2, getPerson(id2));
        return biBfs(0, new HashMap<>(), new HashMap<>(), startAdded, endAdded);
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

    private void manageTripleCount(int id1, int id2, boolean add) {
        Collection<Person> person = ((MyPerson) getPerson(id1)).getAcquaintance().values();
        for (Person p : person) {
            tripleCount += (p.getId() != id2 && p.isLinked(getPerson(id2))) ? (add ? 1 : -1) : 0;
        }
    }

    private void manageCoupleSum(int id1, int id2, int oldCp1, int oldCp2) {
        int newCp1 = ((MyPerson) getPerson(id1)).getBestId();
        int newCp2 = ((MyPerson) getPerson(id2)).getBestId();
        final MyPerson oldPerson1 = (MyPerson) getPerson(oldCp1);
        final MyPerson oldPerson2 = (MyPerson) getPerson(oldCp2);
        final MyPerson newPerson1 = (MyPerson) getPerson(newCp1);
        final MyPerson newPerson2 = (MyPerson) getPerson(newCp2);
        coupleSum -= (oldCp1 == id2 && oldCp2 == id1) ? 1 : 0;
        coupleSum -= (oldPerson1 != null && id2 != oldCp1 && id1 == oldPerson1.getBestId()) ? 1 : 0;
        coupleSum -= (oldPerson2 != null && id1 != oldCp2 && id2 == oldPerson2.getBestId()) ? 1 : 0;
        coupleSum += (newCp1 == id2 && newCp2 == id1) ? 1 : 0;
        coupleSum += (newPerson1 != null && id2 != newCp1 && id1 == newPerson1.getBestId()) ? 1 : 0;
        coupleSum += (newPerson2 != null && id1 != newCp2 && id2 == newPerson2.getBestId()) ? 1 : 0;
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

    public int[] getEmojiIdList() { return null; }

    public int[] getEmojiHeatList() { return null; }

    public Message[] getMessages() { return messages.values().toArray(new Message[0]); }

    private void PersonIdNotFound(int id) throws MyPersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    private void PersonIdNotFound(int id1, int id2, int tagId) throws MyPersonIdNotFoundException {
        if (!getPerson(id2).getTag(tagId).hasPerson(getPerson(id1))) {
            throw new MyPersonIdNotFoundException(id1);
        }
    }

    private void EqualPersonId(int id1, int id2) throws MyEqualPersonIdException {
        if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        }
    }

    private void RelationNotFound(int id1, int id2) throws MyRelationNotFoundException {
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
    }

    private void TagIdNotFound(int personId, int tagId) throws MyTagIdNotFoundException {
        if (!getPerson(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        }
    }
}