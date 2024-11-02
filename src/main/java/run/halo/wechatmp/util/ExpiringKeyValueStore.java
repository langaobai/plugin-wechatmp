package run.halo.wechatmp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@EnableScheduling
public class ExpiringKeyValueStore {
    private final Map<String, ValueWithExpiry> store = new ConcurrentHashMap<>();

    private static class ValueWithExpiry {
        String value;
        long expiryTime;

        ValueWithExpiry(String value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }

    public void put(String key, String value, long ttlInSeconds) {
        long expiryTime = System.currentTimeMillis() + ttlInSeconds * 1000;
        store.put(key, new ValueWithExpiry(value, expiryTime));
    }

    public String get(String key) {
        ValueWithExpiry entry = store.get(key);
        if (entry != null && System.currentTimeMillis() < entry.expiryTime) {
            return entry.value;
        } else {
            store.remove(key); // 清理过期的键
            return null;
        }
    }

    @Scheduled(fixedRate = 1000)
    public void cleanUpExpired() {
        long currentTime = System.currentTimeMillis();
        store.values().removeIf(entry -> currentTime >= entry.expiryTime);
    }
}
