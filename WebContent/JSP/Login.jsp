<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Page de connexion</title>
<%--    Permet peut importe le nom du dossier d'actualiser le css--%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style/style.css" />
</head>

<body>
	<h1>Login :</h1>

    <s:if test="#application.messageReinitialisation != null">
        <div style="color: green;">
            <s:property value="#application.messageReinitialisation"/>
            <%-- On supprime le message pour qu'il ne s'affiche qu'une fois --%>
            <s:set var="dummy" value="#application.remove('messageReinitialisation')" />
        </div>
    </s:if>

	<s:form name="myForm" action="controller.Connect.login.action"
		method="POST">
		<s:textfield label="Code user" name="userCde" />
		<s:password label="Password" name="userPwd" />
		<s:submit name="submit" />
	</s:form>
    <p>
        <s:url action="reinitialiserMotDePasse" var="urlReinitialiser" />
        <s:a href="%{urlReinitialiser}">Mot de passe oublié ?</s:a>
    </p>
	<s:form name="myFormRetour" action="retourAccueil" method="POST">
		<s:submit name="Retour" value="Retour à l'accueil" />
	</s:form>
</body>
<jsp:include page="/JSP/Footer.jsp" />
</html>