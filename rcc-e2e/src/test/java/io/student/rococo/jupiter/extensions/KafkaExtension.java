package io.student.rococo.jupiter.extensions;

import io.student.rococo.kafka.KafkaService;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaExtension implements SuiteExtension {

    private static final KafkaService kafkaService = new KafkaService();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void beforeSuite(ExtensionContext context) {
        executor.submit(kafkaService);
    }

    @Override
    public void afterSuite() {
        kafkaService.shutdown();
        executor.shutdownNow();
    }
}