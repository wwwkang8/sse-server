package com.stock.price_notification.service;

import java.util.Map;
import java.util.Random;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StockPricePublisher {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    // 주식 초기 가격 설정
    private final Map<String, Double> stockPrices = Map.of(
        "AAPL", 150.0,
        "TSLA", 700.0,
        "AMZN", 3300.0,
        "GOOGL", 2800.0,
        "MSFT", 300.0
    );

    public StockPricePublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 3000) // 3초마다 실행
    public void publishStockPrice() {
        String stockSymbol = getRandomStock();
        double newPrice = generateRandomPrice(stockSymbol);

        // Redis Stream에 추가 (XADD)
        redisTemplate.opsForStream().add("stock_prices", Map.of(
            "symbol", stockSymbol,
            "price", String.format("%.2f", newPrice)
        ));

        System.out.println("Published: " + stockSymbol + " -> $" + newPrice);
    }

    private String getRandomStock() {
        Object[] stocks = stockPrices.keySet().toArray();
        return (String) stocks[random.nextInt(stocks.length)];
    }

    private double generateRandomPrice(String stockSymbol) {
        double currentPrice = stockPrices.get(stockSymbol);
        double change = (random.nextDouble() * 10 - 5); // -5% ~ +5% 변동
        double newPrice = Math.max(1.0, currentPrice + change); // 최소 1달러 유지
        return newPrice;
    }

}
