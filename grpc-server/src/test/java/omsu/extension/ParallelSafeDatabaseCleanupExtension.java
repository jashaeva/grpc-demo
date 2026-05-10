package omsu.extension;

import org.junit.jupiter.api.extension.*;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSafeDatabaseCleanupExtension
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    // Счётчик активных тестов (потокобезопасный)
    private static final AtomicInteger activeTests = new AtomicInteger(0);

    // Флаг, что очистка уже запланирована
    private static volatile boolean cleanupScheduled = false;

    // JdbcTemplate будем лениво получать через ApplicationContext
    private static JdbcTemplate cachedJdbcTemplate;

    @Override
    public void beforeAll(ExtensionContext context) {
        // Ничего не делаем — ждём BeforeEach
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        int count = activeTests.incrementAndGet();
        System.out.println("Test started. Active tests: " + count);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        int count = activeTests.decrementAndGet();
        System.out.println("Test finished. Active tests left: " + count);

        // Если это был последний тест во всём раннере
        if (count == 0 && !cleanupScheduled) {
            cleanupScheduled = true;

            // Используем отдельный поток, чтобы не блокировать завершение
            new Thread(() -> {
                try {
                    System.out.println("Cleanup thread started, preparing to sleep...");
                    // Даём небольшую задержку, чтобы все тесты точно завершились
                    Thread.sleep(1000);
                    performFinalCleanup(context);
                } catch (Exception e) {
                    System.err.println("❌ Final cleanup failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Не используем — он вызывается для каждого тестового класса
    }

    private synchronized void performFinalCleanup(ExtensionContext context) {
        System.out.println("Starting FINAL database cleanup...");

        try {
            JdbcTemplate jdbc = getJdbcTemplate(context);

            // Получаем все таблицы в схеме public
            List<String> tables = jdbc.queryForList(
//                " SELECT table_name FROM information_schema.tables WHERE table_schema = 'inventory_schema';",
                    "SELECT tablename FROM pg_tables WHERE schemaname = 'inventory_schema'",
                    String.class
            );

            if (tables.isEmpty()) {
                System.out.println("No tables found in inventory_schema");
                return;
            }

            // Отключаем проверки внешних ключей
            jdbc.execute("SET session_replication_role = 'replica';");

            // Очищаем все таблицы
            for (String table : tables) {
                jdbc.execute("TRUNCATE TABLE \"" + table + "\" RESTART IDENTITY CASCADE;");
                System.out.println("  ✅ Truncated: " + table);
            }

            // Включаем обратно проверки
            jdbc.execute("SET session_replication_role = 'origin';");

            System.out.println("FINAL cleanup completed SUCCESSfully!");

        } catch (Exception e) {
            System.err.println("Cleanup FAILED: " + e.getMessage());
            throw new RuntimeException("Database cleanup failed", e);
        }
    }

    private JdbcTemplate getJdbcTemplate(ExtensionContext context) {
        if (cachedJdbcTemplate == null) {
            ApplicationContext appContext = SpringExtension.getApplicationContext(context);
            cachedJdbcTemplate = appContext.getBean(JdbcTemplate.class);
        }
        return cachedJdbcTemplate;
    }
}
