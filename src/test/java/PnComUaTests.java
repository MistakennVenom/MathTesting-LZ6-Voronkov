import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        // Автоматично завантажує chromedriver для Linux/Windows/Mac
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless"); // Розкоментувати для запуску без вікон
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Сценарій 4: Сортування від дешевих до дорогих (з Таблиці 3.1)
    @Test
    public void testPriceSorting() {
        driver.get(TARGET_URL);

        // Знаходимо і клікаємо на сортування (шукаємо посилання з sort=price)
        WebElement sortLink = driver.findElement(By.cssSelector("a[href*='sort=price']"));
        sortLink.click();

        // Чекаємо оновлення сторінки
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".price strong")));

        // Збираємо всі ціни
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".price strong"));
        List<Integer> prices = new ArrayList<>();

        for (WebElement el : priceElements) {
            String priceText = el.getText().replaceAll("[^0-9]", ""); // Видаляємо пробіли та "грн"
            if (!priceText.isEmpty()) {
                prices.add(Integer.parseInt(priceText));
            }
        }

        // Перевіряємо чи відсортовано за зростанням
        boolean isSorted = true;
        for (int i = 0; i < prices.size() - 1; i++) {
            if (prices.get(i) > prices.get(i + 1)) {
                isSorted = false;
                break;
            }
        }

        Assert.assertTrue("Ціни не відсортовані за зростанням!", isSorted);
    }

    // Сценарій 3: Додавання до порівняння (з Таблиці 3.1)
    @Test
    public void testCompareItems() throws InterruptedException {
        driver.get(TARGET_URL);

        // Знаходимо кнопки "Порівняти" (іконки ваг) і клікаємо перші дві
        List<WebElement> compareButtons = driver.findElements(By.cssSelector(".compare-icon"));
        if (compareButtons.size() >= 2) {
            compareButtons.get(0).click();
            Thread.sleep(1000); // Невелика пауза для анімації UI
            compareButtons.get(1).click();
        }

        // Клікаємо на плаваючу кнопку "Порівняти X товари"
        WebElement compareLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".compare-link")));
        compareLink.click();

        // Перевіряємо URL
        Assert.assertTrue("URL не містить слова 'compare'", driver.getCurrentUrl().contains("compare"));

        // Перевіряємо кількість товарів на сторінці (повинно бути 2)
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
