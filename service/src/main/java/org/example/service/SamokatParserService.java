package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Product;
import org.example.entity.Shop;
import org.example.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SamokatParserService implements Parserable {

    private static final String URL = "https://samokat.ru/category/voda";

    @Override
    public List<Product> getProducts() throws InterruptedException {
        WebDriver driver = DriverUtils.getDriver();
        try {
            log.info("Starting Samokat parsing from URL: {}", URL);
            driver.get(URL);
            Thread.sleep(3000);

            List<Product> products = new ArrayList<>();
            List<WebElement> productLists = driver.findElements(By.cssSelector("[class=\"ProductsList_productList__jjQpU\"]"));

            for (WebElement productList : productLists) {
                List<WebElement> elementsInList = productList.findElements(By.tagName("a"));

                for (WebElement element : elementsInList) {
                    try {
                        String href = element.getDomProperty("href");

                        // Проверяем, что товар еще не добавлен
                        if (products.stream().noneMatch(p -> p.getRef().equals(href))) {

                            String title = element
                                    .findElement(By.cssSelector("[class=\"ProductCard_name__2VDcL\"]"))
                                    .findElement(By.cssSelector("[class=\"Text_text__7SbT7 Text_text--type_p3SemiBold__nZftu\"]"))
                                    .getText();

                            String volume = element
                                    .findElement(By.cssSelector("[class=\"ProductCard_specification__Y0xA6\"]"))
                                    .findElement(By.cssSelector("[class=\"Text_text__7SbT7 Text_text--type_p3SemiBold__nZftu\"]"))
                                    .getText();

                            // Старая цена (если есть скидка)
                            String oldPrice = null;
                            List<WebElement> oldPriceElements = element.findElements(By.cssSelector("[class=\"ProductCardActions_oldPrice__d7vDY\"]"));
                            if (!oldPriceElements.isEmpty()) {
                                oldPrice = oldPriceElements.get(0).getText();
                            }

                            // Текущая цена
                            String price = null;
                            List<WebElement> priceElements = element
                                    .findElement(By.cssSelector("[class=\"ProductCard_content__EjT48\"]"))
                                    .findElements(By.xpath(".//span[contains(text(),'₽')]"));
                            if (!priceElements.isEmpty()) {
                                price = priceElements.get(0).getText();
                            }

                            Product product = Product.builder()
                                    .title(title)
                                    .oldPriceStr(oldPrice)  // Старая цена (может быть null)
                                    .newPriceStr(price)     // Текущая цена
                                    .volume(volume)
                                    .ref(href)
                                    .shop(Shop.SAMOKAT)
                                    .build();

                            products.add(product);
                        }
                    } catch (Exception e) {
                        log.debug("Error parsing Samokat product: {}", e.getMessage());
                    }
                }
            }

            log.info("Samokat parsing completed. Found {} products", products.size());
            return products;

        } finally {
            driver.quit();
        }
    }
}
