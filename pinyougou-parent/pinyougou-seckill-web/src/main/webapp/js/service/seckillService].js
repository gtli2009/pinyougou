app.service('seckillGoodsService',function($http){
      //读取参加秒杀的商品
        this.findList=function(){
            return $http.get('seckillGoods/findList.do');
        }
        //从缓存中读取商品详情
        this.findOne=function (id) {
            return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
        }

    //提交订单
    this.submitOrder=function(seckillId){
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }
});