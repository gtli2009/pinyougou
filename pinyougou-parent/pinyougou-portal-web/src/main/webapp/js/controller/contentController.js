app.controller('contentController',function($scope,contentService){
	
 	//广告列表
	var contentList=[];
				//根据广告分类Id查询广告
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
				function(response){
					$scope.contentList[categoryId]=response;
				}
		);
	}

	//页面跳转
	$scope.search =function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}


});