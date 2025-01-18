import com.oocourse.spec1.exceptions.RelationNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyRelationNotFoundExceptionTest {

    @Test
    void print() {
        RelationNotFoundException e1 = new MyRelationNotFoundException(1,2);
        e1.print();
        RelationNotFoundException e2 = new MyRelationNotFoundException(3,1);
        e2.print();
        RelationNotFoundException e3 = new MyRelationNotFoundException(3,2);
        e3.print();
    }
}