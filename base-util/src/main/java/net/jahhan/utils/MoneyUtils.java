package net.jahhan.utils;

import java.text.DecimalFormat;

/**
 * @author nince
 */
public class MoneyUtils {

    public static String long2string(Long price) {
        String priceString = "0";
        DecimalFormat df = new DecimalFormat("#0.00");
        // priceString = Double.toString(price * 0.1 / 100);
        priceString = df.format(price * 0.1 / 100);
        return priceString;
    }

    public static String double2string(Double price) {
        String priceString = "0";
        DecimalFormat df = new DecimalFormat("#0.00");
        // priceString = Double.toString(price * 0.1 / 100);
        priceString = df.format(price * 0.1 / 100);
        return priceString;
    }

    public static String int2string(int price) {
        String priceString = "0";
        DecimalFormat df = new DecimalFormat("#0.00");
        priceString = df.format(price * 0.1 / 100);
        return priceString;
    }

    public static void main(String[] args) {
        Long price = 1789L;
        DecimalFormat df = new DecimalFormat("#.00000");
        // priceString = Double.toString(price * 0.1 / 100);
        String priceString = df.format(price * 0.1 / 100);
        System.out.println(priceString);
    }
}