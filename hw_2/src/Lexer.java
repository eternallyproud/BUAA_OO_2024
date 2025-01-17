public class Lexer {
    private final String input;
    private int position;
    private String curToken;

    public Lexer(String input) {
        this.input = input;
        position = 0;//'+' or '-'?
        this.next();
    }

    public void next() {
        if (position == input.length()) {
            return;
        }
        char c = input.charAt(position);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if ("()*+-,".indexOf(c) != -1) { //c is in "()*+-"
            position++;
            curToken = String.valueOf(c); //"12345" BigInteger
        } else if (c == '^') {
            position++;
            if (input.charAt(position) == '+') {
                position++;
            }
            curToken = "^" + getNumber();//"^114514" int
        } else if (c == 'x') {
            curToken = getVariable(); //"x^777" int
        } else if (c == 'e') {
            curToken = "exp";
            position += 3;
        } else if ("fgh".indexOf(c) != -1) {
            curToken = String.valueOf(c);
            position++;
        } else {
            System.out.print("your Lexer is wrong!");//bug
        }
    }

    private String getNumber() { //get a number and stop at a none-digit position
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            sb.append(input.charAt(position));
            ++position;
        }
        return sb.toString();
    }

    private String getVariable() {
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && "()*+-,".indexOf(input.charAt(position)) == -1) {
            sb.append(input.charAt(position));
            ++position;
        }
        if (sb.toString().equals("x^")) {
            position++;
            sb.append(getNumber());
        }
        return sb.toString();
    }

    public String peek() {
        return curToken;
    }
}
