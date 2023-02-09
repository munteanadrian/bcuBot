import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.tinylog.Logger;


import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class Driver {
    private final WebDriver driver;
    public Driver() {
        // set path to chromedriver
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        this.driver.manage().deleteAllCookies();
        this.driver.get("http://aleph.bcucluj.ro:8991");
        trafficError();
        Logger.info("Website loaded successfully, options loaded");
    }

    public static void main(String[] args) {
        Driver browser = new Driver();

        browser.login();
        browser.goToSeats();
        browser.getSeatList();
    }

    public void login() {
        while (true) {
            try {
                driver.navigate().to("http://aleph.bcucluj.ro:8991/");
                this.driver.findElement(By.xpath("//a[contains(text(), 'Autentificare')]")).click();
                Logger.info("On login page");

//                workaround to not delete input automatically
                this.driver.findElement(By.cssSelector("input[name = 'bor_id']")).click();
                this.driver.findElement(By.cssSelector("input[name = 'bor_id']")).clear();

                this.driver.findElement(By.cssSelector("input[name = 'bor_id']")).sendKeys(""); // aici id
                this.driver.findElement(By.cssSelector("input[name = 'bor_verification']")).sendKeys(""); // aici parola
                Logger.info("UID & PWW entered");

                this.driver.findElement(By.cssSelector("input[title='Autentificare utilizator cu permis valid']")).click();
                Logger.info("Logged in");

                break;
            } catch (Exception e) {
                trafficError();
                driver.navigate().to("http://aleph.bcucluj.ro:8991/");
                Logger.info(e);
            }
        }
    }

    public void goToSeats() {
        while (true) {
            try {
                this.driver.navigate().to("http://aleph.bcucluj.ro:8991");
                this.driver.findElement(By.cssSelector("a[title='REZERVARE LOCURI - necesită AUTENTIFICARE']")).click();
                Logger.info("On seats page");

                break;
            } catch (Exception e) {
                trafficError();
                Logger.info(e);
            }
        }
    }

    public void trafficError() {
        while(this.driver.getPageSource().contains("Incercati mai tarziu")) {
            this.driver.navigate().refresh();
            Logger.info("Busy, retrying...");
        }
    }

    public void getSeatList() {
        while (true) {
            try {
//                if (this.driver.getPageSource().contains("Autentificare")) {
//                 nu merge
//                    login();
//                    goToSeats();
//                }

                List<WebElement> freeSeats = this.driver.findElements(By.cssSelector("a[href*=\"item_sequence=00"));
                Logger.info("List of seats: " + freeSeats.size() + " available");

                while (freeSeats.size() == 0) {
                    this.driver.navigate().refresh();
                    freeSeats.addAll(this.driver.findElements(By.cssSelector("a[href*=\"item_sequence=00")));
                }

                boolean booked = false;
                Logger.info("Cycling through " + freeSeats.size() + " seats");
                for (WebElement seat : freeSeats) {
                    if (!booked) {
                        Logger.info("Trying for seat " + seat.toString());
                        booked = this.book(seat);
                    } else {
                        Logger.info("Booked seat " + seat.toString());
                        break;
                    }
                }

                    break;
            } catch (Exception e) {
                trafficError();
                goToSeats();
                Logger.info(e);
            }
        }
    }

    public boolean book(WebElement seat) {
        while (true) {
            try {
                seat.click();
                Logger.info("On seat page");

                this.driver.findElement(By.cssSelector("input[alt=\"Order\"")).click();
                this.driver.findElement(By.cssSelector("input[src=\"http://aleph.bcucluj.ro:8991/exlibris/aleph/u23_1/alephe/www_f_rum/icon/fin-ru.gif\"]")).click();
                Logger.info("Success");

                break;
            } catch (Exception e) {
                trafficError();

                goToSeats();
                Logger.info(e);
            }
        }

        if (this.driver.getPageSource().contains("Nu a fost selectat")) {
            getSeatList();
            Logger.info("Seat booked by someone else, going back");
            return false;
        }

        return true;
    }
}
