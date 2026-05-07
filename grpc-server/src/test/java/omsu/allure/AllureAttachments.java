package omsu.allure;

import io.qameta.allure.Allure;

import java.util.List;
import java.util.Map;

public final class AllureAttachments {
    private AllureAttachments() {}

    public static void attachText(String name, String content) {
         Allure.addAttachment(name, content);
    }

    public static void attachTable(List<Map<String, Object>> rows, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title != null ? title + "\n" : "");
        rows.forEach(row -> {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getValue()).append(",");
            };
            sb.append("\n");
        });
        Allure.addAttachment("Table Data", "text/csv", sb.toString());
    }

}
