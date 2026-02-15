<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<script type="text/javascript">
	function DisplayMessage() {
		alert('Ce TD a été donné pour les AS dans le cadre du cours de CO Avancé (Promotion 2017-2018)');
	}
</script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style/style.css" />
<link href="/_00_ASBank2025/style/favicon.ico" rel="icon"
	type="image/x-icon" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Application IUT </title>
</head>
<body>

	<p>
		<img
				src="https://www.univ-lorraine.fr/wp-content/uploads/2021/03/unys-universite-de-lorraine.png"
				<%--src="https://www.iut-metz.univ-lorraine.fr/images/AdminSite/Logos/Logo_IUT_Metz.UL.small.png"--%>
			alt="logo" />
	</p>
	<input type="button" value="Information" name="info"
		onClick="DisplayMessage()" />
	<p style="font-size: 2em">
		<a href="${pageContext.request.contextPath}/redirectionLogin.action">
			Page de Login
		</a>
	</p>
</body>
<jsp:include page="/JSP/Footer.jsp" />
</html>
