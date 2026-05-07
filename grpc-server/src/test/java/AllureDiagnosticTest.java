import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AllureDiagnosticTest {

    @Test
    void diagnoseAllure() {
        System.out.println("=== ALLURE DIAGNOSTIC ===");

        // 1. Проверяем системные свойства
        System.out.println("allure.results.directory: " + System.getProperty("allure.results.directory"));
        System.out.println("allure.model.version: " + System.getProperty("allure.model.version"));

        // 2. Пытаемся создать вложение
        System.out.println("Creating attachment...");
        Allure.addAttachment("Diagnostic Text", "text/plain", "This is a test attachment");
        System.out.println("Attachment created");

        // 3. Проверяем, есть ли listener
        System.out.println("Allure lifecycle: " + Allure.getLifecycle());

        // 4. Смотрим, какие файлы создались в results
        String resultsDir = System.getProperty("allure.results.directory", "build/allure-results");
        System.out.println("Results directory: " + resultsDir);

        Path dir = Paths.get(resultsDir);
        if (Files.exists(dir)) {
            System.out.println("Directory exists. Files in it:");
            try {
                Files.list(dir).forEach(p -> System.out.println("  - " + p.getFileName()));
            } catch (Exception e) {
                System.out.println("Error listing files: " + e.getMessage());
            }
        } else {
            System.out.println("Directory DOES NOT EXIST: " + dir.toAbsolutePath());
        }

        System.out.println("=== END DIAGNOSTIC ===");
    }
}