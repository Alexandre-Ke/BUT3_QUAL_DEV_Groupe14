<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Réinitialiser le mot de passe - Étape 1</title>
</head>
<body>
    <h2>Réinitialisation du mot de passe - Vérification</h2>
    <p>Veuillez fournir les informations suivantes pour vérifier votre identité.</p>

    <s:if test="hasActionErrors()">
        <div style="color: red;">
            <s:actionerror/>
        </div>
    </s:if>

    <s:form action="verifierIdentite" method="post">
        <s:textfield label="Identifiant" name="userId" required="true" />
        <s:textfield label="Nom" name="nom" required="true" />
        <s:textfield label="Prénom" name="prenom" required="true" />
        <s:textfield label="Numéro de client" name="numeroClient" required="true" />
        <s:submit value="Vérifier" />
    </s:form>
</body>
</html>
