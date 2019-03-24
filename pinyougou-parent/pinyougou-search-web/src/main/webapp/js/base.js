var app=angular.module('pinyougou',[]);
//设置过滤器
app.filter('trustHtml',['$sce',function($sce){
   //传入前的内容
    return function (data) {
        //过滤后的内容
        return $sce.trustAsHtml(data)
    }
}]);
