package com.pinyougou.page.service;

public interface ItemPageService {
    /**
     *生成商品详情页
     * @param
     * @return
     */
    boolean getItemHtml(Long goodsId);

    /**
     * 删除详情页
     * @param goodIds
     * @return
     */
    boolean deleteItemHtml(Long[] goodIds);


}
