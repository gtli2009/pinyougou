package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

	/**
	 * 搜索
	 * 
	 * @param searchMap
	 * @return
	 */
	Map find(Map searchMap);

	/**
	 * 批量导入solr
	 */
	void importList(List list);

	/**
	 * 删除商家上架的商品（spuId）
	 * @param goodsIds
	 */
	void deleteByGoodsIds(List goodsIds);

}
