<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/bootstrap/bootstrap.min.js"></script>
	<link href="${pageContext.request.contextPath}/css/bootstrap/bootstrap.min.css" rel="stylesheet" type="text/css"/>
	<link href="${pageContext.request.contextPath}/css/ld/admin/public.css" rel="stylesheet" type="text/css"/>
	<link href="${pageContext.request.contextPath}/css/ld/admin/user.css" rel="stylesheet" type="text/css"/>
	<title>添加用户</title>
</head>
<body>
	<!-- 一级导航栏 start-->
    <header class="navbar navbar-fixed-top navbar-ld">
        <div class="container-fluid">
            <div class="header-content">
                <div class="header-logo">
                    <img src="${pageContext.request.contextPath}/img/logo.png">
                </div>
                <!-- <div class="navbar-collapse">
                    <ul class="nav navbar-nav navbar-right">
                        <li class="dropdown">
                            <div class="dropdown-toggle" data-toggle="dropdown" id="user_dropdown">
                             	 欢迎您，${CUR_ENV.cur_user.USERNAME}
                            <span class="caret"></span>
                            </div>
                            <ul class="dropdown-menu" aria-labelledby="user_dropdown" aria-expanded="false">
                              <li class="li"><a href="#">修改密码</a></li>
                              <li role="separator" class="divider"></li>
                              <li class="li"><a href="${pageContext.request.contextPath}/logout.action">退出</a></li>
                            </ul>
                        </li>
                   </ul>
                </div>-->
                <div class="navbar-collapse">
                                                               欢迎您，${CUR_ENV.cur_user.USERNAME}
                     <div class="logoutDiv"><a title="退出" class="logoutImg" href="${pageContext.request.contextPath}/logout.action"></a></div>
                </div>
            </div>
        </div>
    </header>
    <!-- 一级导航栏 end-->

    <!-- 二级导航栏 start -->
    <div class="main-nav"> 
        <div class="aside-box">
            <ul>
                <li class="linormal"><span class="glyphicon glyphicon-home"></span><a href="${pageContext.request.contextPath}/views/admin/homeAdmin.jsp">&nbsp;&nbsp;首页</a></li>
                <li class="liactive"><span class="glyphicon glyphicon-user"></span><a href="${pageContext.request.contextPath}/views/admin/userList.jsp">&nbsp;&nbsp;用户管理</a></li>
                <li class="linormal"><span class="glyphicon glyphicon-lock"></span><a href="${pageContext.request.contextPath}/views/admin/roleEdit.jsp">&nbsp;&nbsp;角色权限查看</a></li>
                <li class="linormal"><span class="glyphicon glyphicon-list-alt"></span><a href="${pageContext.request.contextPath}/views/admin/rateList.jsp">&nbsp;&nbsp;收费标准管理</a></li>
            </ul>
        </div>
    </div>
    <div class="nav-second">
        <div class="toolbar">
            <div class="nav-secondul">
                <ul>               
                <li class="liactive"><a href="javascript:void(0);">用户列表</a></li>
                </ul>
            </div>    
        </div>
    </div>
    <!-- 二级导航栏 end-->

    <!-- 页面内容 start-->
    <div class="row">   
        <!-- 新建用户 start-->
        <div class="col-lg-12">
            <h4>新建用户</h4>
            <div class="btn btn-new btngoback" onclick="requestAjaxAddUser();" style="margin-top:-60px; margin-right:120px;">确认添加</div>
            <div class="btn btn-goback btngoback" style="margin-top:-60px;"><a href="${pageContext.request.contextPath}/views/admin/homeAdmin.jsp">返&nbsp;&nbsp;回</a></div>
        </div>
	    <div class="col-sm-12"></div>
        <div class="col-sm-12"></div>
        
        
        <div class="input-group-custom">
            <div class="col-lg-4"><span class="span">用户名 ：</span></div>
            <div class="col-lg-4">
              <input id="AdminUsername" type="text" class="form-control" value="test"/>
            </div>
            <div class="col-lg-4"><span class="spanright">*&nbsp;&nbsp;</span><span style="line-height:34px;">必填</span></div>
        </div>
        <div id="userNameNull" style="clear:both;">
            <div class="col-sm-12"></div>
	        <div style="display:none;" class="input-group-custom">
	        	<div class="col-lg-4"></div>
	        	<div class="col-lg-4"><span style="color:red;">！！不能为空</span></div>
	        	<div class="col-lg-4"></div>
	        </div>
        </div>
        <div class="col-sm-12"></div>
        
        
        <div class="input-group-custom">
            <div class="col-lg-4"><span class="span">姓名：</span></div>
            <div class="col-lg-4">
              <input id="AdminName" type="text" class="form-control" value="test"/>
            </div>
            <div class="col-lg-4"><span class="spanright">*&nbsp;&nbsp;</span><span style="line-height:34px;">必填</span></div>
        </div>
        <div id="nameNull" style="clear:both;">
            <div class="col-sm-12"></div>
	        <div style="display:none;" class="input-group-custom">
	        	<div class="col-lg-4"></div>
	        	<div class="col-lg-4"><span style="color:red;">！！不能为空</span></div>
	        	<div class="col-lg-4"></div>
	        </div>
        </div>
        <div class="col-sm-12"></div>


        <div class="input-group-custom">
            <div class="col-lg-4"><span class="span">工号：</span></div>
            <div class="col-lg-4">
              <input id="AdminNum" type="text" class="form-control" value="TEST_"/>
            </div>
        </div>      
        <div class="col-sm-12"></div>
        <div class="col-sm-12"></div>


        <div class="input-group-custom">
            <div class="col-lg-4"><span class="span">部门：</span></div>
            <div class="col-lg-4">
                <div id="AdminDepart" onclick="requestAjaxDepart();" class="form-control">
                	<span class="caret"></span>
                </div>
                <div id="AdminDepartMenu" class="dropdownMenu"></div>
            </div>
        </div>
        <div class="col-sm-12"></div>
        <div class="col-sm-12"></div>
        
        
        <div class="input-group-custom">
            <div class="col-lg-4"><span class="span">角色：</span></div>
            <div class="col-lg-4">
                <div id="AdminRole" onclick="requestAjaxRole();" class="form-control">
                	<span class="caret"></span>
            	</div>
            <div id="AdminRoleMenu" class="dropdownMenu"></div>
            </div>
            <div class="col-lg-4"><span class="spanright">*&nbsp;&nbsp;</span><span style="line-height:34px;">必填</span></div>
        </div>      
        <div id="roleNull" style="clear:both;">
            <div class="col-sm-12"></div>
	        <div style="display:none;" class="input-group-custom">
	        	<div class="col-lg-4"></div>
	        	<div class="col-lg-4"><span style="color:red;">！！不能为空</span></div>
	        	<div class="col-lg-4"></div>
	        </div>
        </div>
        <div class="col-sm-12"></div>
        <!-- 新建用户  end-->
    </div>
    <!-- 页面内容 end-->
    <script type="text/javascript" src="${pageContext.request.contextPath }/js/ld/admin/public.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath }/js/ld/admin/user.js"></script>
</body>
</html>