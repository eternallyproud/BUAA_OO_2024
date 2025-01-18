import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyMessage implements Message {
    private int id;
    private int type;
    private int socialValue;
    private Tag tag;
    private Person person1;
    private Person person2;

    public MyMessage(int messageId, int messageSocialValue,
                     Person messagePerson1, Person messagePerson2) {
        id = messageId;
        person1 = messagePerson1;
        person2 = messagePerson2;
        socialValue = messageSocialValue;
        type = 0;
        tag = null;
    }

    public MyMessage(int messageId, int messageSocialValue, Person messagePerson1, Tag messageTag) {
        id = messageId;
        tag = messageTag;
        person1 = messagePerson1;
        socialValue = messageSocialValue;
        type = 1;
        person2 = null;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public Person getPerson1() {
        return person1;
    }

    @Override
    public Person getPerson2() {
        return person2;
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            return ((Message) obj).getId() == id;
        }
        return false;
    }
}
