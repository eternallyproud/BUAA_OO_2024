import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class QueryCoupleCountTest {
    private final Network newNetwork;
    private final Network oldNetwork;

    public QueryCoupleCountTest(Network newNetwork, Network oldNetwork) {
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
    }

    @Test
    public void testQueryTripleSum() {
        Person[] oldPersons = ((MyNetwork) oldNetwork).getPersons();
        int compare = 0;
        for (int i = 0; i < oldPersons.length; i++) {
            for (int j = i + 1; j < oldPersons.length; j++) {
                try {
                    int bestId1 = oldNetwork.queryBestAcquaintance(oldPersons[i].getId());
                    int bestId2 = oldNetwork.queryBestAcquaintance(oldPersons[j].getId());
                    if (bestId1 == oldPersons[j].getId() && bestId2 == oldPersons[i].getId()) {
                        compare++;
                    }
                } catch (AcquaintanceNotFoundException | PersonIdNotFoundException ignored) {
                }
            }
        }
        int result = newNetwork.queryCoupleSum();
        assertEquals(compare, result);
        //nothing changed
        Person[] newPersons = ((MyNetwork) newNetwork).getPersons();
        //System.out.println(result);
        assertEquals(oldPersons.length, newPersons.length);
        for (int i = 0; i < oldPersons.length; i++) {
            assertEquals(true, ((MyPerson) newPersons[i]).strictEquals(oldPersons[i]));
        }
        for (int i = 0; i < oldPersons.length; i++) {
            for (int j = i + 1; j < oldPersons.length; j++) {
                boolean b1, b2;
                b1 = oldPersons[i].isLinked(oldPersons[j]);
                b2 = newPersons[i].isLinked(newPersons[j]);
                assertEquals(b1, b2);
                assertEquals(false, ((MyPerson) newPersons[i]).strictEquals(newPersons[j]));
            }
        }
    }

    @Parameterized.Parameters
    public static Collection prepareData() {
        int testNum = 200;
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            Network n1 = new MyNetwork();
            Network n2 = new MyNetwork();
            if (i % testNum == 0) {
                generateNoTriple(n1, n2);
            } else if (i % testNum == 1) {
                generateSpecial(n1, n2);
            } else {
                generateSomeTriple(n1, n2);
            }
            object[i] = new Object[]{n1, n2};
        }
        return Arrays.asList(object);
    }

    public static void generateNoTriple(Network n1, Network n2) {
        for (int i = 1; i <= 50; i++) {
            try {
                n1.addPerson(new MyPerson(i, "person" + i, 70 - i));
                n2.addPerson(new MyPerson(i, "person" + i, 70 - i));
            } catch (EqualPersonIdException ignored) {
            }
        }
    }

    public static void generateSpecial(Network n1, Network n2) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        for (int i = 1; i <= 100; i++) {
            try {
                n1.addPerson(new MyPerson(i, "person" + i, 100 - i));
                n2.addPerson(new MyPerson(i, "person" + i, 100 - i));
            } catch (EqualPersonIdException ignored) {
            }
            for (int j = i + 1; j <= 100; j++) {
                try {
                    int value = random.nextInt(10) + 1;
                    n1.addRelation(i, j, value);
                    n2.addRelation(i, j, value);
                } catch (EqualRelationException | PersonIdNotFoundException ignored) {
                }
            }
        }
    }

    public static void generateSomeTriple(Network n1, Network n2) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        ArrayList<Integer> arrayList = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < 100; i++) {
            id = id + 1;
            arrayList.add(id);
            try {
                n1.addPerson(new MyPerson(id, "person-" + id, 0));
                n2.addPerson(new MyPerson(id, "person-" + id, 0));
            } catch (EqualPersonIdException ignored) {
            }
        }
        for (int i = 0; i < 2000; i++) {
            int index1, index2;
            index1 = random.nextInt(100);
            index2 = random.nextInt(100);
            try {
                int value = random.nextInt(30) + 1;
                n1.addRelation(arrayList.get(index1), arrayList.get(index2), value);
                n2.addRelation(arrayList.get(index1), arrayList.get(index2), value);
            } catch (EqualRelationException | PersonIdNotFoundException ignored) {
            }
        }
    }
}
