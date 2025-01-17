import java.util.ArrayList;

public class Expr implements Factor {
    private ArrayList<Term> termList; //sign is in different terms
    private int power;

    public Expr() {
        termList = new ArrayList<>();
        power = 1;
    }

    public void addTerm(Term term) {
        termList.add(term);
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        for (Term term : termList) {
            polynomial.addPolynomial(term.toPolynomial());
        }
        return polynomial.powPolynomial(power);
    }
}
