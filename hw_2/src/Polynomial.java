import java.math.BigInteger;
import java.util.HashMap;

public class Polynomial {
    private HashMap<Monomial, Monomial> monoMap;

    public Polynomial() {
        monoMap = new HashMap<>();
    }

    public void addMonomial(Monomial monomial) {
        if (monoMap.containsKey(monomial)) {
            Monomial mono = monoMap.get(monomial);
            mono.addMonomial(monomial);
            if (mono.getCoefficient().equals(BigInteger.ZERO)) {
                monoMap.remove(mono);
            }
            return;
        }
        if (!monomial.getCoefficient().equals(BigInteger.ZERO)) {
            monoMap.put(monomial, monomial);
        }
    }

    public void addPolynomial(Polynomial polynomial) {
        for (Monomial monomial : polynomial.monoMap.keySet()) {
            addMonomial(monomial);
        }
    }

    public Polynomial mulMonomial(Monomial multiplier) {
        Polynomial result = new Polynomial();
        for (Monomial multiplicand : monoMap.keySet()) {
            result.addMonomial(multiplicand.mulMonomial(multiplier));
        }
        return result;
    }

    public Polynomial mulPolynomial(Polynomial multiplier) {
        Polynomial result = new Polynomial();
        for (Monomial multiplicand : monoMap.keySet()) {
            result.addPolynomial(multiplier.mulMonomial(multiplicand));
        }
        return result;
    }

    public Polynomial powPolynomial(int power) {
        if (power == 0) {
            Polynomial result = new Polynomial();
            result.addMonomial(new Monomial(BigInteger.ONE));
            return result;
        }
        if (power != 1) {
            return this.mulPolynomial(this.powPolynomial(power - 1));//power can't be too big
        }
        return this;
    }

    public void mulNumber(int num) {
        BigInteger number = new BigInteger(String.valueOf(num));
        for (Monomial monomial : monoMap.keySet()) {
            monomial.mulNumber(number);
        }
    }

    public String polyToString() {
        StringBuilder sb = new StringBuilder();
        Monomial mark = null;
        for (Monomial monomial : monoMap.keySet()) {
            if (monomial.getCoefficient().compareTo(BigInteger.ZERO) > 0) {
                mark = monomial;
                break;
            }
        }
        if (mark != null) { //一个小小的优化
            sb.append(mark.monoToString(true));
        }
        for (Monomial monomial : monoMap.keySet()) {
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

    @Override
    public int hashCode() {
        return monoMap.size();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof Polynomial) {
            Polynomial other = (Polynomial) object;
            if (this.hashCode() != other.hashCode()) {
                return false;
            }
            for (Monomial i : monoMap.keySet()) {
                if (other.monoMap.containsKey(i)) {
                    if (!other.monoMap.get(i).getCoefficient().equals(i.getCoefficient())) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Polynomial createSame() {
        Polynomial newPoly = new Polynomial();
        for (Monomial monomial : monoMap.keySet()) {
            newPoly.addMonomial(monomial.createSame());
        }
        return newPoly;
    }

    public void simplify() {
        for (Monomial mono : monoMap.keySet()) {
            mono.simplify();
        }
    }

    public BigInteger getGcd() {
        boolean flag = true;
        BigInteger gcd = BigInteger.ONE;
        for (Monomial mono : monoMap.keySet()) {
            if (!mono.getCoefficient().equals(BigInteger.ZERO)) {
                if (flag) {
                    flag = false;
                    gcd = mono.getCoefficient().abs();
                } else {
                    gcd = gcd.gcd(mono.getCoefficient());
                }
            }
        }
        return gcd;
    }

    public void divideGcd(BigInteger powerE) {
        for (Monomial mono : monoMap.keySet()) {
            mono.divideGcd(powerE);
        }
    }
}
