import java.util.ArrayList;

public class Expr implements Factor {
    private ArrayList<Term> termList; //sign is in different terms
    private int exponent;

    public Expr() {
        termList = new ArrayList<>();
        exponent = 1;
    }

    public void addTerm(Term term) {
        termList.add(term);
    }

    public void setExp(int exp) {
        exponent = exp;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial polynomial = new Polynomial();
        for (Term term : termList) {
            polynomial.addPolynomial(term.toPolynomial());
        }
        return polynomial.powPolynomial(exponent);
    }
}
