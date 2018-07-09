package net.dublin.bus.backend.data;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class RemoteStopDataSource {
    private static final String NAMESPACE = "http://dublinbus.ie/";
    private static final String API_URL_BASE = "http://rtpi.dublinbus.ie/";
    private static final String API_URL_BASE_SERVICE = "http://rtpi.dublinbus.ie/DublinBusRTPIService.asmx?op=";
    private static final String API_URL_STOP_METHOD = "GetAllDestinations";
    private static final String API_URL_ROUTE_BY_STOP_METHOD = "GetRoutesServicedByStopNumber";

    public List<Stop> getStops() throws Exception {
        SoapObject soapObject = new SoapObject(NAMESPACE, API_URL_STOP_METHOD);
        soapObject.addProperty("filter", "");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);
        HttpTransportSE httpTransportSE = new HttpTransportSE(API_URL_BASE_SERVICE + API_URL_STOP_METHOD);
        httpTransportSE.call(NAMESPACE + API_URL_STOP_METHOD, envelope);

        SoapObject soapPrimitive = (SoapObject) envelope.getResponse();
        SoapObject result = (SoapObject) soapPrimitive.getProperty(0);

        List<Stop> list = new ArrayList<>();
        int i = result.getPropertyCount();

        for (int j = 0; j < i; j++) {
            SoapObject o = (SoapObject) result.getProperty(j);
            Stop stop = new Stop();

            stop.setStopnumber(o.getProperty("StopNumber").toString());

            Double lat = parseDouble(o.getProperty("Latitude").toString());
            stop.setLat(lat != null ? lat : 0.0D);

            lat = parseDouble(o.getProperty("Longitude").toString());
            stop.setLng(lat != null ? lat : 0.0D);

            stop.setDescription(o.getProperty("Description").toString());

            list.add(stop);
        }

        return list;
    }

    public List<RouteStop> getRouteStops(List<Stop> stops) {
        List<RouteStop> routeStops = new ArrayList<>();
        List<RouteStopThread> threads = new ArrayList<>();
        int rangeInitial = 0;
        int rangeFinal = 500;
        int range = 500;

        for (int i = 0; i < 10; i++) {
            threads.add(new RouteStopThread(rangeInitial, rangeFinal, stops));
            rangeInitial = rangeFinal;
            rangeFinal += range;
        }
        threads.get(threads.size() - 1).end = stops.size();

        for (RouteStopThread thread : threads) {
            thread.start();
        }

        try {
            for (RouteStopThread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (RouteStopThread thread : threads) {
            routeStops.addAll(thread.routeStops);
        }

        return routeStops;
    }

    private List<Route> getRoutesByStopNumber(String stopNumber) {
        int retryCounter = 0;
        int maxRetries = 4;
        List<Route> list = new ArrayList<>();

        while (true) {
            try {
                SoapObject soapObject = new SoapObject(NAMESPACE, API_URL_ROUTE_BY_STOP_METHOD);
                soapObject.addProperty("stopId", stopNumber);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(soapObject);
                HttpTransportSE httpTransportSE = new HttpTransportSE(API_URL_BASE_SERVICE + API_URL_ROUTE_BY_STOP_METHOD);
                httpTransportSE.call(NAMESPACE + API_URL_ROUTE_BY_STOP_METHOD, envelope);

                SoapObject soapPrimitive = (SoapObject) envelope.getResponse();
                int i = soapPrimitive.getPropertyCount();

                for (int j = 0; j < i; j++) {
                    SoapObject o = (SoapObject) soapPrimitive.getProperty(j);
                    Route route = new Route();

                    route.setNumber(o.getProperty("Number").toString());
                    list.add(route);
                }

                return list;
            } catch (Exception ex) {
                ++retryCounter;
                if (retryCounter >= maxRetries) {
                    return null;
                }
            }
        }
    }

    private Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static class RouteStopThread extends Thread {
        private int start;
        private int end;
        private List<Stop> list;
        private List<RouteStop> routeStops = new ArrayList<>();

        RouteStopThread(int start, int end, List<Stop> list) {
            this.start = start;
            this.end = end;
            this.list = list;
        }

        @Override
        public void run() {
            RemoteStopDataSource a = new RemoteStopDataSource();

            for (int i = start; i < end; i++) {
                //System.out.println(Thread.currentThread().getName() + " " + i);
                Stop stop = list.get(i);

                List<Route> routes = a.getRoutesByStopNumber(stop.getStopnumber());
                if (routes != null) {
                    routes.sort(new RouteComparator());

                    stop.setRoutes(StringUtil.convertListToString(routes));

                    for (Route route : routes) {
                        routeStops.add(new RouteStop(route.getNumber(), stop.getStopnumber()));
                    }
                }

                break;
            }
        }
    }
}
