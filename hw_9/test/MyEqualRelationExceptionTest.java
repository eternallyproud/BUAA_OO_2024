import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyEqualRelationExceptionTest {

    @Test
    void print() {
        MyEqualRelationException e1 = new MyEqualRelationException(1, 1);
        e1.print();
        MyEqualRelationException e2 = new MyEqualRelationException(1, 2);
        e2.print();
        MyEqualRelationException e3 = new MyEqualRelationException(2, 1);
        e3.print();
    }
}