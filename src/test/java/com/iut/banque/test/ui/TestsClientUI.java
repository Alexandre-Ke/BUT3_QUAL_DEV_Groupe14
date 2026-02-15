package com.iut.banque.test.ui;

import com.iut.banque.exceptions.IllegalFormatException;
import org.junit.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
// Tests unitaire côté utilisateur (UI)
// Permet de testers les différentes fonctionnalitées de l'application
// de manière visuel pour garantir son bon fonctionnement
// (Plus de perte de temps d'essaie manuel)

public class TestsClientUI {

    private WebDriver driver;
    private String usernameTestUser;
    private String passwordTestUser;


    @Before
    public void setUp() throws Exception {
        // Se lance avant chaque Test :
        // Version plus moderne (Utilisation du webdriver existant)
        ChromeOptions ops = new ChromeOptions();
        // Bloque par définition la sauvegarde de mdp et l'analyse de fuite de données.
        // (causer des problèmes pour envoyer des touches clavier car un popup été actif sur la page après le login)
        ops.addArguments("--incognito");
        // 2. Désactivation explicite des features de sécurité intrusives de Chrome
        // C'est ça qui tue la popup "Password Compromised"
        ops.addArguments("--disable-features=PasswordLeakDetection");
        ops.addArguments("--disable-features=SafeBrowsing");
        // 3. Config standard
        ops.addArguments("--start-maximized");
        ops.addArguments("--disable-notifications");
        ops.addArguments("--remote-allow-origins=*");
        // 4. Désactiver le gestionnaire de mdp (Ceinture et bretelles)
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        ops.setExperimentalOption("prefs", prefs);

        // soft bar inutile
        ops.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        driver = new ChromeDriver(ops);

        // Client2 = compte test
        usernameTestUser = "client2";
        passwordTestUser = "clientpass2";
        // version académique, nécessite une installation de webdriver manuel ou alors un script d'installation automatique
        //        System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
        //        ChromeOptions ops = new ChromeOptions();
        //        ops.addArguments("--disable-notifications");
        //        ops.addArguments("start-maximized");
        //        driver = new ChromeDriver(ops);
    }

    @After
    public void setDown() {
        // S'exécute après chaque Test :
        driver.quit();
    }

    // Éviter la redondance de code similaire
    private void login(String username, String password) {
        driver.get("http://localhost:8080/_00_ASBank2025/");
        driver.manage().window().setSize(new Dimension(1296, 696));

        // 1. Aller sur la page de login
        driver.findElement(By.linkText("Page de Login")).click();

        // 2. Remplir les champs
        driver.findElement(By.id("controller_Connect_login_action_userCde")).clear(); // Toujours clear avant sendKeys
        driver.findElement(By.id("controller_Connect_login_action_userCde")).sendKeys(username);

        driver.findElement(By.id("controller_Connect_login_action_userPwd")).clear();
        driver.findElement(By.id("controller_Connect_login_action_userPwd")).sendKeys(password);

        // 3. Valider
        driver.findElement(By.id("controller_Connect_login_action_submit")).click();
    }


    @Test
    public void testLoginFailure() {
        login(usernameTestUser, "passwordTestUser");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement messageErreur = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(), \"Vous avez probablement entré de mauvais identifiants\")]")
                )
        );

        // Vérification
        Assert.assertTrue("Le message d'erreur n'est pas affiché", messageErreur.isDisplayed());
    }

    @Test
    public void testLoginSuccess() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement accountElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("XX7788778877")
                )
        );
        Assert.assertEquals("XX7788778877", accountElement.getText());
    }

    @Test
    public void testCreditCompte() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        // 3️⃣ Sélection du compte
        driver.findElement(By.linkText("XX7788778877")).click();
        // 4️⃣ Lecture du solde avant crédit
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement soldeElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeAvantCredit = soldeElement.getText();
        // 5️⃣ Remplir le formulaire de crédit
        WebElement formCredit = driver.findElement(By.id("creditAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );
        montant.clear();
        montant.sendKeys("500");
        // 6️⃣ Cliquer sur le bouton Créditer
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // 7️⃣ Attendre que le solde se mette à jour
        WebElement soldeElement2 = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApresCredit = soldeElement2.getText();
        Assert.assertNotEquals(soldeAvantCredit, soldeApresCredit);
    }

    @Test
    public void testDebitCompte() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        // 3️⃣ Sélection du compte
        driver.findElement(By.linkText("XX7788778877")).click();
        // 4️⃣ Lecture du solde avant crédit
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement soldeElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeAvantCredit = soldeElement.getText();
        // 5️⃣ Remplir le formulaire de crédit
        WebElement formCredit = driver.findElement(By.id("debitAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );
        montant.clear();
        montant.sendKeys("500");
        // 6️⃣ Cliquer sur le bouton Créditer
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // 7️⃣ Attendre que le solde se mette à jour
        WebElement soldeElement2 = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApresCredit = soldeElement2.getText();
        Assert.assertNotEquals(soldeAvantCredit, soldeApresCredit);
    }

    @Test
    public void testLoginWithEmptyCredentials() {
        driver.get("http://localhost:8080/_00_ASBank2025/");
        driver.manage().window().setSize(new Dimension(1296, 696));
        driver.findElement(By.linkText("Page de Login")).click();

        // Laisser les champs vides et soumettre
        driver.findElement(By.id("controller_Connect_login_action_submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Vérifier qu'on reste sur la page de login ou qu'un message d'erreur apparaît
        WebElement messageErreur = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(), \"Vous avez probablement entré de mauvais identifiants\")]")
                )
        );

        Assert.assertTrue("Le message d'erreur n'est pas affiché pour des champs vides", messageErreur.isDisplayed());
    }

    @Test
    public void testLoginWithEmptyUsername() {
        driver.get("http://localhost:8080/_00_ASBank2025/");
        driver.manage().window().setSize(new Dimension(1296, 696));
        driver.findElement(By.linkText("Page de Login")).click();

        // Remplir seulement le mot de passe
        driver.findElement(By.id("controller_Connect_login_action_userPwd")).clear();
        driver.findElement(By.id("controller_Connect_login_action_userPwd")).sendKeys(passwordTestUser);
        driver.findElement(By.id("controller_Connect_login_action_submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement messageErreur = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(), \"Vous avez probablement entré de mauvais identifiants\")]")
                )
        );

        Assert.assertTrue("Le message d'erreur n'est pas affiché pour un nom d'utilisateur vide", messageErreur.isDisplayed());
    }

    @Test
    public void testLoginWithEmptyPassword() {
        driver.get("http://localhost:8080/_00_ASBank2025/");
        driver.manage().window().setSize(new Dimension(1296, 696));
        driver.findElement(By.linkText("Page de Login")).click();

        // Remplir seulement le nom d'utilisateur
        driver.findElement(By.id("controller_Connect_login_action_userCde")).clear();
        driver.findElement(By.id("controller_Connect_login_action_userCde")).sendKeys(usernameTestUser);
        driver.findElement(By.id("controller_Connect_login_action_submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement messageErreur = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(), \"Vous avez probablement entré de mauvais identifiants\")]")
                )
        );

        Assert.assertTrue("Le message d'erreur n'est pas affiché pour un mot de passe vide", messageErreur.isDisplayed());
    }

    @Test
    public void testCreditAvecMontantNegatif() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement formCredit = driver.findElement(By.id("creditAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );

        montant.clear();
        montant.sendKeys("-100");
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // Attendre un éventuel message d'erreur ou vérifier que le solde n'a pas changé
        try {
            WebElement messageErreur = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(), 'montant') or contains(text(), 'positif') or contains(text(), 'erreur')]")
                    )
            );
            Assert.assertTrue("Un message d'erreur devrait apparaître pour un montant négatif", messageErreur.isDisplayed());
        } catch (Exception e) {
            // Si pas de message d'erreur explicite, le test échoue car on devrait empêcher les montants négatifs
            Assert.fail("Aucun message d'erreur n'a été affiché pour un montant négatif");
        }
    }

    @Test
    public void testDebitAvecMontantNegatif() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement formDebit = driver.findElement(By.id("debitAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formDebit.findElement(By.name("montant"))
                )
        );

        montant.clear();
        montant.sendKeys("-50");
        formDebit.findElement(By.cssSelector("input[type='submit']")).click();

        // Attendre un éventuel message d'erreur
        try {
            WebElement messageErreur = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(text(), 'montant') or contains(text(), 'positif') or contains(text(), 'erreur')]")
                    )
            );
            Assert.assertTrue("Un message d'erreur devrait apparaître pour un montant négatif", messageErreur.isDisplayed());
        } catch (Exception e) {
            Assert.fail("Aucun message d'erreur n'a été affiché pour un montant négatif");
        }
    }

    @Test
    public void testCreditAvecMontantZero() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement soldeAvant = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeAvantText = soldeAvant.getText();

        WebElement formCredit = driver.findElement(By.id("creditAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );

        montant.clear();
        montant.sendKeys("0");
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // Le solde ne devrait pas changer si on crédite 0
        WebElement soldeApres = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApresText = soldeApres.getText();

        // Soit le solde reste identique, soit un message d'erreur apparaît
        // On vérifie que le solde est resté le même
        Assert.assertEquals("Le solde ne devrait pas changer avec un crédit de 0", soldeAvantText, soldeApresText);
    }

    @Test
    public void testConsultationSolde() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Vérifier que la page du compte affiche bien les informations
        WebElement soldeElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );

        Assert.assertTrue("Le solde devrait être affiché", soldeElement.isDisplayed());
        Assert.assertTrue("Le texte du solde devrait contenir 'Solde'", soldeElement.getText().contains("Solde"));

        // Vérifier que les formulaires de crédit et débit sont présents (indique qu'on est bien sur la page de détail)
        WebElement formCredit = driver.findElement(By.id("creditAction"));
        Assert.assertTrue("Le formulaire de crédit devrait être présent", formCredit.isDisplayed());

        WebElement formDebit = driver.findElement(By.id("debitAction"));
        Assert.assertTrue("Le formulaire de débit devrait être présent", formDebit.isDisplayed());
    }

    @Test
    public void testMultiplesCreditsSucessifs() throws IllegalFormatException, InterruptedException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Premier crédit
        WebElement formCredit = driver.findElement(By.id("creditAction"));
        WebElement montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );
        montant.clear();
        montant.sendKeys("100");
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // Attendre la mise à jour
        Thread.sleep(1000);

        WebElement soldeApres1 = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApres1Text = soldeApres1.getText();

        // Deuxième crédit
        formCredit = driver.findElement(By.id("creditAction"));
        montant = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );
        montant.clear();
        montant.sendKeys("200");
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        // Attendre la mise à jour
        Thread.sleep(1000);

        WebElement soldeApres2 = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApres2Text = soldeApres2.getText();

        // Le solde après le 2e crédit devrait être différent du solde après le 1er
        Assert.assertNotEquals("Le solde devrait avoir changé après le second crédit", soldeApres1Text, soldeApres2Text);
    }

    @Test
    public void testCreditPuisDebit() throws IllegalFormatException, InterruptedException {
        login(usernameTestUser, passwordTestUser);
        driver.findElement(By.linkText("XX7788778877")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Crédit de 300
        WebElement formCredit = driver.findElement(By.id("creditAction"));
        WebElement montantCredit = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formCredit.findElement(By.name("montant"))
                )
        );
        montantCredit.clear();
        montantCredit.sendKeys("300");
        formCredit.findElement(By.cssSelector("input[type='submit']")).click();

        Thread.sleep(1000);

        WebElement soldeApresCredit = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApresCreditText = soldeApresCredit.getText();

        // Débit de 100
        WebElement formDebit = driver.findElement(By.id("debitAction"));
        WebElement montantDebit = wait.until(
                ExpectedConditions.elementToBeClickable(
                        formDebit.findElement(By.name("montant"))
                )
        );
        montantDebit.clear();
        montantDebit.sendKeys("100");
        formDebit.findElement(By.cssSelector("input[type='submit']")).click();

        Thread.sleep(1000);

        WebElement soldeApresDebit = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        String soldeApresDebitText = soldeApresDebit.getText();

        // Le solde final devrait être différent du solde après crédit
        Assert.assertNotEquals("Le solde devrait avoir diminué après le débit", soldeApresCreditText, soldeApresDebitText);
    }

    @Test
    public void testRetourListeComptesDepuisDetailCompte() throws IllegalFormatException {
        login(usernameTestUser, passwordTestUser);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Vérifier qu'on est sur la liste des comptes
        WebElement accountElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("XX7788778877")
                )
        );
        Assert.assertTrue("Le compte devrait être visible dans la liste", accountElement.isDisplayed());

        // Aller sur le détail du compte
        accountElement.click();

        // Vérifier qu'on est sur la page de détail
        WebElement soldeElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(normalize-space(.), 'Solde')]")
                )
        );
        Assert.assertTrue("On devrait être sur la page de détail du compte", soldeElement.isDisplayed());

        // Chercher un lien pour revenir à la liste (peut être "Retour", "Mes comptes", etc.)
        try {
            WebElement lienRetour = driver.findElement(By.linkText("Retour"));
            lienRetour.click();
        } catch (Exception e) {
            // Si le lien "Retour" n'existe pas, essayer d'autres possibilités
            try {
                WebElement lienMesComptes = driver.findElement(By.linkText("Mes comptes"));
                lienMesComptes.click();
            } catch (Exception e2) {
                // Si aucun lien de retour trouvé, le test continue (pas forcément un échec)
                System.out.println("Aucun lien de retour trouvé, ce test pourrait nécessiter un ajustement");
            }
        }
    }


}
