<%@ page session="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page import="java.util.*"%>
<%@ page import="org.stoevesand.brain.*"%>
<%@ page import="org.stoevesand.brain.auth.*"%>
<%@ page import="org.stoevesand.brain.model.*"%>
<%@ page session="false"%>
<%
	String error = request.getParameter("msg");
	String msg = null;
	if (error != null) {
		msg = "Wrong user or password.";
	}
%>
<html>
<h:head>
<title>Notonto</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.css" />
<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="http://code.jquery.com/mobile/1.0/jquery.mobile-1.0.min.js"></script>
</h:head>

<%
	if (msg != null) {
%>

<h:body onload="alert('<%=msg%>');">
<%
	} else {
%>

<h:body>
<%
	}
%>
<div data-role="page">
<div data-role="header">
<h1>Notonto Login</h1>
</div>
<div data-role="content">
<form id="lessons" class="panel" method="GET" title="Notonto" selected="true" action="/iphone/lessons.jsp" target="_self">
<fieldset>
	<div data-role="fieldcontain">
		<label for="username">Name</label> 
		<input type="text" id="username" name="username" value="" placeholder="Username"/>
	</div>
	<div data-role="fieldcontain">
		<label for="password">Password</label> 
		<input type="password" id="password" name="password" value="" placeholder="Password"/>
	</div>
</fieldset>
<button type="submit" data-theme="b" name="submit" value="submit-value">Submit</button>
</form>
</div>
<div data-role="footer">...</div>
</div>
</h:body>
</html>
