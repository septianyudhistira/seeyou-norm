package com.seeyou.dieselpoint.norm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class SqlInsertToMap {
    public static String parseValues(String queryInsert) {
        String validSql = "";

        try {
            final String       query       = queryInsert.replaceAll("  ", " ").trim();
            final StringBuffer sb          = new StringBuffer();
            final String       valuesName  = ") VALUES (";
            final int          valuesIndex = query.indexOf(valuesName);
            final String       insert1     = query.substring(query.indexOf("(") + 1, valuesIndex);
            final String       insert2     = query.substring(valuesIndex + valuesName.length(), query.length() - 1);
            sb.append(query.substring(0, query.indexOf("("))).append(" ");

            final List<String> replaceSubQuery1 = getSelectSql(insert1);
            String             query1           = insert1;

            for (String replace : replaceSubQuery1) {
                query1 = query1.replace(replace, "");
            }

            sb.append("(");
            sb.append(query1);

            final List<String> replaceSubQuery2 = getSelectSql(insert2);
            String             query2           = insert2;

            for (String replace : replaceSubQuery2) {
                query2 = query2.replace(replace, "");
            }

            sb.append(valuesName);
            sb.append(query2).append(")");

            validSql = sb.toString();
        } catch (Exception e) {
        }

        return validSql;
    }

    public static List<String> getSelectSql(final String query) {
        final int          length   = query.length();
        final List<String> replaces = new ArrayList<String>();
        int                start    = 0;
        int                end      = 0;

        for (int i = 0; i < length; i++) {
            if (query.charAt(i) == '(') {
                start = i;
            }

            if (query.charAt(i) == ')') {
                end = i;

                final String substring = query.substring(start, end + 1);

                //
                if (substring.contains("SELECT")) {
                    replaces.add(substring);
                }

                start = i + 1;
            }
        }

        return replaces;
    }

    public static Map<String, String> parseSqlInsert(final String query1) {
        Map<String, String> map   = new HashMap<String, String>();
        String              query = parseValues(query1);

        if ((query == null) || query.isEmpty()) {
            return map;
        }

        Pattern pattern = Pattern.compile("\\((.*?)\\)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(query);
        List<String[]> tokens  = new ArrayList<String[]>();

        while (matcher.find()) {
            tokens.add(matcher.group().replace("(", "").replace(")", "").split(","));
        }

        if ((tokens.size() == 2) && (tokens.get(0).length == tokens.get(1).length)) {
            for (int x = 0; x < tokens.get(0).length; x++) {
                map.put(tokens.get(0)[x].trim(), tokens.get(1)[x].replace("'", "").trim());
            }
        }

        return map;
    }

    public static void indexs(String query, final String guess, final List<Map<Integer, String>> list) {
        int index = query.indexOf(guess);

        while (index >= 0) {
            Map<Integer, String> map = new HashMap<Integer, String>();
            map.put(index, guess);
            list.add(map);
            index = query.indexOf(guess, index + 1);
        }
    }

    public static int getIndex(String str, String substring) {
        return Arrays.asList(str.split("\\s+")).indexOf(substring) + 1;
    }

    public static int[] getStringPositions(String string, String subString) {
        final String[] splitString = string.split(subString); //Split the string
        int            totalLen    = 0; //Total length of the string, added in the loop
        int[]          indexValues = new int[splitString.length - 1]; //Instances of subString in string

        //Loop through the splitString array to get each position
        for (int i = 0; i < (splitString.length - 1); i++) {
            if (i == 0) {
                //Only add the first splitString length because subString is cut here.
                totalLen = totalLen + splitString[i].length();
            } else {
                //Add totalLen & splitString len and subStringLen
                totalLen = totalLen + splitString[i].length() + subString.length();
            }

            indexValues[i] = totalLen; //Set indexValue at current i
        }

        return indexValues;
    }
}
