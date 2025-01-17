import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomFunction {
    private static HashMap<String, String> functions = new HashMap<>();
    private static HashMap<String, Integer> variableNum = new HashMap<>();
    private static final String[] args = new String[]{"!", "#", "%"};
    private static final String mark = "~";

    public static void addFunction(String input) {
        Pattern pattern = Pattern.compile("^([f-h])\\(([x-z]),?([x-z])?,?([x-z])?\\)=(.+)$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            int size = 0;
            final String funcName = matcher.group(1);
            String function = matcher.group(5);
            function = function.replaceAll("exp", mark);
            for (int i = 2; i < 5; i++) {
                if (matcher.group(i) != null) {
                    function = function.replaceAll(matcher.group(i), args[i - 2]);
                    size++;
                }
            }
            function = function.replaceAll(mark, "exp");
            functions.put(funcName, function);
            variableNum.put(funcName, size);
        }
    }

    public static String callFunction(String funcName, String para) { //para = "(x),(exp(x)),(3+5)"
        String function = functions.get(funcName);
        int paraNum = variableNum.get(funcName);
        String s = para.replaceAll(mark, ")" + mark + "(");
        String[] paras = s.split(mark);
        for (int i = 0; i < paraNum; i++) {
            function = function.replaceAll(args[i], paras[i]);
        }
        return function;
    }

    public static String getMark() {
        return mark;
    }

}
