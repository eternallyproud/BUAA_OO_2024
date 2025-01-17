import java.math.BigInteger;
import java.util.ArrayList;

public class Polynomial {
    private ArrayList<Monomial> monoList;

    public Polynomial() {
        monoList = new ArrayList<>();
    }

    public void addMonomial(Monomial monomial) {
        for (Monomial mon : monoList) {
            if (mon.canMergeWith(monomial)) {
                mon.addMonomial(monomial);
                return;
            }
        }
        monoList.add(monomial);
    }

    public void addPolynomial(Polynomial polynomial) {
        for (Monomial monomial : polynomial.monoList) {
            addMonomial(monomial);
        }
    }

    public Polynomial mulMonomial(Monomial multiplier) {
        Polynomial result = new Polynomial();
        for (Monomial multiplicand : monoList) {
            result.addMonomial(multiplicand.mulMonomial(multiplier));
        }
        return result;
    }

    public Polynomial mulPolynomial(Polynomial multiplier) {
        Polynomial result = new Polynomial();
        for (Monomial multiplicand : monoList) {
            result.addPolynomial(multiplier.mulMonomial(multiplicand));
        }
        return result;
    }

    public Polynomial powPolynomial(int exponent) {
        Polynomial result = new Polynomial();
        if (exponent == 0) {
            result.addMonomial(new Monomial(BigInteger.ONE, 0));
            return result;
        }
        for (int i = 1; i < exponent; i++) {
            return this.mulPolynomial(this.powPolynomial(exponent - 1));
        }
        return this;
    }

    public String polyToString() {
        StringBuilder sb = new StringBuilder();
        Monomial mark = null;
        for (Monomial monomial : monoList) {
            if (monomial.getCoefficient().compareTo(BigInteger.ZERO) > 0) {
                mark = monomial;
                break;
            }
        }
        if (mark != null) { //一个小小的优化
            sb.append(mark.monoToString(true));
        }
        for (Monomial monomial : monoList) {
            String s = monomial.monoToString(sb.toString().isEmpty());
            if (s != null && monomial != mark) {
                sb.append(s);
            }
        }
        if (sb.toString().isEmpty()) {
            return "0";
        }
        return sb.toString();
    }
}
