app.controller("payController", function ($http, payService) {
    //本地支付
    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                //金额
                $scope.money = (response.total_fee / 100).toFixed(2);
                $scope.out_trade_no = response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element: document.getElementById('qrious'),
                    size: 250,
                    level: 'H',
                    value: response.code_url
                });
                //二维码生成后调用查询
                queryPayStatus();
            }
        );
    }
    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(
            function (response) {
                if (response.success) {
                    location.href = "paysuccess.html#?money=" + $scope.money;
                } else {
                    if (response.message == "二维码超时") {
                        createNative();
                    } else {
                        location.href = "payfail";
                    }
                }
            }
        );
    }
    //获取金额
    $scope.getMoney = function () {
        return $location.search()['money'];
    }

});