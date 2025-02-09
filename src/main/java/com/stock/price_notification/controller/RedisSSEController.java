package com.stock.price_notification.controller;


import java.io.IOException;
import java.time.Duration;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisSSEController {

    private final StringRedisTemplate redisTemplate;

    public RedisSSEController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/sse/stocks")
    public void streamStockPrices(HttpServletResponse response, @RequestParam(defaultValue = "$") String lastId) throws
        IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("data: Connected\n\n");
        response.getWriter().flush();

        while (true) {
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                .read(StreamReadOptions.empty().block(Duration.ofSeconds(5)),
                    StreamOffset.create("stock_prices", ReadOffset.from(lastId)));

            for (MapRecord<String, Object, Object> message : messages) {
                response.getWriter().write("data: " + message.getValue() + "\n\n");
                response.getWriter().flush();
                lastId = message.getId().getValue(); // 마지막으로 읽은 메시지 ID 업데이트
            }
        }
    }

}
