package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }

        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void submit(Long seckillId, String userId) {
        //1.从缓存中获取商品信息
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (seckillGoods.getStockCount() == 0) {
            throw new RuntimeException("商品已被抢光");
        }
        //2.减少库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //存入缓存
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
        //更新数据库
        if (seckillGoods.getStockCount() == 0) {
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            //清楚缓存
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }
        //3.存储订单
        long orderId = idWorker.nextId();
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(orderId);
        seckillOrder.setCreateTime(new Date());
        //秒杀价格
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        //设置用户ID
        seckillOrder.setUserId(userId);
        //支付状态
        seckillOrder.setStatus("0");

        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
        System.out.println("保存订单成功");

    }

    @Override
    public TbSeckillOrder seachOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //1.从缓存中提取订单
        TbSeckillOrder seckillOrder = seachOrderFromRedisByUserId(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("不存在该订单");
        }
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单号不符");
        }
        //2.修改订单数据
        //支付日期
        seckillOrder.setPayTime(new Date());
        //支付流水号
        seckillOrder.setTransactionId(transactionId);
        //支付状态
        seckillOrder.setStatus("1");
        //3.保存至数据库
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
        //4.清除缓存中的订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }

    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1.删除缓存中的订单
        TbSeckillOrder seckillOrder = seachOrderFromRedisByUserId(userId);
        if(seckillOrder!=null){
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
        //从缓存中提取订单
        TbSeckillGoods seckillGoods = (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
        //2.库存回退
        if(seckillGoods!=null){
            //库存加1
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
            //保存
            redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
        }else{
            //已经清空商品，创建商品后库存加1
            seckillGoods=new TbSeckillGoods();
            seckillGoods.setId(seckillOrder.getSeckillId());
            seckillGoods.setStockCount(1);
            redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
        }
        System.out.println("订单取消"+orderId);
    }

}
