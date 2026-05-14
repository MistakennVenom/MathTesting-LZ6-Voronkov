import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PnComUaTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private final String TARGET_URL = "https://pn.com.ua/ct/1047/"; // Варіант 4

    @BeforeClass
    public static void setUp() {
        // Магія для Firefox: сам знайде і завантажить потрібний geckodriver
        WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();
        // options.addArguments("--headless"); // Розкоментувати для запуску без вікон
        
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Сценарій 4: Сортування від дешевих до дорогих
    @Test
    public void testPriceSorting() {
        driver.get(TARGET_URL);

        WebElement sortLink = driver.findElement(By.cssSelector("a[href*='sort=price']"));
        sortLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".price strong")));

        List<WebElement> priceElements = driver.findElements(By.cssSelector(".price strong"));
        List<Integer> prices = new ArrayList<>();

        for (WebElement el : priceElements) {
            String priceText = el.getText().replaceAll("[^0-9]", "");
            if (!priceText.isEmpty()) {
                prices.add(Integer.parseInt(priceText));
            }
        }

        boolean isSorted = true;
        for (int i = 0; i < prices.size() - 1; i++) {
            if (prices.get(i) > prices.get(i + 1)) {
                isSorted = false;
                break;
            }
        }

        Assert.assertTrue("Ціни не відсортовані за зростанням!", isSorted);
    }

    // Сценарій 3: Додавання до порівняння
    @Test
    public void testCompareItems() throws InterruptedException {
        driver.get(TARGET_URL);

        List<WebElement> compareButtons = driver.findElements(By.cssSelector(".compare-icon"));
        if (compareButtons.size() >= 2) {
            compareButtons.get(0).click();
            Thread.sleep(1000); 
            compareButtons.get(1).click();
        }

        WebElement compareLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".compare-link")));
        compareLink.click();

        Assert.assertTrue("URL не містить слова 'compare'", driver.getCurrentUrl().contains("compare"));

        List<WebElement> comparedItems = driver.findElements(By.cssSelector(".compare-item"));
        Assert.assertTrue("На сторінці не 2 товари", comparedItems.size() >= 2);
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
