package net.dublin.bus.backend.servlet;

import net.dublin.bus.backend.common.Storage;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LastUpdateServlet", value = "/lastUpdate")
public class LastUpdateServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long responseStr = 0L;

        try {
            responseStr = new Storage().returnLastDate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain");
        response.getWriter().println(responseStr);
    }
}