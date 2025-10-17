package org.example.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DriverUtils {

    @Value("${app.selenium.headless:true}")
    private boolean headless;

    @Value("${app.selenium.window-size:1920,1080}")
    private String windowSize;

    public static WebDriver getDriver() {
        // В production используйте WebDriverManager для автоматической загрузки драйверов
        ChromeOptions options = new ChromeOptions();

        // Настройки для production/Docker
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");

        // Headless режим для сервера
        String headlessProperty = System.getProperty("selenium.headless", "true");
        if ("true".equals(headlessProperty)) {
            options.addArguments("--headless");
        }

        // User-Agent для обхода блокировок
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        return new ChromeDriver(options);
    }
}
