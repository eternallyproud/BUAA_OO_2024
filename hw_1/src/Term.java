import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private ArrayList<Factor> factorList;
    private int sign;

    public Term(int sign) {
        this.factorList = new ArrayList<>();
        this.sign = sign;
    }

    public void addFactor(Factor factor) {
        factorList.add(factor);
    }

    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        if (sign == 1) {
            polynomial.addMonomial(new Monomial(BigInteger.ONE, 0));
        } else {
            polynomial.addMonomial(new Monomial(new BigInteger("-1"), 0));
        }
        for (Factor factor : factorList) {
            polynomial = polynomial.mulPolynomial(factor.toPolynomial());
        }
        return polynomial;
    }
}
