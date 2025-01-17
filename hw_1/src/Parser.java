import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm(getSign()));
        while (lexer.peek().charAt(0) == '+' || lexer.peek().charAt(0) == '-') {
            //(Objects.equals(lexer.peek(), "+") || Objects.equals(lexer.peek(), "+"))
            expr.addTerm(parseTerm(getSign()));
        }
        return expr;
    }

    public Term parseTerm(int sign) {
        Term term = new Term(sign);
        term.addFactor(parseFactor(getSign()));
        while (lexer.peek().charAt(0) == '*') {
            lexer.next();
            term.addFactor(parseFactor(getSign()));
        }
        return term;
    }

    private int getSign() { //if there is '+' or '-', then get it
        if (lexer.peek().charAt(0) == '-') {
            lexer.next();
            return -1;
        }
        if (lexer.peek().charAt(0) == '+') {
            lexer.next();
        }
        return 1;
    }

    public Factor parseFactor(int sign) {
        if (lexer.peek().charAt(0) == '(') { //expr
            lexer.next();
            Expr expr = parseExpr();
            lexer.next();
            if (lexer.peek().charAt(0) == '^') { //deal with exponent
                expr.setExp(Integer.parseInt(lexer.peek().substring(1)));
                lexer.next();
            }
            return expr;
        } else if (Character.isDigit(lexer.peek().charAt(0))) {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num, sign);
        } else {
            char var = lexer.peek().charAt(0);
            if (lexer.peek().length() > 1) {
                int exp = Integer.parseInt(lexer.peek().substring(2));
                lexer.next();
                return new Power(sign, var, exp);
            }
            lexer.next();
            return new Power(sign, var, 1);
        }
    }
}
