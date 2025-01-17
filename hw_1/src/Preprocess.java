public class Preprocess {
    public String simplify(String input) {
        String result = input.replaceAll("[ \t]", "");
        while (!result.equals(result.replaceAll("\\+-", "-"))) {
            result = result.replaceAll("\\+-", "-");
        }
        while (!result.equals(result.replaceAll("---", "-"))) {
            result = result.replaceAll("---", "-");
        }
        while (!result.equals(result.replaceAll("--", "+"))) {
            result = result.replaceAll("--", "+");
        }
        while (!result.equals(result.replaceAll("\\+\\+", "+"))) {
            result = result.replaceAll("\\+\\+", "+");
        }
        result = result.replaceAll("-\\+", "-");
        return result;
    }
}
