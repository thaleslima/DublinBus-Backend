package net.dublin.bus.backend.servlet;

import net.dublin.bus.backend.common.Storage;
import net.dublin.bus.backend.data.RemoteRouteDataSource;
import net.dublin.bus.backend.data.Route;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "RoutesServlet", value = "/routes")
public class RoutesServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseStr = "ok";
        Storage storage = new Storage();
        String nameFile = "routes.gz";
        Entity entity = null;

        try {
            entity = storage.saveLog(nameFile);
            RemoteRouteDataSource dataSource = new RemoteRouteDataSource();
            List<Route> routes = dataSource.getRoutes();
            boolean update = storage.saveData(routes, nameFile);
            storage.saveLogSuccess(entity, update);

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