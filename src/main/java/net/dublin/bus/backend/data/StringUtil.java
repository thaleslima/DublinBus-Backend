package net.dublin.bus.backend.data;

import java.util.List;
import java.util.stream.Collectors;

public final class StringUtil {
    public static String convertListToString(List<Route> routes) {
        List<String> collect = routes.stream().map(Route::getNumber).collect(Collectors.toList());
        return String.join(", ", collect);
    }
}
