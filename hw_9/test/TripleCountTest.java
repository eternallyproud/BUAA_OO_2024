import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TripleCountTest {
    private final Network newNetwork;
    private final Network oldNetwork;

    public TripleCountTest(Network newNetwork, Network oldNetwork) {
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
    }

    @Test
    public void testQueryTripleSum() {
        Person[] oldPersons = ((MyNetwork) oldNetwork).getPersons();
        int compare = 0;
        for (int i = 0; i < oldPersons.length; i++) {
            for (int j = i + 1; j < oldPersons.length; j++) {
                for (int k = j + 1; k < oldPersons.length; k++) {
                    if (oldPersons[i].isLinked(oldPersons[j])
                            && oldPersons[j].isLinked(oldPersons[k])
                            && oldPersons[k].isLinked(oldPersons[i])) {
                        compare++;
                    }
                }
            }
        }
        int result = newNetwork.queryTripleSum();
        assertEquals(compare, result);
        //nothing changed
        Person[] newPersons = ((MyNetwork) newNetwork).getPersons();
        assertEquals(oldPersons.length, newPersons.length);
        for (int i = 0; i < oldPersons.length; i++) {
            assertTrue(((MyPerson) newPersons[i]).strictEquals(oldPersons[i]));
        }
        for (int i = 0; i < oldPersons.length; i++) {
            for (int j = i + 1; j < oldPersons.length; j++) {
                boolean b1, b2;
                b1 = oldPersons[i].isLinked(oldPersons[j]);
                b2 = newPersons[i].isLinked(newPersons[j]);
                assertEquals(b1, b2);
            }
        }
    }

    @Parameterized.Parameters
    public static Collection prepareData() {
        int testNum = 300;
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            Network n1 = new MyNetwork();
            Network n2 = new MyNetwork();
            if (i % 30 == 0) {
                generateNoTriple(n1, n2);
            } else if (i % 30 != 1) {
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

    public static void generateSomeTriple(Network n1, Network n2) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        ArrayList<Integer> arrayList = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < 100; i++) {
            id = id + random.nextInt(10) + 1;
            arrayList.add(id);
            try {
                n1.addPerson(new MyPerson(id, "person-" + id, 0));
                n2.addPerson(new MyPerson(id, "person-" + id, 0));
            } catch (EqualPersonIdException ignored) {
            }
        }
        for (int i = 0; i < 300; i++) {
            int index1, index2;
            index1 = random.nextInt(80);
            index2 = random.nextInt(80);
            try {
                n1.addRelation(arrayList.get(index1), arrayList.get(index2), 1);
                n2.addRelation(arrayList.get(index1), arrayList.get(index2), 1);
            } catch (EqualRelationException | PersonIdNotFoundException ignored) {
            }
        }
    }
}
