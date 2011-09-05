<%@ include file="/WEB-INF/views/jsp/common/include.jsp"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 

<html xmlns="http://www.w3.org/1999/xhtml">

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		
		<!-- page title -->
		<title><tiles:getAsString name="title" /></title>
		
		<!-- common <head> content -->
		<tiles:insertAttribute name="head" />
		
		<!-- page specific javascript -->
		<tiles:insertAttribute name="script" />
		    
	</head>
	
	<body class="yui-skin-sam">
	
		<table align="center" width="900px">
		
			<tr><td><tiles:insertAttribute name="header" /></td></tr>
			<tr><td><hr class="line"/></td></tr>
			<tr><td><tiles:insertAttribute name="body" /></td></tr>
			<tr><td><tiles:insertAttribute name="footer" /></td></tr>
		
		</table>
		
	</body>
	
</html>