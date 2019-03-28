package com.pinyougou.pay.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.数据封装
        Map param = new HashMap();
        //公众账号ID
        param.put("appid", appid);
        //商户号
        param.put("partner", partner);
        //随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body", "品优购");
        //商户订单号 out_trade_no
        param.put("out_trade_no", out_trade_no);
        //标价金额 total_fee
        param.put("total_fee", total_fee);
        //终端IP spbill_create_ip
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "http://www.itcast.com");
        //交易类型 trade_type
        param.put("trade_type", "NATIVE");
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();

            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            Map map = new HashMap();
            map.put("code_url", mapResult.get("code_url"));
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
    return pay(out_trade_no,"https://api.mch.weixin.qq.com/pay/orderquery");
    }

    @Override
    public Map closePay(String out_trade_no) {
     return pay(out_trade_no,"https://api.mch.weixin.qq.com/pay/closeorder");
    }

    private Map pay(String out_trade_no, String Url){
        Map param = new HashMap();
        //公众账号ID
        param.put("appid", appid);
        //商户号
        param.put("mch_id", partner);
        //订单号
        param.put("out_trade_no", out_trade_no);
        //随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        String url =Url;
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}


