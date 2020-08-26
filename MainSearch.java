import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class MainSearch {
    private WebDriver wd;
    private String url;
    private String year;
    private String sortMethod;

    @Before
    public void setUp(){
        System.setProperty("webdriver.gecko.driver", "C:/SeleniumGecko/geckodriver.exe");
        wd = new FirefoxDriver();
        url = "https://www.autohero.com/de/search/";
        year = "2015";
        sortMethod = "Höchster Preis";
    }

    public void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                    }
                };
        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(wd, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            Assert.fail("Timeout waiting for Page Load Request to complete.");
        }
    }

    private void selectDropdown(String fieldName, String valueSet){
        Select temp = new Select(wd.findElement(By.xpath("//select[@name='" + fieldName + "']")));
        temp.selectByVisibleText(valueSet);
    }

    @Test
    public void mainTest(){
        wd.get(url);
        wd.findElement(By.xpath("//div[@data-qa-selector='filter-year']")).click();
        selectDropdown("yearRange.min", year);
        selectDropdown("sort", sortMethod);
        float lastLowest = 0;

        do {
            waitForPageLoaded();
            List<WebElement> pageYearRecords = wd.findElements(By.xpath("//div[@class='item___T1IPF']//child::ul/li[1]"));
            for (int i = 0; i < pageYearRecords.size(); i++) {
                if (Integer.parseInt(pageYearRecords.get(i).getText().substring(5)) < Integer.parseInt(year))
                    Assert.fail("Error, there is a car older than" + year);
            }

            List<WebElement> pagePriceRecords = wd.findElements(By.xpath("//div[@class='item___T1IPF']//child::span[@data-qa-selector='price']"));
            for (int i = 0; i < pagePriceRecords.size(); i++) {
                String temp = pagePriceRecords.get(i).getText().replace(".", "");
                temp = temp.substring(0, temp.length() - 2);
                if (lastLowest < Float.parseFloat(temp)) {
                    if (lastLowest == 0) lastLowest = Float.parseFloat(temp);
                    else Assert.fail("Error, elements are not sorted properly!");
                } else {
                    lastLowest = Float.parseFloat(temp);
                }
            }

            int secondLastElementIndex = wd.findElements(By.xpath("//ul[@class='pagination']/li")).size()-1;

            if(wd.findElement(By.xpath("//ul[@class='pagination']//child::li["+secondLastElementIndex+"]")).getAttribute("class").equals("disabled")) break;
            else wd.get(wd.findElement(By.xpath("//ul[@class='pagination']//child::li["+secondLastElementIndex+"]/a")).getAttribute("href"));
        }while (true);
    }

    @After
    public void tearDown() {
        wd.quit();
    }
}
