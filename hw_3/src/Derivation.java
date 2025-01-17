public class Derivation implements Factor {
    private Expr expr;

    public Derivation(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Polynomial toPolynomial() {
        return expr.toPolynomial().deprive();
    }
}
