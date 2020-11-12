package de.mmenze.corona.web.schedule;

import com.smattme.MysqlExportService;
import de.mmenze.corona.web.service.SendMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

// currently disabled
@Slf4j
@Component
public class ExportDbScheduledHandler {

    @Autowired
    private SendMailService sendMailService;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${spring.datasource.password}")
    private String dbPassword;


    // @Scheduled(cron = "0 30 4 * * *")
    public void exportDb() {
        try {
            log.debug("Exporting DB data");
            Path tempDirWithPrefix = Files.createTempDirectory("dbexport");
            Properties properties = new Properties();

            properties.setProperty(MysqlExportService.JDBC_CONNECTION_STRING, dbUrl);
            properties.setProperty(MysqlExportService.DB_USERNAME, dbUsername);
            properties.setProperty(MysqlExportService.DB_PASSWORD, dbPassword);
            properties.setProperty(MysqlExportService.TEMP_DIR, tempDirWithPrefix.toString());
            properties.setProperty(MysqlExportService.PRESERVE_GENERATED_ZIP, "true");

            MysqlExportService mysqlExportService = new MysqlExportService(properties);
            mysqlExportService.export();
            log.debug("Done exporting DB data, now sending mail");

            File file = mysqlExportService.getGeneratedZipFile();
            sendMailService.sendMessageWithAttachment(file, "export.sql");
            log.debug("Done sending mail. Export finished");
        } catch (ClassNotFoundException | SQLException | IOException e) {
            log.error("Error exporting DB data", e);
            throw new RuntimeException(e);
        }
    }

}
