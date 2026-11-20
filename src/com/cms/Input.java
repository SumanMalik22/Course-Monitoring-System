package com.cms;

import java.util.Scanner;

/** Small helpers for reading console input safely. */
public class Input {
    private static final Scanner SC = new Scanner(System.in);

    public static String text(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Value cannot be empty.");
        }
    }

    public static int integer(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(text(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    public static double decimal(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(text(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number.");
            }
        }
    }
}
