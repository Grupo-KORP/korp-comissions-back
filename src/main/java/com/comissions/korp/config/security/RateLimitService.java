package com.comissions.korp.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 5;
    private static final Duration REFILL_DURATION = Duration.ofHours(1);

    public boolean tryConsume(String key) {
        Bucket bucket = cache.computeIfAbsent(key, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, Refill.intervally(MAX_REQUESTS, REFILL_DURATION));
        return Bucket.builder().addLimit(limit).build();
    }
}