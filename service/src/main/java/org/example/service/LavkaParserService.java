package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Product;
import org.example.entity.Shop;
import org.example.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class LavkaParserService implements Parserable {

    private static final String URL = "https://lavka.yandex.ru/catalog/grocery/category/water";

    @Override
    public List<Product> getProducts() throws InterruptedException {
        WebDriver driver = DriverUtils.getDriver();
        try {
            log.info("Starting Lavka parsing from URL: {}", URL);
            driver.get(URL);
            Thread.sleep(15000);

            Set<String> seenRefs = new HashSet<>();
            List<Product> products = new ArrayList<>();
            JavascriptExecutor js = (JavascriptExecutor) driver;

            int stableRepeats = 0, prevCount = 0, maxRepeats = 4;

            while (stableRepeats < maxRepeats) {
                List<WebElement> elems = driver.findElements(By.cssSelector("[class=\"p19kkpiw\"]"));

                for (WebElement card : elems) {
                    try {
                        String ref = card
                                .findElement(By.cssSelector("[data-type=\"product-card-link\"]"))
                                .getDomProperty("href");

                        String title = card
                                .findElement(By.cssSelector("[class=\"t13q9bt7 t18stym3 bw441np r88klks r1dbrdpx n10d4det l14lhr1r\"]"))
                                .getText().replaceAll("[\\p{Pd}\\u00AD\\u200B]", "");

                        String volume = card.findElement(By.cssSelector("[class=\"m12g4kzj\"]")).getText();

                        if (!seenRefs.contains(ref) && !title.isBlank()) {
                            seenRefs.add(ref);

                            String priceDiscount = card
                                    .findElement(By.cssSelector("[class=\"b15aiivf t18stym3 b1clo64h m493tk9 m1fg51qz tnicrlv l14lhr1r\"]"))
                                    .getText();

                            String price = card
                                    .findElement(By.cssSelector("[class=\"t18stym3 bw441np r88klks r1dbrdpx t1dh4tmf l14lhr1r\"]"))
                                    .getText();

                            Product product = Product.builder()
                                    .title(title)
                                    .oldPriceStr(price)  // Основная цена
                                    .newPriceStr(priceDiscount)  // Цена со скидкой
                                    .volume(volume)
                                    .ref(ref)
                                    .shop(Shop.LAVKA)
                                    .build();

                            products.add(product);

                        } else if (seenRefs.contains(ref) && !title.isBlank() && volume != null) {
                            // Обновляем объем для существующего продукта
                            products.stream()
                                    .filter(p -> p.getRef().equals(ref))
                                    .forEach(p -> p.setVolume(volume));
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing product card: {}", e.getMessage());
                    }
                }

                if (products.size() == prevCount) {
                    stableRepeats++;
                } else {
                    stableRepeats = 0;
                    prevCount = products.size();
                }

                js.executeScript("window.scrollBy(0,850);");
                Thread.sleep(200);
            }

            // Финальный проход для дозагрузки
            Thread.sleep(500);
            List<WebElement> finalElements = driver.findElements(By.cssSelector("[class=\"p19kkpiw\"]"));

            for (WebElement card : finalElements) {
                try {
                    String ref = card
                            .findElement(By.cssSelector("[data-type=\"product-card-link\"]"))
                            .getDomProperty("href");

                    String title = card
                            .findElement(By.cssSelector("[class=\"t13q9bt7 t18stym3 bw441np r88klks r1dbrdpx n10d4det l14lhr1r\"]"))
                            .getText().replaceAll("[\\p{Pd}\\u00AD\\u200B]", "");

                    if (!seenRefs.contains(ref) && !title.isBlank()) {
                        seenRefs.add(ref);

                        String priceDiscount = card
                                .findElement(By.cssSelector("[class=\"b15aiivf t18stym3 b1clo64h m493tk9 m1fg51qz tnicrlv l14lhr1r\"]"))
                                .getText();

                        String price = card
                                .findElement(By.cssSelector("[class=\"t18stym3 bw441np r88klks r1dbrdpx t1dh4tmf l14lhr1r\"]"))
                                .getText();

                        String volume = card.findElement(By.cssSelector("[class=\"m12g4kzj\"]")).getText();

                        Product product = Product.builder()
                                .title(title)
                                .oldPriceStr(price)
                                .newPriceStr(priceDiscount)
                                .volume(volume)
                                .ref(ref)
                                .shop(Shop.LAVKA)
                                .build();

                        products.add(product);
                    }
                } catch (Exception e) {
                    log.debug("Error in final parsing: {}", e.getMessage());
                }
            }

            log.info("Lavka parsing completed. Found {} products", products.size());
            return products;

        } finally {
            driver.quit();
        }
    }
}