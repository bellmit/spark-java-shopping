package com.mphj.freelancer;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mphj.freelancer.index.IndexController;
import com.mphj.freelancer.index.admin.AdminCategoryController;
import com.mphj.freelancer.mocks.MockedRLocalCache;
import com.mphj.freelancer.utils.AppProperties;
import com.mphj.freelancer.utils.Cache;
import com.mphj.freelancer.utils.HibernateUtils;
import com.mphj.freelancer.utils.Redis;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {
        initRedis();
        initDB();
        final int portNumber = (args.length >= 1) ? Integer.parseInt(args[0]) : 8090;
        port(portNumber);
        staticFiles.location("/public");
        get("/", (req, resp) -> IndexController.index(req, resp));
        get("/admin/categories", (req, resp) -> AdminCategoryController.index(req, resp));
        post("/admin/categories", (req, resp) -> AdminCategoryController.modify(req, resp));
        delete("/admin/categories", (req, resp) -> AdminCategoryController.delete(req, resp));
    }



    public static void initRedis() {
        if (AppProperties.isRedisEnabled()) {
            Redis.init();
            Cache.init(Redis.getInstance().getLocalCachedMap("any", Cache.options()));
        } else {
            Cache.init(new MockedRLocalCache());
        }
    }

    public static void initDB() {
        HibernateUtils.getSessionFactory().getCurrentSession();
    }

}