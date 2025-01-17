import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Preprocess preprocess = new Preprocess();
        Lexer lexer = new Lexer(preprocess.simplify(input));
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Polynomial polynomial = expr.toPolynomial();
        System.out.print(polynomial.polyToString());//change the polynomial -> use carefully
    }
}
