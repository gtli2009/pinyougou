package com.pinyougou.pay.controller;

import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
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
    private OrderService orderService;

    @RequestMapping("/creteNative")
    public Map creteNative() {
        IdWorker idWorker = new IdWorker();
        //获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //获取支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);

        if (payLog != null) {
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
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
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));

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
                break;
            }

        }
        return result;
    }
}
