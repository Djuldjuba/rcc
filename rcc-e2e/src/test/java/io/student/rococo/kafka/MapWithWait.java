package io.student.rococo.kafka;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MapWithWait<K, V> {

    private final Map<K, SyncObject> store = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        store.computeIfAbsent(key, SyncObject::new).put(value);
    }

    public V get(K key, long maxWaitTimeMs) throws InterruptedException {
        SyncObject syncObject = store.computeIfAbsent(key, SyncObject::new);
        return syncObject.latch.await(maxWaitTimeMs, TimeUnit.MILLISECONDS)
                ? syncObject.value
                : null;
    }

    private final class SyncObject {
        private final CountDownLatch latch;
        private final K key;
        private V value;

        private SyncObject(K key) {
            this.key = key;
            this.latch = new CountDownLatch(1);
        }

        private synchronized void put(V value) {
            if (latch.getCount() != 0L) {
                this.value = value;
                latch.countDown();
            }
        }
    }
}