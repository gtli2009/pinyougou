package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 执行定时任务，刷新秒杀商品，增量更新
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("执行了增量更新任务调度" + new Date());
        //下旬所有的key
        List keys = (new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys()));


        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        //审核通过
        criteria.andStatusEqualTo("1");
        //剩余库存大于0
        criteria.andStockCountGreaterThan(0);
        //开始时间小于等于当前时间
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        //结束时间大于当前时间
        criteria.andEndTimeGreaterThan(new Date());

        if (keys.size() > 0) {
            //排除商品中已经存在的商品id集合
            criteria.andIdNotIn(keys);
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //将数据存入缓存
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            System.out.println("增量更新商品");
        }
    }

    /**
     * \
     * 执行定时任务每秒删除缓存中的商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        //查询出缓存中的数据，如果过期则删除
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGood : seckillGoods) {
                //过期时间小于当前时间，商品信息同步到数据库，并从缓存中删除商品,
            if (seckillGood.getEndTime().getTime() < System.currentTimeMillis()) {
                //同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                //过期时间小于当前时间，删除商品
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
            }
        }
    }


}