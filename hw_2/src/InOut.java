import java.util.Scanner;

public class InOut {
    private final Scanner scanner = new Scanner(System.in);
    private final Preprocess preprocess = new Preprocess();

    public void getFunction() {
        int n = scanner.nextLine().charAt(0) - '0';
        for (int i = 0; i < n; i++) {
            CustomFunction.addFunction(preprocess.simplify(scanner.nextLine()));
        }
    }

    public String getExpression() {
        return preprocess.simplify(scanner.nextLine());
    }

    public void putExpression(String expression) {
        System.out.print(expression);
    }
}
