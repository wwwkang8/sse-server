package com.stock.price_notification.controller;

import java.time.Duration;
import java.util.Random;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SSEController {

    // HTTP 1.1 기반 SSE
    @GetMapping(value = "/http1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sseHttp1() {
        return generateStockPriceStream("HTTP/1.1");
    }

    // HTTP/2 기반 SSE
    @GetMapping(value = "/http2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sseHttp2() {
        return generateStockPriceStream("HTTP/2");
    }

    // 공통 데이터 스트림 생성
    private Flux<String> generateStockPriceStream(String protocol) {
        Random random = new Random();
        return Flux.interval(Duration.ofSeconds(1))
            .map(tick -> {
                double price = 100 + (random.nextDouble() * 20);
                return "data: { \"protocol\": \"" + protocol + "\", \"price\": " + String.format("%.2f", price) + " }\n\n";
            });
    }

}
