package omsu.kafka;

import omsu.dto.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class KafkaLogProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaLogProducer.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final int MAX_BATCH_SIZE = 100;
    private static final int QUEUE_WARNING_THRESHOLD = 8000;
    private static final int STATS_LOG_INTERVAL_MINUTES = 5;

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;
    private final BlockingQueue<LogEvent> fallbackQueue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r, "kafka-producer-retry");
                t.setDaemon(true);
                return t;
            }
    );

    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong failureCounter = new AtomicLong(0);
    private final AtomicLong retrySuccessCounter = new AtomicLong(0);
    private final AtomicLong retryFailureCounter = new AtomicLong(0);

    @Value("${kafka.topic.grpc-logs:grpc-logs}")
    private String topic;

    @Value("${kafka.producer.fallback-dir:./kafka-fallback}")
    private String fallbackDir;

    @Value("${kafka.producer.retry-interval:5000}")
    private long retryInterval;

    @Value("${kafka.producer.max-retry-attempts:3}")
    private int maxRetryAttempts;

    public KafkaLogProducer(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void init() throws IOException {
        log.info("Initializing KafkaLogProducer for topic: {}", topic);

        // Создаем директорию для fallback
        Path path = Paths.get(fallbackDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("Created fallback directory: {}", fallbackDir);
        }

        // Запускаем фоновый ретрай
        scheduler.scheduleAtFixedRate(
                this::retryFailedMessages,
                retryInterval,
                retryInterval,
                TimeUnit.MILLISECONDS
        );

        // Логируем статус
        scheduler.scheduleAtFixedRate(
                this::logStats,
                STATS_LOG_INTERVAL_MINUTES,
                STATS_LOG_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("KafkaLogProducer initialized successfully");
    }

    public void sendLog(LogEvent logEvent) {
        if (logEvent == null) {
            log.warn("Attempted to send null LogEvent");
            return;
        }

        // Проверяем, не переполнен ли буфер
        if (isBufferNearlyFull()) {
            log.warn("Kafka buffer nearly full, using fallback for: {}", logEvent.method());
            if (!fallbackQueue.offer(logEvent)) {
                log.error("Fallback queue is full, saving to file immediately");
                saveToFile(logEvent, "overflow");
            }
            return;
        }

        try {
            CompletableFuture<SendResult<String, LogEvent>> future =
                    kafkaTemplate.send(topic, logEvent.request(), logEvent);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    handleFailure(throwable, logEvent);
                } else {
                    handleSuccess(result, logEvent);
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error sending log: {}", e.getMessage(), e);
            // Сохраняем в fallback
            if (!fallbackQueue.offer(logEvent)) {
                saveToFile(logEvent, "error");
            }
        }
    }

    private void handleSuccess(SendResult<String, LogEvent> result, LogEvent logEvent) {
        var metadata = result.getRecordMetadata();
        successCounter.incrementAndGet();

        if (log.isDebugEnabled()) {
            log.debug("Log sent: topic={}, partition={}, offset={}, method={}",
                    metadata.topic(), metadata.partition(), metadata.offset(),
                    logEvent.method());
        }
    }

    private void handleFailure(Throwable failure, LogEvent logEvent) {
        failureCounter.incrementAndGet();
        log.error("Failed to send log: method={}, error={}",
                logEvent.method(), failure.getMessage());

        // Добавляем в очередь для ретрая с метаданными о попытках
        if (!fallbackQueue.offer(logEvent)) {
            log.error("Fallback queue is full, saving to file");
            saveToFile(logEvent, "fallback");
        }
    }

    private void retryFailedMessages() {
        if (fallbackQueue.isEmpty()) {
            return;
        }

        List<LogEvent> batch = new ArrayList<>();
        fallbackQueue.drainTo(batch, MAX_BATCH_SIZE);

        if (batch.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed messages", batch.size());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (LogEvent event : batch) {
            CompletableFuture<Void> future = kafkaTemplate
                    .send(topic, event.request(), event)
                    .handle((result, throwable) -> {
                        if (throwable != null) {
                            // Неудачный ретрай
                            log.error("Retry failed for: {}", event.method());
                            retryFailureCounter.incrementAndGet();

                            // Пробуем снова добавить в очередь, если не превышен лимит
                            if (!fallbackQueue.offer(event)) {
                                saveToFile(event, "retry-failed");
                            }
                        } else {
                            // Успешный ретрай
                            log.debug("Retry successful for: {}", event.method());
                            retrySuccessCounter.incrementAndGet();
                            successCounter.incrementAndGet();

                            if (log.isDebugEnabled()) {
                                var metadata = result.getRecordMetadata();
                                log.debug("Retry sent: topic={}, partition={}, offset={}",
                                        metadata.topic(), metadata.partition(), metadata.offset());
                            }
                        }
                        return null;
                    });

            futures.add(future);
        }

        // Ожидаем завершения всех ретраев (не блокирующим образом)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    log.error("Error during batch retry processing", throwable);
                    return null;
                });
    }

    private void saveToFile(LogEvent event, String reason) {
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String filename = String.format("%s/%s_%s_%d.log",
                    fallbackDir,
                    reason,
                    timestamp,
                    System.nanoTime() // Используем nanoTime для уникальности
            );

            // Создаем директорию если её нет
            Path parentPath = Paths.get(filename).getParent();
            if (parentPath != null && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
                writer.write(String.format("[%s] method=%s, request=%s, response=%s%n",
                        LocalDateTime.now(),
                        event.method() != null ? event.method() : "unknown",
                        event.request() != null ? event.request() : "",
                        event.response() != null ? event.response() : ""
                ));
            }

            log.info("Saved to fallback file: {}", filename);
        } catch (IOException e) {
            log.error("Failed to save fallback file for event: {}", event, e);
        }
    }

    private boolean isBufferNearlyFull() {
        int queueSize = fallbackQueue.size();
        int capacity = fallbackQueue.remainingCapacity() + queueSize;

        // Если заполнено более 80% или превышен порог
        return queueSize > QUEUE_WARNING_THRESHOLD ||
                (capacity > 0 && (double) queueSize / capacity > 0.8);
    }

    private void logStats() {
        log.info("Kafka producer stats: success={}, failure={}, retrySuccess={}, retryFailure={}, queue={}",
                successCounter.get(),
                failureCounter.get(),
                retrySuccessCounter.get(),
                retryFailureCounter.get(),
                fallbackQueue.size()
        );
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down KafkaLogProducer");

        // Пытаемся отправить все оставшиеся сообщения
        if (!fallbackQueue.isEmpty()) {
            log.info("Processing {} remaining messages", fallbackQueue.size());
            retryFailedMessages();

            // Даем время на отправку
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Флешим и закрываем
        kafkaTemplate.flush();

        // Graceful shutdown
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("Scheduler did not terminate");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("KafkaLogProducer shut down. Final stats: success={}, failure={}",
                successCounter.get(), failureCounter.get());
    }

    // Метод для ручного восстановления из файлов
    public void recoverFromFiles() throws IOException {
        Path dir = Paths.get(fallbackDir);
        if (!Files.exists(dir)) {
            log.warn("Fallback directory does not exist: {}", fallbackDir);
            return;
        }

        List<Path> files;
        try (var stream = Files.list(dir)) {
            files = stream
                    .filter(p -> p.toString().endsWith(".log"))
                    .toList();
        }

        if (files.isEmpty()) {
            log.info("No fallback files found");
            return;
        }

        log.info("Found {} fallback files to recover", files.size());
        int recoveredCount = 0;

        for (Path file : files) {
            try {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    try {
                        LogEvent event = parseLogEvent(line);
                        if (event != null) {
                            sendLog(event);
                            recoveredCount++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse line: {}", line, e);
                    }
                }

                // Удаляем файл после успешного восстановления
                Files.delete(file);
                log.info("Processed and deleted: {}", file.getFileName());

            } catch (IOException e) {
                log.error("Failed to process file: {}", file, e);
            }
        }

        log.info("Recovery complete. Recovered {} messages", recoveredCount);
    }

    // Парсинг строки лога
    private LogEvent parseLogEvent(String line) {
        try {
            // Формат: [2024-01-01T10:00:00] method=someMethod, request=..., response=...
            // Упрощенный парсинг - в реальности нужно использовать более надежный метод
            String[] parts = line.split(", ");
            String method = null;
            String request = null;
            String response = null;

            for (String part : parts) {
                if (part.startsWith("method=")) {
                    method = part.substring(7);
                } else if (part.startsWith("request=")) {
                    request = part.substring(8);
                } else if (part.startsWith("response=")) {
                    response = part.substring(9);
                }
            }

            if (method != null) {
                return new LogEvent(method, request, response);
            }
        } catch (Exception e) {
            log.error("Failed to parse log line: {}", line, e);
        }
        return null;
    }

    // Дополнительный метод для получения статистики
    public String getStats() {
        return String.format("Success: %d, Failure: %d, RetrySuccess: %d, RetryFailure: %d, Queue: %d",
                successCounter.get(),
                failureCounter.get(),
                retrySuccessCounter.get(),
                retryFailureCounter.get(),
                fallbackQueue.size()
        );
    }
}