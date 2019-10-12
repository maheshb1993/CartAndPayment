import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class CartTests {
    WebDriver driver;
    WebDriverWait wait;
    By addToCartProductPageButton = By.name("add-to-cart");
    By addToCartCategoryPageButton = By.cssSelector("[data-product_id=\"391\"]");
    By showCartCategoryPageButton = By.cssSelector("[class='added_to_cart wc-forward']");
    By boxMessage = By.className("woocommerce-message");
    By quantityInput = By.xpath(".//td[@class='product-quantity']//input");
    By cartButton = By.xpath(".//ul[@id='menu-menu']/li[4]");
    String expectedText = "";
    String quantity = "";
    WebElement addProduct;
    Boolean notice = true;
    String[] productPages = {"egipt-el-gouna/", "fuerteventura-sotavento/", "grecja-limnos/", "windsurfing-w-karpathos/",
    "wyspy-zielonego-przyladka-sal/", "wspinaczka-island-peak/", "wspinaczka-via-ferraty/", "wakacje-z-yoga-w-kraju-kwitnacej-wisni/",
    "wczasy-relaksacyjne-z-yoga-w-toskanii/", "yoga-i-pilates-w-hiszpanii/"};

    @BeforeEach
    public void setupDriver() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
//        WebDriverManager.firefoxdriver().setup();
//        driver = new FirefoxDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 10);
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();
    }

    @Test
    public void addProductFromProductPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", notice);
        addToCartFormProductPage();
        expectedText = "“Grecja – Limnos” został dodany do koszyka.";

        Assertions.assertTrue(driver.findElement(boxMessage).getText().contains(expectedText),
                "Message after add product from product page is not what expected.");
    }

    @Test
    public void addProductFromCategoryPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/windsurfing/", notice);
        addProduct = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(addToCartCategoryPageButton)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addProduct);
        addProduct.click();
        wait.until(ExpectedConditions.elementToBeClickable(showCartCategoryPageButton)).click();

        Assertions.assertTrue(driver.findElement(By.cssSelector("[data-product_id='391']")).isDisplayed(),
                "Cart after add product from category page is empty.");
    }

    @Test
    public void addTenTimesProductFromProductPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", notice);
        for(int i=0; i < 10; i++){
            addToCartFormProductPage();
        }
        driver.findElement(cartButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(quantityInput));

        Assertions.assertEquals("10", driver.findElement(quantityInput).getAttribute("value"),
                "Cart doesn't have correct quantity of product, after add 10 times product from product page.");
    }

    @Test
    public void addProductChangeQuantityOnProductPage() {
        quantity = "5";

        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", notice);
        WebElement quantityElementInput = driver.findElement(By.name("quantity"));
        quantityElementInput.clear();
        quantityElementInput.sendKeys(quantity);
        addToCartFormProductPage();
        driver.findElement(cartButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(quantityInput));

        Assertions.assertEquals("5", driver.findElement(quantityInput).getAttribute("value"),
                "Cart doesn't have correct quantity of product, after change quantity and add product from product page.");
    }

    @Test
    public void addTenDifferentProducts() {
        for(int i=0; i < 10; i++){
            if(i>0){
                notice = false;
            }
            navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/" + productPages[i], notice);
            addToCartFormProductPage();
        }
        driver.findElement(cartButton).click();

        Assertions.assertEquals(10,
                driver.findElements(By.xpath(".//form//tbody/tr[contains(@class, 'cart_item')]")).size(),
                "The cart has different quantity products than 10.");
    }

    @Test
    public void addProductChangeQuantityOnCartPage() {
        quantity = "3";
        By updateLocator = By.name("update_cart");
        By subTotalLocator = By.cssSelector("td.product-subtotal");

        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", notice);
        addToCartFormProductPage();
        wait.until(ExpectedConditions.elementToBeClickable(cartButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(quantityInput)).clear();
        driver.findElement(quantityInput).sendKeys(quantity);
        wait.until(ExpectedConditions.elementToBeClickable(updateLocator)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("blockOverlay")));

        Assertions.assertEquals("9 600,00 zł", driver.findElement(subTotalLocator).getText(),
                "Cart doesn't have correct total price, after change quantity and update cart.");
    }

    @Test
    public void deleteProductFromCartPage() {
        By removeLocator = By.cssSelector("td.product-remove>a");

        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/yoga-i-pilates-w-hiszpanii/", notice);
        addToCartFormProductPage();
        driver.findElement(cartButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(removeLocator)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(removeLocator));

        Assertions.assertTrue(driver.findElements(By.xpath(".//form//tbody/tr[contains(@class, 'cart_item')]")).size() == 0,
                "Product was not remove from cart.");
    }

    private void navigateAndCloseDemoNotice(String page, Boolean notice) {
        driver.navigate().to(page);
        if (notice) {
            driver.findElement(By.className("woocommerce-store-notice__dismiss-link")).click();
        }
    }

    private void addToCartFormProductPage(){
        wait.until(ExpectedConditions.elementToBeClickable(addToCartProductPageButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(boxMessage));
    }

}
