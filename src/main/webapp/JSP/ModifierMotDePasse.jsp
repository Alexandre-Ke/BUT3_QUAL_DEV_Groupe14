<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Modifier le mot de passe</title>
</head>
<body>
    <h2>Modifier le mot de passe</h2>

    <s:if test="hasActionErrors()">
        <div style="color: red;">
            <s:actionerror/>
        </div>
    </s:if>

    <s:if test="hasActionMessages()">
        <div style="color: green;">
            <s:actionmessage/>
        </div>
    </s:if>

    <s:form action="modifierMotDePasse" method="post">
        <s:textfield label="Identifiant" name="userIdDisplay" value="%{getConnectedUser().getUserId()}" disabled="true" />
        <s:password label="Mot de passe actuel" name="oldPwd" required="true" />
        <s:password label="Nouveau mot de passe" name="newPwd" required="true" />
        <s:password label="Confirmer le nouveau mot de passe" name="newPwdConfirm" required="true" />
        <s:submit value="Changer le mot de passe" />
    </s:form>
</body>
</html>
