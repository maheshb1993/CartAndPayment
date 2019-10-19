import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTests {
    WebDriver driver;
    WebDriverWait wait;
    WebElement product;
    WebElement cardNumber;
    WebElement expDate;
    WebElement cvc;
    By toCartButton = By.cssSelector("[class='added_to_cart wc-forward']");
    By checkoutButton = By.cssSelector(".checkout-button");
    By logoutButton = By.xpath(".//ul[@class='phoen_nav_tab']/li[5]");
    By orderMessage = By.className("woocommerce-thankyou-order-received");

    @BeforeEach
    public void setupDriver() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
//        WebDriverManager.firefoxdriver().setup();
//        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 10);
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();
    }

    @Test
    public void buyWithoutAccount() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/yoga-i-pilates/", true);
        addProductAndViewCart(61);
        driver.findElement(checkoutButton).click();
        fillBillingFields();
        fillPaymentFields();
        buyAndWait();
        Assertions.assertTrue(driver.findElements(orderMessage).size() == 1,
                "After buy without account, number of order received message is not 1. Was order successful?");
    }

    @Test
    public void buyWithAccount(){
        registerUserAndLogout();
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/yoga-i-pilates/", false);
        addProductAndViewCart(64);
        driver.findElement(checkoutButton).click();
        loginOnOrderPage();
        fillBillingFields();
        fillPaymentFields();
        buyAndWait();
        Assertions.assertTrue(driver.findElements(orderMessage).size() == 1,
                "After buy with account, number of order received message is not 1. Was order successful?");
        deleteUser();
    }

    @Test
    public void buyAndCreateAccount(){
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/wspinaczka/", true);
        addProductAndViewCart(40);
        driver.findElement(checkoutButton).click();
        fillBillingFields();
        fillPaymentFields();
        driver.findElement(By.id("createaccount")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("account_password"))).sendKeys("jan@jan.pl");
        buyAndWait();
        Assertions.assertTrue(driver.findElements(orderMessage).size() == 1,
                "After create account and buy, number of order received message is not 1. Was order successful?");
        deleteUser();
    }

    @Test
    public void buyWithAccountAndSeeOrder(){
        registerUserAndLogout();
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/wspinaczka/", false);
        addProductAndViewCart(42);
        driver.findElement(checkoutButton).click();
        loginOnOrderPage();
        fillBillingFields();
        fillPaymentFields();
        buyAndWait();
        goToMyAccountAndSeeOrders();
        Assertions.assertTrue(driver.findElements(By.xpath(".//table[contains(@class, 'account-orders-table')]//tbody/tr")).size() == 1,
                "Number of order in orders page is not 1.");
        deleteUser();
    }

    @Test
    public void buyWithoutAccountOrderSummary() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/yoga-i-pilates/", true);
        addProductAndViewCart(61);
        driver.findElement(checkoutButton).click();
        fillBillingFields();
        fillPaymentFields();
        buyAndWait();

        int orderNumber = Integer.parseInt(wait.until(ExpectedConditions.
                visibilityOf(driver.findElement(By.cssSelector(".order>strong")))).getText());
        String currentDate = getCurrentDate();
        String expectedDate = driver.findElement(By.cssSelector(".date>strong")).getText();
        String price = driver.findElement(By.cssSelector("strong>span.amount")).getText();
        String expectedPrice = "4 299,00 zł";
        String paymentMethod = driver.findElement(By.cssSelector(".method")).getText();
        String productName = driver.findElement(By.cssSelector(".product-name>a")).getText();
        String expectedProductName = "Wakacje z yogą w Kraju Kwitnącej Wiśni";
        String productQuantity = driver.findElement(By.cssSelector(".product-name>strong")).getText();
        String expectedProductQuantity = "× 1";

        assertAll(
                ()->assertTrue(orderNumber > 0, "Order number is not bigger than 0"),
                ()->assertEquals(expectedDate, currentDate, "Expected " + expectedDate + " date, but was " +
                        currentDate),
                ()->assertEquals(expectedPrice, price, "Expected " + expectedPrice + " price, but was " +
                        price),
                ()->assertTrue(paymentMethod.contains("Karta debetowa/kredytowa (Stripe)"), "Payment method was in not what expected"),
                ()->assertEquals(expectedProductName, productName, "Expected " + expectedProductName +
                        " name, but was " + productName),
                ()->assertEquals(expectedProductQuantity, productQuantity, "Expected " + expectedProductQuantity +
                        " product quantity, but was " + productQuantity)
                );

    }

    private String getCurrentDate() {
        Calendar date = Calendar.getInstance();
        String fullDate = getPolishMonth(date.get(Calendar.MONTH)) + " " + date.get(Calendar.DAY_OF_MONTH) + ", " +
                date.get(Calendar.YEAR);
        return fullDate;
    }

    private String getPolishMonth(int numberOfMonth) {
        String[] months = {"Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień",
        "Październik", "Listopad", "Grudzień"};
        return months[numberOfMonth];
    }

    private void navigateAndCloseDemoNotice(String page, Boolean notice) {
        driver.navigate().to(page);
        if (notice) {
            wait.until(ExpectedConditions.elementToBeClickable(By.className("woocommerce-store-notice__dismiss-link"))).click();
        }
    }

    private void registerUserAndLogout(){
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/moje-konto/", true);
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("reg_email")))).sendKeys("jan@jan.pl");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("reg_password")))).sendKeys("jan@jan.pl");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("register")))).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(logoutButton));
        driver.findElement(logoutButton).click();
    }

    private void deleteUser(){
        driver.findElement(By.cssSelector("#menu-menu>.my-account")).click();
        if(wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.className("delete-me")))).isEnabled()){
            driver.findElement(By.className("delete-me")).click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("entry-title")));
    }

    private void fillBillingFields(){
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_first_name"))).sendKeys("Jan");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_last_name"))).sendKeys("Nowak");
        wait.until(ExpectedConditions.elementToBeClickable(By.className("select2-selection__arrow"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id*='-FR']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_address_1"))).sendKeys("Krucza 10");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_postcode"))).sendKeys("00-000");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_city"))).sendKeys("Warszawa");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_phone"))).sendKeys("123456789");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("billing_email"))).clear();
        driver.findElement(By.id("billing_email")).sendKeys("jan@jan.pl");
    }

    private void fillPaymentFields(){
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("blockOverlay")));
        driver.findElement(By.name("terms")).click();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("__privateStripeFrame8")));
        cardNumber = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("cardnumber"))));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cardNumber);
        typeSlow(cardNumber, "4242424242424242");
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("__privateStripeFrame9")));
        expDate = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("exp-date"))));
        typeSlow(expDate, "1219");
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("__privateStripeFrame10")));
        cvc = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("cvc"))));
        typeSlow(cvc, "123");
        driver.switchTo().defaultContent();
    }

    private void typeSlow(WebElement element, String text){
        for(int i=0; i<text.length(); i++){
            element.sendKeys(Character.toString(text.charAt(i)));
            element.isSelected();
        }
    }

    private void buyAndWait(){
        driver.findElement(By.id("place_order")).click();
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.urlContains("zamowienie/zamowienie-otrzymane"));
    }

    private void addProductAndViewCart(int id){
        product = wait.until(ExpectedConditions.elementToBeClickable(
                driver.findElement(By.cssSelector("[data-product_id='" + id + "']"))));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", product);
        product.click();
        wait.until(ExpectedConditions.elementToBeClickable(toCartButton)).click();
    }

    private void goToMyAccountAndSeeOrders(){
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-item-201"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[class*='navigation-link--orders']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table[class*='account-orders-table']")));
    }

    private void loginOnOrderPage(){
        wait.until(ExpectedConditions.elementToBeClickable(By.className("showlogin"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("username"))).sendKeys("jan@jan.pl");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("password"))).sendKeys("jan@jan.pl");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("login"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("blockOverlay")));
    }

}
