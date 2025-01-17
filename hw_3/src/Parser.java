import java.math.BigInteger;
import java.util.Objects;

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
            lexer.next();                  //get rid of right bracket
            if (lexer.peek().charAt(0) == '^') { //deal with exponent
                expr.setPower(Integer.parseInt(lexer.peek().substring(1)));
                lexer.next();
            }
            return expr;
        } else if (Character.isDigit(lexer.peek().charAt(0))) {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num, sign);
        } else if (Objects.equals(lexer.peek(), "exp")) {
            lexer.next();
            lexer.next();
            Exponent exponent = new Exponent(parseFactor(getSign()));
            lexer.next();
            if (lexer.peek().charAt(0) == '^') {
                exponent.setPower(Integer.parseInt((lexer.peek().substring(1))));
                lexer.next();
            }
            return exponent;
        } else if ("fgh".indexOf(lexer.peek().charAt(0)) != -1) {
            String funcName = lexer.peek();
            lexer.next();//'('
            String para = this.getParameter();
            String expression = CustomFunction.callFunction(funcName, para);
            return new Func(expression);
        } else if (lexer.peek().charAt(0) == 'd') {
            lexer.next();
            lexer.next();
            Derivation derivation = new Derivation(parseExpr());
            lexer.next();
            return derivation;
        } else {
            char var = lexer.peek().charAt(0);
            if (lexer.peek().length() > 1) {
                int exp = Integer.parseInt(lexer.peek().substring(2));
                lexer.next();
                return new Power(var, exp);//no need for sign: *+x^7 is not valid input
            }
            lexer.next();
            return new Power(var, 1);
        }
    }

    public String getParameter() {
        StringBuilder sb = new StringBuilder().append("(");
        int bracketCount = 1;
        while (bracketCount != 0) {
            lexer.next();
            if (lexer.peek().charAt(0) == '(') {
                bracketCount++;
            }
            if (lexer.peek().charAt(0) == ')') {
                bracketCount--;
            }
            if ((lexer.peek().charAt(0) == ',') && bracketCount == 1) {
                sb.append(CustomFunction.getMark());
            } else {
                sb.append(lexer.peek());
            }
        }
        lexer.next();
        return sb.toString();
    }
}
