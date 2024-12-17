package com.codegym.salesmanagement.formatter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {
    private static final Locale LOCALE_VIETNAM = new Locale("vi", "VN");

    public static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }
        NumberFormat formatter = NumberFormat.getInstance(LOCALE_VIETNAM);
        return formatter.format(price) + " ₫";
    }
}
