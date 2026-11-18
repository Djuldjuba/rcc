package io.student.rococo.data.logging;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.StdoutLogger;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import org.apache.commons.lang3.StringUtils;

public class AllureAppender extends StdoutLogger {

    private final String templateName = "sql-attachment.ftl";
    private final AttachmentProcessor<AttachmentData> attachmentProcessor = new DefaultAttachmentProcessor();

    @Override
    public void logSQL(int connectionId, String now, long elapsed, Category category,
                       String prepared, String sql, String url) {
        if (StringUtils.isNoneEmpty(sql)) {
            String dbName = "unknown";
            try {
                if (url != null) {
                    dbName = StringUtils.substringBetween(url, "3306/", "?");
                    if (dbName == null) {
                        dbName = StringUtils.substringAfterLast(url, "/");
                    }
                }
            } catch (Exception ignored) {
            }

            String title = sql.trim().split("\\s+")[0].toUpperCase() + " query to: " + dbName;

            SqlAttachmentData attachmentData = new SqlAttachmentData(
                    title,
                    SqlFormatter.of(Dialect.MySql).format(sql)
            );

            attachmentProcessor.addAttachment(
                    attachmentData,
                    new FreemarkerAttachmentRenderer(templateName)
            );
        }
    }
}