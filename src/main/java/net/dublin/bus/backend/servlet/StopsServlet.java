package net.dublin.bus.backend.servlet;

import net.dublin.bus.backend.common.Storage;
import net.dublin.bus.backend.data.RemoteStopDataSource;
import net.dublin.bus.backend.data.RouteStop;
import net.dublin.bus.backend.data.Stop;
import com.google.appengine.api.datastore.Entity;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "StopsServlet", value = "/stops")
public class StopsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseStr = "ok";
        Storage storage = new Storage();
        String nameFile = "stops.gz";
        Entity entity = null;

        try {
            entity = storage.saveLog(nameFile);
            RemoteStopDataSource dataSource = new RemoteStopDataSource();
            List<Stop> stops = dataSource.getStops();
            List<RouteStop> routeStops = dataSource.getRouteStops(stops);

            boolean update = storage.saveData(stops, nameFile);
            boolean update2 = storage.saveData(routeStops, "routeStops.gz");

            storage.saveLogSuccess(entity, update || update2);
        } catch (Exception e) {
            e.printStackTrace();
            responseStr = e.getLocalizedMessage();
            response.setStatus(500);
            storage.saveLogError(entity, e.toString());
        }

        response.setContentType("text/plain");
        response.getWriter().println(responseStr);
    }
}