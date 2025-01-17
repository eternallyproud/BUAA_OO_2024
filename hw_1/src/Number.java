import java.math.BigInteger;

public class Number implements Factor {
    private BigInteger num;

    public Number(BigInteger num, int sign) {
        this.num = num.multiply(new BigInteger(String.valueOf(sign)));
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        polynomial.addMonomial(new Monomial(num, 0));
        return polynomial;
    }
}
