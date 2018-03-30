package com.mphj.freelancer.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.mphj.freelancer.repository.*;
import com.mphj.freelancer.repository.models.*;
import com.mphj.freelancer.utils.GatewayHelper;
import com.mphj.freelancer.utils.HibernateUtils;
import com.mphj.freelancer.utils.Path;
import com.mphj.freelancer.utils.ViewUtils;
import spark.Request;
import spark.Response;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCardController {


    public static String view(Request request, Response response) {

        String jsonData = null;
        try {
            jsonData = URLDecoder.decode(request.cookie("data_sc"));
        } catch (Exception e) {

        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ShoppingCardObject> shoppingCardObjects = new HashMap<>();

        try {
            shoppingCardObjects = objectMapper.readValue(jsonData, new TypeReference<Map<String, ShoppingCardObject>>() {
            });
        } catch (Exception e) {

        }

        ProductDao productDao = new ProductDao(HibernateUtils.getSessionFactory());
        List<Product> products = new ArrayList<>();
        for (ShoppingCardObject shoppingCardObject : shoppingCardObjects.values()) {
            if (shoppingCardObject.getCount() <= 0)
                continue;
            Product product = productDao.findById(shoppingCardObject.getId());
            if (product.getMainImage() == null) {
                product.setMainImage("/image/test.jpg");
            }
            products.add(product);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("products", products);

        CategoryDao categoryDao = new CategoryDao(HibernateUtils.getSessionFactory());
        map.put("cats", categoryDao.getAll());

        response.type(MediaType.HTML_UTF_8.toString());
        String body = ViewUtils.render(Path.Template.SHOPPING_CARD, map);
        return body;
    }


    public static String newShoppingCard(Request request, Response response) {
        String token = request.cookie("token");
        if (token == null || token.trim().isEmpty()) {
            response.redirect("/shc?not_user_err=1");
            return null;
        }

        UserDao userDao = new UserDao(HibernateUtils.getSessionFactory());
        User user = null;
        try {
           user = userDao.findByToken(token);
        } catch (Exception e) {
            // Bad token
        }
        if (user == null) {
            response.redirect("/shc?bad_token=1");
            return null;
        }

        String email = request.queryParams("email");

        String address = request.queryParams("address");

        if (address == null || address.trim().isEmpty()) {
            response.redirect("/shc?bad_address=1");
            return null;
        }

        String description = request.queryParams("description");

        String name = request.queryParams("name");

        int totalPrice = 0;

        ProductPriceDao productPriceDao = new ProductPriceDao(HibernateUtils.getSessionFactory());

        String jsonData = null;
        try {
            jsonData = URLDecoder.decode(request.cookie("data_sc"));
        } catch (Exception e) {

        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ShoppingCardObject> shoppingCardObjects = new HashMap<>();

        try {
            shoppingCardObjects = objectMapper.readValue(jsonData, new TypeReference<Map<String, ShoppingCardObject>>() {
            });
        } catch (Exception e) {

        }

        if (shoppingCardObjects.size() == 0) {
            response.redirect("/shc?empty_card=1");
            return null;
        }

        for (ShoppingCardObject shpObj : shoppingCardObjects.values()) {
            ProductPrice productPrice = productPriceDao.findLatestByProductId(shpObj.getId());
            totalPrice += productPrice.getActualPrice();
        }

        ShoppingCardDao shoppingCardDao = new ShoppingCardDao(HibernateUtils.getSessionFactory());

        ShoppingCardProductDao shoppingCardProductDao = new ShoppingCardProductDao(HibernateUtils.getSessionFactory());

        ShoppingCard shoppingCard = new ShoppingCard();
        shoppingCard.setAddress(address);
        shoppingCard.setDescription(description);
        shoppingCard.setUserId(user.getId());
        shoppingCard.setCreatedAt(System.currentTimeMillis() / 1000l);
        shoppingCard.setEmail(email);
        shoppingCard.setUserName(name);
        shoppingCard.setPrice(totalPrice);

        shoppingCardDao.save(shoppingCard);

        try {
            shoppingCard.setToken(GatewayHelper.newSession(totalPrice, shoppingCard.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        shoppingCardDao.save(shoppingCard);

        List<ShoppingCardProduct> shoppingCardProducts = new ArrayList<>();

        for (ShoppingCardObject shpObj : shoppingCardObjects.values()) {
            ShoppingCardProduct shoppingCardProduct = new ShoppingCardProduct();
            shoppingCardProduct.setCardId(shoppingCard.getId());
            shoppingCardProduct.setCount(shpObj.getCount());
            shoppingCardProduct.setProductId(shpObj.getId());
            shoppingCardProducts.add(shoppingCardProduct);
        }

        shoppingCardProductDao.save(shoppingCardProducts);
        response.redirect(GatewayHelper.gatewayUrl(shoppingCard.getToken()));
        return null;
    }

}
