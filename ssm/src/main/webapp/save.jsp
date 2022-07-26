<%--
  Created by IntelliJ IDEA.
  User: kuroyume
  Date: 2022/7/25
  Time: 19:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>save</title>
</head>
<body>
<h1>添加账户信息</h1>
<form name="accuntForm" action="${pageContext.request.contextPath}/account/save" method="post">
    账户名称：<input type="text" name="name"><br/>
    账户金额:<input type="text" name="money"><br/>
    <input type="submit" value="提交">
</form>
</body>
</html>
