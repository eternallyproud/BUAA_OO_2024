import java.math.BigInteger;

public class Power implements Factor {
    private char variable;
    private int exponent;
    private int sign;//no need...

    public Power(int s, char var, int exp) {
        variable = var;
        exponent = exp;
        sign = s;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        if (exponent == 0) {
            polynomial.addMonomial(new Monomial(new BigInteger(String.valueOf(sign)), 0));
        } else {
            polynomial.addMonomial(new Monomial(new BigInteger(String.valueOf(sign)), exponent));
        }
        return polynomial;
    }
}
