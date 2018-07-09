package net.dublin.bus.backend.common;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.util.zip.GZIPOutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;

import com.google.appengine.api.datastore.Query;

public class Storage {
    private static final String KEY_CACHE_DATE = "key_cache_date";
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final GcsService gcsService = GcsServiceFactory.createGcsService(
            new RetryParams.Builder()
                    .initialRetryDelayMillis(10)
                    .retryMaxAttempts(10)
                    .totalRetryPeriodMillis(15000)
                    .build());

    public boolean saveData(Object src, String fileName) throws Exception {
        GcsFilename gcsFilename = new GcsFilename("dublin-bus", fileName);
        String sJson = new Gson().toJson(src);
        if (hasAlteration(sJson, fileName)) {
            writeDataToStorageJson(sJson, gcsFilename);
            updateDate();
            return true;
        }
        return false;
    }

    private void updateDate() {
        Entity employee = new Entity("Update", "1");
        long date = new Date().getTime();

        employee.setProperty("date", date);
        datastore.put(employee);

        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        syncCache.put(KEY_CACHE_DATE, date);
    }

    public Entity saveLog(String name) {
        Entity employee = new Entity("Log");
        employee.setProperty("description", name);
        employee.setProperty("dateInit", new Date().getTime());
        datastore.put(employee);
        return employee;
    }

    public void saveLogSuccess(Entity employee, boolean update) {
        if (employee == null) return;

        employee.setProperty("status", "success");
        employee.setProperty("dateFinal", new Date().getTime());
        employee.setProperty("hasUpdate", update);
        datastore.put(employee);
    }

    public void saveLogError(Entity employee, String exception) {
        if (employee == null) return;

        employee.setProperty("status", "error");
        employee.setProperty("dateFinal", new Date().getTime());
        employee.setProperty("exception", exception);
        datastore.put(employee);
    }

    public Long returnLastDate() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        Long lastDate = (Long) syncCache.get(KEY_CACHE_DATE);

        if (lastDate == null) {
            List<Entity> entitys = listUpdateEntities();
            if (entitys.size() == 0) return 0L;
            lastDate = (Long) entitys.get(0).getProperty("date");
            syncCache.put(KEY_CACHE_DATE, lastDate);
        }

        return lastDate;
    }

    private List<Entity> listUpdateEntities() {
        Query query = new Query("Update").addSort("date", Query.SortDirection.DESCENDING);
        return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
    }

    private boolean hasAlteration(String src, String fileName) throws Exception {
        String sJson = HttpUtil.openUrlGet("https://storage.googleapis.com/dublin-bus/" + fileName);
        System.out.println(sJson.equals(src));
        return !sJson.equals(src);
    }

    private void writeDataToStorageJson(String src, GcsFilename fileName) throws IOException {
        GcsFileOptions options = new GcsFileOptions.Builder()
                .acl("public-read")
                .cacheControl("public, max-age=3600, no-transform").build();

        GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, options);
        GZIPOutputStream zipOut = new GZIPOutputStream(Channels.newOutputStream(outputChannel));
        zipOut.write(src.getBytes("UTF-8"));
        zipOut.close();
    }
}