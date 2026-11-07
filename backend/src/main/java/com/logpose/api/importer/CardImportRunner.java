package com.logpose.api.importer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * Runs the import at startup when {@code logpose.import.enabled=true}, e.g.
 * {@code mvnw spring-boot:run -Dspring-boot.run.arguments=--logpose.import.enabled=true}.
 */
@Component
@ConditionalOnBooleanProperty("logpose.import.enabled")
public class CardImportRunner implements ApplicationRunner {

    private final CardImporter importer;
    private final ImportProperties properties;

    public CardImportRunner(CardImporter importer, ImportProperties properties) {
        this.importer = importer;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        importer.importFrom(properties.source());
    }
}
