package modemmon.util;

public class NumberFormatter {

    private static final String[] POSTFIX = new String[]{" ", "K", "M", "B", "T", "Q", "E"};

    public static String format(long number) {

        if (number < 1_000) {
            return String.valueOf(number).concat(POSTFIX[0]);
        }

        String digits = String.valueOf(number);
        int length = digits.length();
        int groups = length / 3;
        int ungrouped = length % 3;

        if (ungrouped == 0) {
            groups--;
            ungrouped = 3;
        }

        String leftDigits = digits.substring(0, ungrouped);
        String rightDigits = (leftDigits.length() == 1) ? ".".concat(digits.substring(ungrouped, ungrouped + 1)) : "";
        return leftDigits.concat(rightDigits) + POSTFIX[groups];
    }
//
//    public static void main(String[] args) {
//
//        System.out.printf("%,d%n%n", Long.MAX_VALUE);
//        for (long i = 1; i <= Long.MAX_VALUE && i > 0; i += (i / 2) + 1) {
//            System.out.printf("| %,36d | = | %5s|%n", i, format(i));
//        }
//
//    }

}
