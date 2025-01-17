import java.math.BigInteger;
import java.util.Objects;

public class Monomial {
    private BigInteger coefficient;
    private BigInteger powerX;//power of x
    private Polynomial expPolynomial = new Polynomial();
    private BigInteger powerE;

    public Monomial(BigInteger coe, BigInteger powerX, Polynomial poly) {
        coefficient = coe;
        this.powerX = powerX;
        expPolynomial = poly;
    }

    public Monomial(BigInteger coe) {
        coefficient = coe;
        powerX = BigInteger.ZERO;
    }

    public Monomial(int powerX) {
        coefficient = BigInteger.ONE;
        this.powerX = new BigInteger(String.valueOf(powerX));
    }

    public Monomial(Polynomial polynomial, int expPower) {
        coefficient = BigInteger.ONE;
        powerX = BigInteger.ZERO;
        if (expPower != 0) {
            expPolynomial.addPolynomial(polynomial);
            expPolynomial.mulNumber(expPower);
        }
    }

    public void addMonomial(Monomial monomial) {
        coefficient = coefficient.add(monomial.coefficient);
    }

    public Monomial mulMonomial(Monomial multiplier) {
        BigInteger coe = multiplier.coefficient.multiply(this.coefficient);
        BigInteger pow = multiplier.powerX.add(this.powerX);
        Polynomial poly = new Polynomial();
        poly.addPolynomial(expPolynomial.createSame());
        poly.addPolynomial(multiplier.expPolynomial.createSame());
        return new Monomial(coe, pow, poly);
    }

    public void mulNumber(BigInteger num) {
        coefficient = coefficient.multiply(num);
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public String monoToString(Boolean start) {
        StringBuilder sb = new StringBuilder();
        BigInteger zero = BigInteger.ZERO;
        BigInteger posOne = BigInteger.ONE;
        if (Objects.equals(coefficient, zero)) {
            return null; //0*x^n
        }
        if (coefficient.compareTo(zero) < 0) { //'+' or '-'
            sb.append("-");
            coefficient = zero.subtract(coefficient); //important change!!!
        } else if (!start) {
            sb.append("+");
        }
        String s = EtoString();
        if (s == null) {
            if (powerX.equals(zero)) {
                sb.append(coefficient);
            } else if (Objects.equals(coefficient, posOne)) {
                sb.append(XtoString());
            } else {
                sb.append(coefficient).append("*");
                sb.append(XtoString());
            }
        } else {
            if (!Objects.equals(coefficient, posOne)) {
                sb.append(coefficient).append("*");
            }
            if (!powerX.equals(zero)) {
                sb.append(XtoString()).append("*");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private String EtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("exp(");
        boolean isEmpty = expPolynomial.isEmpty();
        boolean isNumber = expPolynomial.isNumber();
        if (isEmpty) {
            return null;
        }
        if (isNumber) {
            BigInteger num = expPolynomial.getNumber().multiply(powerE);
            sb.append(num).append(")");
            return sb.toString();
        }
        String s = expPolynomial.polyToString();
        char[] charArray = s.toCharArray();
        int bracketCount = 0;
        boolean flag = true;
        for (Character c : charArray) {
            if (c == '(') {
                bracketCount++;
            }
            if (c == ')') {
                bracketCount--;
            }
            if ((bracketCount == 0) && ("+-*".indexOf(c) != -1)) {
                flag = false;
            }
        }
        if (flag) {
            sb.append(s).append(")");
        } else {
            sb.append("(").append(s).append("))");
        }
        if (!Objects.equals(powerE, BigInteger.ONE)) {
            sb.append("^").append(powerE);
        }
        return sb.toString();
    }

    private String XtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("x");
        if (!powerX.equals(BigInteger.ONE)) {
            sb.append("^").append(powerX);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return powerX.intValue();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof Monomial) {
            Monomial other = (Monomial) object;
            boolean sizeMatch = this.hashCode() == other.hashCode();
            return this.expPolynomial.equals(other.expPolynomial) && sizeMatch;
        } else {
            return false;
        }
    }

    public Monomial createSame() {
        BigInteger coe = new BigInteger(String.valueOf(this.coefficient));
        BigInteger pow = new BigInteger(String.valueOf(this.powerX));
        Polynomial poly = this.expPolynomial.createSame();
        return new Monomial(coe, pow, poly);
    }

    public void simplify() {
        powerE = expPolynomial.getGcd();
        expPolynomial.divideGcd(powerE);
        expPolynomial.simplify();
    }

    public void divideGcd(BigInteger pow) {
        coefficient = coefficient.divide(pow);
    }

    public Polynomial deprive() {
        Polynomial polynomial = new Polynomial();
        if (!powerX.equals(BigInteger.ZERO)) {
            BigInteger coe = coefficient.multiply(powerX);
            BigInteger pow = powerX.subtract(BigInteger.ONE);
            Polynomial poly = expPolynomial.createSame();
            Monomial mono = new Monomial(coe, pow, poly);
            polynomial.addMonomial(mono);
        }
        Polynomial poly = expPolynomial.deprive();
        polynomial.addPolynomial(poly.mulMonomial(this));
        return polynomial;
    }

    public boolean isNumber() {
        return (powerX.equals(BigInteger.ZERO) && expPolynomial.isEmpty());
    }
}
