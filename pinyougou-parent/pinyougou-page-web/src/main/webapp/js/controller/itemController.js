//品牌控制层 
app.controller('itemController', function ($scope, $http) {
    //存储用户选择的规格
    $scope.specificationItems = {};
    //购买数量的加减
    $scope.addNum = function (x) {
        $scope.num += x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }
    //用户选择规格调用的方法
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
        //查询Sku
        searchSku();
    }
    //用户是否选中
    $scope.isSelected = function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        } else {
            return false;
        }
    }
    //当前选择的SKu
    $scope.sku = {};
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //匹配对象是否相等
    matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    }
    //根据规格查询sku
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        $scoep.sku = {id: 0, title: '----------', price: 0};
    }
    //添加商品到购物车
    $scope.addToCart = function () {
        //alert('SKUID'+$scope.sku.id);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id + '&num=' + $scope.num ,{'withCredentials':true}).success(
            function (response) {
                if (response.success) {
                    location.href = 'http://localhost:9107/cart.html';//跳转到购物车页面
                } else {
                    alert(response.message);
                }
            }
        );
    }
});