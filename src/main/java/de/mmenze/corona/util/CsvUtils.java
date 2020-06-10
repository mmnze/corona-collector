package de.mmenze.corona.util;

import org.apache.commons.csv.CSVRecord;

public class CsvUtils {

    private CsvUtils() {
    }


    public static double getDoubleFrom(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double getDoubleWithDefaultIfMissingFrom(CSVRecord record, String s) {
        try {
            return CsvUtils.getDoubleFrom(record.get(s));
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static int getIntegerFrom(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getIntegerWithDefaultIfMissingFrom(CSVRecord record, String s) {
        try {
            return CsvUtils.getIntegerFrom(record.get(s));
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static String getStringWithDefaultIfMissingFrom(CSVRecord record, String s) {
        try {
            return record.get(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
