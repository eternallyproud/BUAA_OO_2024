public class Exponent implements Factor {
    private Factor factor;
    private int power;

    public Exponent(Factor factor) {
        this.factor = factor;
        power = 1;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        polynomial.addMonomial(new Monomial(factor.toPolynomial(), power));
        return polynomial;
    }
}
