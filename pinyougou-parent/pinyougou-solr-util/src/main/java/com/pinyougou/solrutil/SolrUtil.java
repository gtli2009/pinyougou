package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
import org.springframework.data.solr.core.query.Query;


@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData() {
        TbItemExample example = new TbItemExample();
        Criteria createCriteria = example.createCriteria();
        createCriteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem item : itemList) {
            System.out.println(item.getId() + " " + item.getTitle());
            // 从数据库提取json，转换成map
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(specMap);

        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("结束");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.deleteAll();
        System.out.println(solrUtil);
    }
    @Test
    public void deleteAll() {
    Query  query =new SimpleQuery("*:*");
     solrTemplate.delete(query);
     solrTemplate.commit();

    }


}
