import java.math.BigInteger;
import java.util.Objects;

public class Monomial {
    private BigInteger coefficient;
    private int exponent;

    public Monomial(BigInteger coe, int exp) {
        coefficient = coe;
        exponent = exp;
    }

    public void addMonomial(Monomial monomial) {
        coefficient = coefficient.add(monomial.coefficient);
    }

    public Monomial mulMonomial(Monomial multiplier) {
        BigInteger coe = multiplier.coefficient.multiply(this.coefficient);
        int exp = multiplier.exponent + this.exponent;
        return new Monomial(coe, exp);
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public String monoToString(Boolean start) {
        StringBuilder sb = new StringBuilder();
        BigInteger zero = BigInteger.ZERO;
        BigInteger posOne = new BigInteger("+1");
        BigInteger negOne = new BigInteger("-1");
        if (Objects.equals(coefficient, zero)) {
            return null; //0*x^n
        }
        if (coefficient.compareTo(zero) < 0) { //'+' or '-'
            sb.append("-");
            coefficient = zero.subtract(coefficient); //important change!!!
        } else if (!start) {
            sb.append("+");
        }
        if (exponent == 0) {
            sb.append(coefficient);
        } else if (Objects.equals(coefficient, posOne) || Objects.equals(coefficient, negOne)) {
            sb.append(XtoString());
        } else {
            sb.append(coefficient).append("*");
            sb.append(XtoString());
        }
        return sb.toString();
    }

    private String XtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("x");
        if (exponent != 1) {
            sb.append("^").append(exponent);
        }
        return sb.toString();
    }

    public Boolean canMergeWith(Monomial other) {
        return this.exponent == other.exponent;
    }
}
