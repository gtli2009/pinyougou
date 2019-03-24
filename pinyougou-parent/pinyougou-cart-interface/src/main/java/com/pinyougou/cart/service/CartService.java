package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品至购物车
     *
     * @param cartList 购物车集合
     * @param itemId   商品id
     * @param num      商品数量
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据用户名从redis中提取购物车列表
     *
     * @param username
     * @return
     */
    List<Cart> findCartListFromRedis(String username);

    /**
     * 把购物车存储到redis中
     *
     * @param username
     * @param cartList
     */
    void saveCartListToRedis(String username, List<Cart> cartList);

    /**
     * 合并购物车
     *
     * @param cartList1
     * @param cartList2
     * @return
     */
    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
