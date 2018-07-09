package net.dublin.bus.backend.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteRouteDataSource {
    private static final String NAMESPACE = "http://dublinbus.ie/";
    private static final String API_URL_BASE = "http://rtpi.dublinbus.ie/";
    private static final String API_URL_BASE_SERVICE = "http://rtpi.dublinbus.ie/DublinBusRTPIService.asmx?op=";
    private static final String API_URL_STOP_METHOD = "GetAllDestinations";
    private static final String API_URL_ROUTE_BY_STOP_METHOD = "GetRoutesServicedByStopNumber";
    private static final String API_URL_ROUTE_METHOD = "GetRoutesIncNiteLink_MobileFareCalc";
    private static final String API_URL_ROUTE_TIMETABLES = "http://www.dublinbus.ie/Your-Journey1/Timetables/";

    private List<Route> getData() throws Exception {
        SoapObject soapObject = new SoapObject(NAMESPACE, API_URL_ROUTE_METHOD);
        soapObject.addProperty("filter", "");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;

        envelope.setOutputSoapObject(soapObject);
        HttpTransportSE httpTransportSE = new HttpTransportSE(API_URL_BASE_SERVICE + API_URL_ROUTE_METHOD);

        httpTransportSE.call(NAMESPACE + API_URL_ROUTE_METHOD, envelope);

        SoapObject soapPrimitive = (SoapObject) envelope.getResponse();
        SoapObject result = (SoapObject) soapPrimitive.getProperty(0);
        List<Route> list = new ArrayList<>();
        int i = result.getPropertyCount();

        for (int j = 0; j < i; j++) {
            SoapObject o = (SoapObject) result.getProperty(j);
            Route route = new Route();
            route.setNumber(o.getProperty("Number").toString().toLowerCase());
            route.setInboundTowards(o.getProperty("InboundTowards").toString().replace("anyType{}", ""));
            route.setOutboundTowards(o.getProperty("OutboundTowards").toString().replace("anyType{}", ""));

            list.add(route);
        }

        return list;
    }

    public final List<Route> getRoutes() throws Exception {
        List<Route> routesList = getData();
        Document doc = Jsoup.connect(API_URL_ROUTE_TIMETABLES).get();
        Elements elements = doc.select("td.RouteNumberColumn");
        Map<String, String> map = new HashMap<>();
        routesList.sort(new RouteComparator());

        for (Element e : elements) {
            Elements link = e.select("a");
            String code = link.attr("href");
            code = code.replace("/Your-Journey1/Timetables/All-Timetables/", "");
            code = code.substring(0, code.length() - 1);

            String number = link.text().trim();
            number = number.replace("/", "");
            map.put(number, code);
        }

        for (Route route : routesList) {
            String code = map.get(route.getNumber());
            if (code != null) {
                route.setCode(code);
            } else {
                route.setCode("");
            }

            //System.out.println(route.getNumber() + " " + (code != null ? code : ""));
        }

        return routesList;
    }
}
