package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 生成二维码
     * @param out_trade_no 商户订单号
     * @param total_fee 标价金额
     * @return
     */
    Map createNative (String out_trade_no,String total_fee);

    /**
     * 查询订单
     * @param out_trade_no
     * @return
     */
   Map queryPayStatus(String out_trade_no);

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    Map closePay(String out_trade_no);

}
