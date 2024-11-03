package com.seeyou.dieselpoint.norm;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class ParsingSql {
    public static String getValueSql(final String rawQuery, final String key) {
        final String   replace     = rawQuery.replaceAll("  ", " ").replaceAll("WHERE", "\nWHERE");
        final String[] split       = replace.split("WHERE");
        String         returnValue = "";

        for (String string : split) {
            final String key1 = " " + key;
            final String key2 = "." + key;

            if (string.contains(key1)) {
                String val = "";

                try {
                    val = string.substring(string.indexOf(key1));
                } catch (Exception e) {
                }

                returnValue = val.replace(" ", "");

                break;
            }

            if (string.contains(key2)) {
                String val = "";

                try {
                    val = string.substring(string.indexOf(key2));
                } catch (Exception e) {
                }

                returnValue = val.replace(".", "");

                break;
            }
        }

        return returnValue;
    }
}
