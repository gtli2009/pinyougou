package com.pinyougou.cart.controller;

import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/cart")
@RestController
public class CartController {
    @Autowired
    private HttpServletRequest request;
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletResponse response;

    /**
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);
        //从购物车中提取cookie
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || "".equals(cartListString)) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        //是否登录
        if ("anonymousUser".equals(username)) {
            return cartList_cookie;
        } else {
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //判断本地cookie中购物车数量大于0才合并
            if (cartList_cookie.size() > 0) {
                //合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //存入redis
                cartService.saveCartListToRedis(username, cartList);
                //清楚本地购物车
                CookieUtil.deleteCookie(request, response, "careList");
                return cartList;
            }
            return cartList_redis;
        }
    }


    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");


        try {
            //（1）从cookie中取出购物车
            List<Cart> cartList = findCartList();
            //（2）向购物车添加商品
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录人" + name);
            //是否登录
            if ("anonymousUser".equals(name)) {
                //（3）将购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);

                CookieUtil.setCookie(request, response, "cartList", cartListString, 24 * 3600, "UTF-8");
            } else {
                cartService.saveCartListToRedis(name, cartList);
            }
            return new Result(true, "存入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "存入购物车失败");
        }
    }

}
