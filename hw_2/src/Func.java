public class Func implements Factor {
    private Expr expr;

    public Func(String expression) {
        Lexer lexer = new Lexer(expression);
        Parser parser = new Parser(lexer);
        expr = parser.parseExpr();
    }

    @Override
    public Polynomial toPolynomial() {
        return expr.toPolynomial();
    }
}
