 //控制层 
app.controller('userController' ,function($scope,$controller ,userService){

	//注册
	$scope.reg =function(){
        //比较两次输入的密码是否一致
        if ($scope.password!=$scope.entity.password){
            alert("两次密码输入不一致，请重新输入");
            $scope.password="";
            $scope.entity.password="";
            return;
        }
        //新增
		userService.add($scope.entity,$scope.smscode).success(
			function(response){
			alert(response.message);
			}
		);
	}
	$scope.sendCode=function(){
		if ($scope.entity.phone==null||$scope.entity.phone==''){
			alert("请填写手机号码");
			return;
		}
		userService.sendCode($scope.entity.phone).success(
			function(response){
				alert(response.message);
			}
		)
	}

});	
