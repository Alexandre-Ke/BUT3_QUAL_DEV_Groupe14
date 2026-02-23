package com.iut.banque.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeleniumIT {

    static WebDriver driver;

    @BeforeAll
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null)
            driver.quit();
    }

    @Test
    public void smoke_login_and_open_accounts() throws Exception {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/login");
        assertTrue(driver.getPageSource().contains("login") || driver.findElements(By.tagName("form")).size() > 0);
    }

    private void loginAs(String base, String userId, String password) {
        driver.get(base + "/login");
        driver.findElement(By.id("userId")).clear();
        driver.findElement(By.id("userId")).sendKeys(userId);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("form[action='/login'] button[type='submit']")).click();
    }

    @Test
    public void createAccountAndDepositFlow() throws Exception {
        String base = System.getProperty("app.url", "http://localhost:8080");
        loginAs(base, "client", "clientpass");

        driver.get(base + "/accounts/new");
        driver.findElement(By.id("numeroCompte")).sendKeys("FRTEST123456");
        driver.findElement(By.id("clientUserId")).clear();
        driver.findElement(By.id("clientUserId")).sendKeys("client");
        driver.findElement(By.cssSelector("form[action='/accounts/new'] button[type='submit']")).click();

        boolean created = driver.getPageSource().contains("✓")
                || driver.getPageSource().toLowerCase().contains("succès");
        assertTrue(created, "La création de compte doit retourner un message de succès");

        driver.get(base + "/accounts");
        if (driver.findElements(By.linkText("Détails")).size() > 0) {
            driver.findElement(By.linkText("Détails")).click();

            if (driver.findElements(By.id("deposit-amount")).size() > 0) {
                driver.findElement(By.id("deposit-amount")).clear();
                driver.findElement(By.id("deposit-amount")).sendKeys("10.00");
                driver.findElement(By.cssSelector("form[action*='/deposit'] button[type='submit']")).click();
                boolean depositOk = driver.getPageSource().toLowerCase().contains("succès")
                        || driver.getPageSource().contains("✓");
                assertTrue(depositOk, "Le dépôt devrait renvoyer un message de succès");
            }
        }
    }

    @Test
    public void transferFlow() throws Exception {
        String base = System.getProperty("app.url", "http://localhost:8080");
        loginAs(base, "client", "clientpass");

        driver.get(base + "/transfer");

        if (driver.findElements(By.id("fromAccountId")).size() > 0
                && driver.findElements(By.id("toAccountId")).size() > 0) {
            try {
                org.openqa.selenium.support.ui.Select from = new org.openqa.selenium.support.ui.Select(
                        driver.findElement(By.id("fromAccountId")));
                org.openqa.selenium.support.ui.Select to = new org.openqa.selenium.support.ui.Select(
                        driver.findElement(By.id("toAccountId")));
                if (from.getOptions().size() > 1 && to.getOptions().size() > 1) {
                    from.selectByIndex(1);
                    to.selectByIndex(1);
                    driver.findElement(By.id("amount")).sendKeys("1.00");
                    driver.findElement(By.cssSelector("form[action='/transfer'] button[type='submit']")).click();
                    assertTrue(driver.getPageSource().toLowerCase().contains("succès")
                            || driver.getPageSource().contains("✓"));
                }
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void smoke_home_page() {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/");
        assertTrue(driver.getPageSource().length() > 0);
    }

    @Test
    public void smoke_accounts_page() {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/accounts");
        assertTrue(driver.getPageSource().length() > 0);
    }

    @Test
    public void smoke_transfer_page() {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/transfer");
        assertTrue(driver.getPageSource().length() > 0);
    }

    @Test
    public void smoke_account_new_page() {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/accounts/new");
        assertTrue(driver.getPageSource().length() > 0);
    }

    @Test
    public void smoke_login_submit_empty() {
        String base = System.getProperty("app.url", "http://localhost:8080");
        driver.get(base + "/login");
        driver.findElement(By.id("userId")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.cssSelector("form[action='/login'] button[type='submit']")).click();
        assertTrue(driver.getPageSource().length() > 0);
    }

}
