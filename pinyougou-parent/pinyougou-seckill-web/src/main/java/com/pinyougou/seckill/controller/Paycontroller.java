package com.pinyougou.seckill.controller;

import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class Paycontroller {
    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/creteNative")
    public Map creteNative() {
        IdWorker idWorker = new IdWorker();
        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.seachOrderFromRedisByUserId(username);

        if (seckillOrder != null) {
            //支付金额由元转成分，在再转成long类型，最后转成字符串
            return weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100) + "");
        } else {
            return new HashMap();
        }
    }

    /**
     * 循环查询支付状态，没3秒查询一次，查询100次后停止
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int i = 1;
        while (true) {
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            //失败
            if (map == null) {
                result = new Result(false, "支付发生错误");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))) {
                result = new Result(true, "支付成功");
                //支付成功，保存订单
                seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),(String)map.get("transaction_id"));
                break;
            }
            //循环休眠3秒
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
            if (i == 100) {
                result = new Result(false, "二维码超时");
                //1.调用微信的关闭订单接口
                Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
                //如果返回结果是正常关闭
                if( "FAIL".equals(payresult.get("result_code")) ){
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        //支付成功，保存订单
                        seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), (String)map.get("transaction_id"));
                    }
                }
                //超时，关闭订单
                if(result.isSuccess()==false){
                    System.out.println("超时，取消订单");
                    //2.调用删除
                    seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
                }
                break;
            }

        }
        return result;
    }
}
