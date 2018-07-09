package net.dublin.bus.backend.data;

import java.util.Comparator;

public class RouteComparator implements Comparator<Route> {
    public int compare(Route paramRoute1, Route paramRoute2) {
        return Integer.valueOf(GetNumbersFromString(paramRoute1.getNumber())).compareTo(Integer.valueOf(GetNumbersFromString(paramRoute2.getNumber())));
    }

    private static String GetNumbersFromString(String paramString) {
        String str = "";
        for (int i = 0; i < paramString.length(); i++) {
            if (Character.isDigit(paramString.charAt(i))) {
                str = str + paramString.charAt(i);
            }
        }
        return str;
    }
}
