<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Réinitialiser le mot de passe - Étape 2</title>
</head>
<body>
    <h2>Réinitialisation du mot de passe - Nouveau mot de passe</h2>
    <p>Veuillez définir votre nouveau mot de passe.</p>

    <s:if test="hasActionErrors()">
        <div style="color: red;">
            <s:actionerror/>
        </div>
    </s:if>

    <s:form action="changerMotDePasse" method="post">
        <s:password label="Nouveau mot de passe" name="nouveauMotDePasse" required="true" />
        <s:password label="Confirmer le nouveau mot de passe" name="confirmationMotDePasse" required="true" />
        <s:submit value="Changer le mot de passe" />
    </s:form>
</body>
</html>
