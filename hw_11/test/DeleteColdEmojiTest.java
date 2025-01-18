import com.oocourse.spec3.main.*;
import com.oocourse.spec3.exceptions.*;

import java.util.*;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DeleteColdEmojiTest {
    private final Network newNetwork;
    private final Network oldNetwork;

    public DeleteColdEmojiTest(Network newNetwork, Network oldNetwork) {
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
    }

    @Test
    public void testDeleteColdEmoji() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        int limit = random.nextInt(20);
        int count = 0;

        Message[] messages = ((MyNetwork) oldNetwork).getMessages();
        Message[] oldMessages = new Message[messages.length];
        int[] keys = ((MyNetwork) oldNetwork).getEmojiIdList();
        int[] oldKeys = new int[keys.length];
        int[] values = ((MyNetwork) oldNetwork).getEmojiHeatList();
        int[] oldValues = new int[values.length];
        int index = 0;
        for (int i = 0; i < keys.length; i++) {
            if (values[i] >= limit) {
                oldKeys[index] = keys[i];
                oldValues[index++] = values[i];
            }
        }
        count = index;
        index = 0;
        for (int i = 0; i < messages.length; i++) {
            int valid = 0;
            if (messages[i] instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) messages[i]).getEmojiId();
                for (int j = 0; j < count; j++) {
                    if (oldKeys[j] == emojiId) {
                        valid = 1;
                    }
                }
            } else {
                valid = 1;
            }
            if (valid == 1) {
                oldMessages[index++] = messages[i];
            }
        }

        int result = newNetwork.deleteColdEmoji(limit);
        assertEquals(result, count);

        Message[] newMessages = ((MyNetwork) newNetwork).getMessages();
        int[] newKeys = ((MyNetwork) newNetwork).getEmojiIdList();
        int[] newValues = ((MyNetwork) newNetwork).getEmojiHeatList();

        assertEquals(newKeys.length, newValues.length);
        assertEquals(count, newKeys.length);
        assertEquals(index, newMessages.length);

        for (int i = 0; i < index; i++) {
            Message oldMessage = oldMessages[i];
            Message newMessage = newMessages[i];
            assertEquals(oldMessage.getId(), newMessage.getId());
            assertEquals(oldMessage.getType(), newMessage.getType());
            assertEquals(oldMessage.getPerson1(), newMessage.getPerson1());
            assertEquals(oldMessage.getPerson2(), newMessage.getPerson2());
            assertEquals(oldMessage.getSocialValue(), newMessage.getSocialValue());
            if (oldMessage instanceof EmojiMessage && newMessage instanceof EmojiMessage) {
                EmojiMessage oldEmoji = (EmojiMessage) oldMessage;
                EmojiMessage newEmoji = (EmojiMessage) newMessage;
                assertEquals(oldEmoji.getEmojiId(), newEmoji.getEmojiId());
            } else if (oldMessage instanceof NoticeMessage && newMessage instanceof NoticeMessage) {
                NoticeMessage oldNotice = (NoticeMessage) oldMessage;
                NoticeMessage newNotice = (NoticeMessage) newMessage;
                assertEquals(true, oldNotice.getString().equals(newNotice.getString()));
            } else if (oldMessage instanceof RedEnvelopeMessage && newMessage instanceof RedEnvelopeMessage) {
                RedEnvelopeMessage oldRed = (RedEnvelopeMessage) oldMessage;
                RedEnvelopeMessage newRed = (RedEnvelopeMessage) newMessage;
                assertEquals(oldRed.getMoney(), newRed.getMoney());
            } else {
                assertEquals(1, 2);
            }
        }

        for (int i = 0; i < count; i++) {
            assertEquals(oldKeys[i], newKeys[i]);
            assertEquals(oldValues[i], newValues[i]);
        }
    }

    @Parameterized.Parameters
    public static Collection prepareData() {
        int testNum = 20;
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            Network n1 = new MyNetwork();
            Network n2 = new MyNetwork();
            generateSomeData(n1, n2);
            object[i] = new Object[]{n1, n2};
        }
        return Arrays.asList(object);
    }

    public static void generateSomeData(Network n1, Network n2) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        for (int i = 1; i <= 100; i++) {
            try {
                n1.addPerson(new MyPerson(i, "person" + i, 101 - i));
                n2.addPerson(new MyPerson(i, "person" + i, 101 - i));
            } catch (EqualPersonIdException ignored) {
            }
        }
        for (int i = 1; i <= 100; i++) {
            for (int j = i + 1; j <= 100; j++) {
                try {
                    int value = random.nextInt(10) + 1;
                    n1.addRelation(i, j, value);
                    n2.addRelation(i, j, value);
                } catch (PersonIdNotFoundException | EqualRelationException ignored) {
                }
            }
        }
        for (int i = 1; i <= 100; i++) {
            try {
                n1.storeEmojiId(i);
                n2.storeEmojiId(i);
            } catch (EqualEmojiIdException ignored) {
            }
        }
        for (int i = 1; i <= 2000; i++) {
            try {
                int emojiId = random.nextInt(100) + 1;
                int id1 = random.nextInt(100) + 1;
                int id2 = random.nextInt(100) + 1;
                n1.addMessage(new MyEmojiMessage(i, emojiId, n1.getPerson(id1), n1.getPerson(id2)));
                n2.addMessage(new MyEmojiMessage(i, emojiId, n2.getPerson(id1), n2.getPerson(id2)));
            } catch (EqualMessageIdException | EmojiIdNotFoundException | EqualPersonIdException ignored) {
            }
            if (random.nextBoolean()) {
                try {
                    n1.sendMessage(i);
                    n2.sendMessage(i);
                } catch (RelationNotFoundException | MessageIdNotFoundException | TagIdNotFoundException ignored) {
                }
            }
            if (random.nextBoolean()) {
                try {
                    int id1 = random.nextInt(100) + 1;
                    int id2 = random.nextInt(100) + 1;
                    n1.addMessage(new MyNoticeMessage(i + 2000, "notice-" + i, n1.getPerson(id1), n1.getPerson(id2)));
                    n2.addMessage(new MyNoticeMessage(i + 2000, "notice-" + i, n2.getPerson(id1), n2.getPerson(id2)));
                } catch (EqualMessageIdException | EmojiIdNotFoundException | EqualPersonIdException ignored) {
                }
            }
            if (random.nextBoolean()) {
                try {
                    int id1 = random.nextInt(100) + 1;
                    int id2 = random.nextInt(100) + 1;
                    n1.addMessage(new MyRedEnvelopeMessage(i + 4000, i, n1.getPerson(id1), n1.getPerson(id2)));
                    n2.addMessage(new MyRedEnvelopeMessage(i + 4000, i, n2.getPerson(id1), n2.getPerson(id2)));
                } catch (EqualMessageIdException | EmojiIdNotFoundException | EqualPersonIdException ignored) {
                }
            }
        }
    }
}
