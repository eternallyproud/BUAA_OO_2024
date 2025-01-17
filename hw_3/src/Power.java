public class Power implements Factor {
    private char variable;
    private int power;

    public Power(char var, int pow) {
        variable = var;
        power = pow;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        polynomial.addMonomial(new Monomial(power));
        return polynomial;
    }
}
