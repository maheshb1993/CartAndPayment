import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
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
    String expectedText = "";
    String quantity = "";
    WebElement addProduct;

    @BeforeEach
    public void setupDriver() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 10);
    }

    @AfterEach
    public void quitDriver() {
        driver.quit();
    }

    private void navigateAndCloseDemoNotice(String page, Boolean notice) {
        driver.navigate().to(page);
        if (notice) {
            driver.findElement(By.className("woocommerce-store-notice__dismiss-link")).click();
        }
    }

    @Test
    public void addProductFromProductPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", true);
        driver.findElement(addToCartProductPageButton).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(boxMessage));
        expectedText = "“Grecja – Limnos” został dodany do koszyka.";
        Assertions.assertTrue(driver.findElement(boxMessage).getText().contains(expectedText),
                "Message after add product from product page is not what expected.");
    }

    @Test
    public void addProductFromCategoryPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product-category/windsurfing/", true);
        addProduct = driver.findElement(addToCartCategoryPageButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addProduct);
        addProduct.click();
        wait.until(ExpectedConditions.elementToBeClickable(showCartCategoryPageButton));
        driver.findElement(showCartCategoryPageButton).click();
        Assertions.assertTrue(driver.findElement(By.cssSelector("[data-product_id='391']")).isDisplayed(),
                "Cart after add product from category page is empty.");
    }

    @Test
    public void addTenTimesProductFromProductPage() {
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", true);
        for(int i=0; i < 10; i++){
            driver.findElement(addToCartProductPageButton).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(boxMessage));
        }
        driver.findElement(By.xpath(".//ul[@id='menu-menu']/li[4]")).click();
        By quantityInput = By.xpath(".//td[@class='product-quantity']//input");
        wait.until(ExpectedConditions.elementToBeClickable(quantityInput));

        Assertions.assertEquals("10", driver.findElement(quantityInput).getAttribute("value"),
                "Cart doesn't have correct quantity of product, after add 10 times product from product page.");
    }

    @Test
    public void addProductChangeQuantityOnProductPage() {
        quantity = "5";
        navigateAndCloseDemoNotice("https://fakestore.testelka.pl/product/grecja-limnos/", true);
        WebElement quantityInput = driver.findElement(By.name("quantity"));
        quantityInput.clear();
        quantityInput.sendKeys(quantity);
        driver.findElement(addToCartProductPageButton).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(boxMessage));
        driver.findElement(By.xpath(".//ul[@id='menu-menu']/li[4]")).click();
        By quantityLocator = By.xpath(".//td[@class='product-quantity']//input");
        wait.until(ExpectedConditions.elementToBeClickable(quantityLocator));

        Assertions.assertEquals("5", driver.findElement(quantityLocator).getAttribute("value"),
                "Cart doesn't have correct quantity of product, after change quantity and add product from product page.");
    }




}
