package com.iut.banque.test.ui;

import com.iut.banque.exceptions.IllegalFormatException;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestsAdminUI {

    private WebDriver driver;
    private String usernameAdmin;
    private String passwordAdmin;

    @Before
    public void setUp() {
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--incognito");
        ops.addArguments("--disable-features=PasswordLeakDetection");
        ops.addArguments("--disable-features=SafeBrowsing");
        ops.addArguments("--start-maximized");
        ops.addArguments("--disable-notifications");
        ops.addArguments("--remote-allow-origins=*");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        ops.setExperimentalOption("prefs", prefs);
        ops.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        driver = new ChromeDriver(ops);
        usernameAdmin = "admin";
        passwordAdmin = "adminpass";
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    private void login(String username, String password) {
        driver.get("http://localhost:8080/_00_ASBank2025/");
        driver.manage().window().setSize(new Dimension(1296, 696));

        driver.findElement(By.linkText("Page de Login")).click();

        driver.findElement(By.id("controller_Connect_login_action_userCde")).clear();
        driver.findElement(By.id("controller_Connect_login_action_userCde")).sendKeys(username);

        driver.findElement(By.id("controller_Connect_login_action_userPwd")).clear();
        driver.findElement(By.id("controller_Connect_login_action_userPwd")).sendKeys(password);

        driver.findElement(By.id("controller_Connect_login_action_submit")).click();
    }

    @Test
    public void testAdminLoginSuccess() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Vérifier qu'on arrive sur le tableau de bord admin
        WebElement titreTableauDeBord = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Tableau de bord - Gestionnaire')]")
                )
        );

        Assert.assertTrue("Le tableau de bord gestionnaire devrait être affiché", titreTableauDeBord.isDisplayed());
    }

    @Test
    public void testAdminLoginFailure() {
        login(usernameAdmin, "wrongpassword");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement messageErreur = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(text(), \"Vous avez probablement entré de mauvais identifiants\")]")
                )
        );

        Assert.assertTrue("Le message d'erreur devrait être affiché", messageErreur.isDisplayed());
    }

    @Test
    public void testAccesListeComptesGlobale() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Cliquer sur "Liste des comptes de la banque"
        WebElement lienListeComptes = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Liste des comptes de la banque")
                )
        );
        lienListeComptes.click();

        // Vérifier qu'on arrive sur la page de liste des comptes
        WebElement titreListe = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Liste des comptes de la banque')]")
                )
        );

        Assert.assertTrue("La liste des comptes devrait être affichée", titreListe.isDisplayed());
    }

    @Test
    public void testAccesListeComptesADecouvert() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Cliquer sur "Liste des comptes à découvert de la banque"
        WebElement lienListeComptesDecouvert = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Liste des comptes à découvert de la banque")
                )
        );
        lienListeComptesDecouvert.click();

        // Vérifier qu'on arrive sur la page de liste des comptes à découvert
        WebElement titreListe = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Liste des comptes à découvert de la banque')]")
                )
        );

        Assert.assertTrue("La liste des comptes à découvert devrait être affichée", titreListe.isDisplayed());
    }

    @Test
    public void testRetourTableauDeBordDepuisListeComptes() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Aller sur la liste des comptes
        driver.findElement(By.linkText("Liste des comptes de la banque")).click();

        // Attendre que la page se charge
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Liste des comptes de la banque')]")
                )
        );

        // Cliquer sur le bouton Retour
        WebElement btnRetour = driver.findElement(By.cssSelector("form[action*='retourTableauDeBordManager'] input[name='Retour']"));
        btnRetour.click();

        // Vérifier qu'on est de retour sur le tableau de bord
        WebElement titreTableauDeBord = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Tableau de bord - Gestionnaire')]")
                )
        );

        Assert.assertTrue("On devrait être de retour sur le tableau de bord", titreTableauDeBord.isDisplayed());
    }

    @Test
    public void testAccesPageAjoutUtilisateur() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Cliquer sur "Ajout d'un utilisateur"
        WebElement lienAjoutUtilisateur = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Ajout d'un utilisateur")
                )
        );
        lienAjoutUtilisateur.click();

        // Vérifier qu'on arrive sur la page de création d'utilisateur
        WebElement titreCreation = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Créer un nouvel utilisateur')]")
                )
        );

        Assert.assertTrue("La page de création d'utilisateur devrait être affichée", titreCreation.isDisplayed());
    }

    @Test
    public void testVerificationPresenceBoutonLogout() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Vérifier que le bouton Logout est présent
        WebElement btnLogout = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[value='Logout']")
                )
        );

        Assert.assertTrue("Le bouton Logout devrait être visible", btnLogout.isDisplayed());
    }

    @Test
    public void testAccesModificationMotDePasse() {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Cliquer sur le lien "Modifier le mot de passe"
        WebElement lienModifierMdp = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Modifier le mot de passe")
                )
        );
        lienModifierMdp.click();

        // Vérifier qu'on arrive sur la page de modification
        WebElement titreModification = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(text(), 'Modifier') or contains(text(), 'modifier')]")
                )
        );

        Assert.assertTrue("La page de modification du mot de passe devrait être affichée", titreModification.isDisplayed());
    }

    @Test
    public void testNavigationCompleteDashboard() throws InterruptedException {
        login(usernameAdmin, passwordAdmin);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Test 1: Aller sur liste des comptes
        driver.findElement(By.linkText("Liste des comptes de la banque")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Liste des comptes de la banque')]")
        ));
        Thread.sleep(500);

        // Retour au dashboard
        driver.findElement(By.cssSelector("form[action*='retourTableauDeBordManager'] input[name='Retour']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Tableau de bord - Gestionnaire')]")
        ));
        Thread.sleep(500);

        // Test 2: Aller sur liste des comptes à découvert
        driver.findElement(By.linkText("Liste des comptes à découvert de la banque")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Liste des comptes à découvert')]")
        ));
        Thread.sleep(500);

        // Retour au dashboard
        driver.findElement(By.cssSelector("form[action*='retourTableauDeBordManager'] input[name='Retour']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Tableau de bord - Gestionnaire')]")
        ));
        Thread.sleep(500);

        // Test 3: Aller sur ajout utilisateur
        driver.findElement(By.linkText("Ajout d'un utilisateur")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Créer un nouvel utilisateur')]")
        ));

        // On a bien navigué partout
        Assert.assertTrue("La navigation complète devrait fonctionner", true);
    }
}
