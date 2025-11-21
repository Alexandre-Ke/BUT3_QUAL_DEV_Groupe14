<%--
  Created by IntelliJ IDEA.
  User: Amar
  Date: 07/11/2025
  Time: 00:39
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!-- Empêcher la mise en cache du formulaire de mot de passe -->
    <meta http-equiv="Cache-Control" content="no-store, no-cache, must-revalidate, max-age=0">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">

    <title>Modifier mon mot de passe</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/style/style.css" />
    <script src="/_00_ASBank2023/js/jquery.js"></script>
    <!-- (optionnel) votre JS côté client pour validations locales -->
    <script src="/_00_ASBank2023/js/jsChangePassword.js"></script>
</head>

<body>
<!-- Bouton Logout -->
<div class="btnLogout">
    <s:form name="logoutForm" action="logout" method="POST">
        <s:submit name="logout" value="Logout" />
    </s:form>
</div>

<h1>Modifier mon mot de passe</h1>

<!-- Messages globaux -->
<div class="messages">
    <s:actionmessage />
    <s:actionerror />
    <s:fielderror />
</div>

<!-- Formulaire de changement de mot de passe -->
<s:form id="changePwdForm" name="changePwdForm" action="modifierMotDePasse" method="POST" autocomplete="off">
    <!-- Token CSRF Struts2 -->
    <s:token />

    <!-- Identité (lecture seule) -->
    <s:if test="#session.userId != null">
        <s:textfield label="Identifiant" name="userIdDisplay" value="%{#session.userId}" readonly="true" />
        <!-- Passage silencieux de l'identifiant côté serveur (on se base sur la session, pas sur cette valeur) -->
        <s:hidden name="userId" value="%{#session.userId}" />
    </s:if>
    <s:else>
        <!-- Si, pour une raison quelconque, la session n'a pas l'ID, on l'affiche vide en lecture seule -->
        <s:textfield label="Identifiant" name="userIdDisplay" value="" readonly="true" />
    </s:else>

    <!-- Mot de passe actuel -->
    <s:password label="Mot de passe actuel" name="oldPwd" required="true" />

    <!-- Nouveau mot de passe -->
    <s:password label="Nouveau mot de passe" name="newPwd" required="true" />

    <!-- Confirmation du nouveau mot de passe -->
    <s:password label="Confirmer le nouveau mot de passe" name="newPwdConfirm" required="true" />

    <s:submit name="submit" value="Changer le mot de passe" />
</s:form>

<!-- Retour tableau de bord (adaptez l’action si besoin: client/manager) -->
<s:form name="retourForm" action="retourTableauDeBordManager" method="POST">
    <s:submit name="Retour" value="Retour" />
</s:form>

<!-- Bloc succès/échec compatible avec votre logique existante -->
<s:if test="(result == \"SUCCESS\")">
    <div class="success">
        <s:property value="message" />
    </div>
</s:if>
<s:else>
    <div class="failure">
        <s:property value="message" />
    </div>
</s:else>
</body>

<jsp:include page="/JSP/Footer.jsp" />
</html>
