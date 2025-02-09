package com.stock.price_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PriceNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceNotificationApplication.class, args);
    }

}
