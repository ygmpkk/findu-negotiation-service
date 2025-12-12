package com.findu.negotiation.infrastructure.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceParser {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    /**
     * 解析价格字符串为整数（分）
     * 示例：
     * "100元/小时" -> 10000
     * "50" -> 5000
     * "99.9元" -> 9990
     *
     * @param priceStr 价格字符串
     * @return 价格（分），解析失败返回0
     */
    public static int parseToCents(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0;
        }

        try {
            Matcher matcher = PRICE_PATTERN.matcher(priceStr);
            if (matcher.find()) {
                String numberStr = matcher.group(1);
                double price = Double.parseDouble(numberStr);
                return (int) Math.round(price * 100);
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        return 0;
    }
}
