package com.stock.price_notification.controller;

import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux; // ✅ Flux 임포트 추가
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

@RestController
@Slf4j
public class StockPriceController {

    // 공유 가능한 핫 퍼블리셔 생성
    private final Flux<String> stockPriceStream;

    public StockPriceController() {

        /**
         * 생성자에서 데이터 스트림을 생성하는 이유
         * 1) 클래스가 생성되는 시점에 딱 1번만 데이터 스트림을 생성
         * /stocks 요청마다 데이터 스트림이 생성되면 비효율적
         *
         * 2) 서버의 생애주기와 데이터 스트림의 생애주기가 일치해야 하기 때문에
         * */
        // Sinks를 통해 데이터를 생성하고 브로드캐스트
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 주기적으로 데이터를 생성
        Flux.interval(Duration.ofSeconds(1))
            .map(tick -> generateStockPrice()) // 주식 가격 생성
            .subscribe(sink::tryEmitNext);    // 데이터를 Sinks로 전달

        // 공유 가능한 Flux 생성
        stockPriceStream = sink.asFlux();
    }

    @GetMapping(value = "/stocks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStockPrices() {
        // 모든 클라이언트에게 동일한 데이터를 스트리밍 리턴
        return stockPriceStream;
    }

    private String generateStockPrice() {
        Random random = new Random();
        double price = 100 + (random.nextDouble() * 20); // 100~120 사이 랜덤 가격
        return "{ \"symbol\": \"AAPL\", \"price\": " + String.format("%.2f", price) + " }";
    }

    @GetMapping(value = "/stocks/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamStockPrices() {

        /** HTTP응답이 MediaType.TEXT_EVENT_STREAM_VALUE => SSE를 활성화 하는 방식
            Flux는 비동기 데이터 스트림을 제공.
            1초마다 비동기적으로 데이터 생성하고, 지속적으로 클라이언트에 제공
        */

        log.info("stock 주식가격 조회에 진입");

        Random random = new Random();
        return Flux.interval(Duration.ofSeconds(1)) // 1초마다 이벤트 생성
            .map(tick -> {
                double price = 100 + (random.nextDouble() * 20); // 100~120 사이 랜덤 가격
                return "{ \"symbol\": \"AAPL\", \"price\": " + String.format("%.2f", price) + " }"; // 순수 JSON 데이터 전송
            });
    }

}
