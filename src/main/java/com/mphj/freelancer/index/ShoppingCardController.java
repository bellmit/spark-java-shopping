package com.mphj.freelancer.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.mphj.freelancer.repository.CategoryDao;
import com.mphj.freelancer.repository.ProductDao;
import com.mphj.freelancer.repository.models.Product;
import com.mphj.freelancer.repository.models.ShoppingCardObject;
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
        ;
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

}
