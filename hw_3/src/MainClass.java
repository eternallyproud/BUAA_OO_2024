public class MainClass {
    public static void main(String[] args) {
        InOut inOut = new InOut();
        inOut.getFunction();
        Lexer lexer = new Lexer(inOut.getExpression());
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Polynomial polynomial = expr.toPolynomial();
        polynomial.simplify();
        inOut.putExpression(polynomial.polyToString());//change the polynomial -> use carefully
    }
}
