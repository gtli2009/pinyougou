package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        //得到购物车数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        //订单id集合
        List<String> orderList = new ArrayList<>();
        double total_money = 0;
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            System.out.println("sellerId:" + cart.getSellerId());
            //新创建订单对象
            TbOrder tborder = new TbOrder();
            tborder.setOrderId(orderId);
            //用户名
            tborder.setUserId(order.getUserId());
            //支付类型
            tborder.setPaymentType(order.getPaymentType());
            //状态：未付款
            tborder.setStatus("1");
            //订单创建日期
            tborder.setCreateTime(new Date());
            //订单更新日期
            tborder.setUpdateTime(new Date());
            //地址
            tborder.setReceiverAreaName(order.getReceiverAreaName());
            //手机号
            tborder.setReceiverMobile(order.getReceiverMobile());
            //收货人
            tborder.setReceiver(order.getReceiver());
            //订单来源
            tborder.setSourceType(order.getSourceType());
            //商家ID
            tborder.setSellerId(cart.getSellerId());
            //循环购物车明细
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());
                //订单ID
                orderItem.setOrderId(orderId);
                orderItem.setSellerId(cart.getSellerId());
                //金额累加
                money += orderItem.getTotalFee().doubleValue();
                orderItemMapper.insert(orderItem);
            }
            tborder.setPayment(new BigDecimal(money));
            orderMapper.insert(tborder);
            orderList.add(orderId + "");
            total_money += money;
        }

        //添加支付日志
        if ("1".equals(order.getPaymentType())) {
            TbPayLog tbPayLog = new TbPayLog();
            //支付订单号
            tbPayLog.setOutTradeNo(idWorker.nextId() + "");
            //创建时间
            tbPayLog.setCreateTime(new Date());
            //用户id
            tbPayLog.setUserId(order.getUserId());
            //订单id串
            tbPayLog.setOrderList(orderList.toString().replace("[", "").replace("]", ""));
            //金额 分
            tbPayLog.setTotalFee((long) total_money * 100);
            //交易状态
            tbPayLog.setTradeState("0");
            //微信支付
            tbPayLog.setPayType("1");
            payLogMapper.insert(tbPayLog);
            //放入缓存
            redisTemplate.boundHashOps("paylog").put(order.getUserId(), tbPayLog);
        }
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("logPay").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1.修改支付日志的支付状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        //支付时间
        payLog.setPayTime(new Date());
        //支付状态
        payLog.setPayType("1");
        //微信支付流水号
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);
        //2.修改订单状态
        String orderList = payLog.getOrderList();
        String[] orderIds = orderList.split(",");
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            //修改支付状态
            tbOrder.setStatus("2");
            //支付时间
            tbOrder.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKey(tbOrder);
        }
        //3.删除缓存中的paylog
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

    }

}
