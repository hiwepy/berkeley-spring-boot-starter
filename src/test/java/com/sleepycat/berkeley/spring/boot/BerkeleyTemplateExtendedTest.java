package com.sleepycat.berkeley.spring.boot;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Extended tests for {@link BerkeleyTemplate} to maximize coverage.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyTemplateExtendedTest {

    @TempDir
    File tempDir;

    private Environment environment;
    private Database database;
    private BerkeleyTemplate template;

    @BeforeEach
    void setUp() {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        environment = new Environment(tempDir, envConfig);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        database = environment.openDatabase(null, "testdb", dbConfig);

        template = new BerkeleyTemplate();
        template.setDatabase(database);
        setField(template, "myDbEnvironment", environment);
    }

    @Test
    void writeAndReadMultipleKeys() {
        for (int i = 0; i < 10; i++) {
            template.writeToDatabase("key" + i, "value" + i, true);
        }
        for (int i = 0; i < 10; i++) {
            assertThat(template.readFromDatabase("key" + i)).isEqualTo("value" + i);
        }
    }

    @Test
    void overwriteExistingKey() {
        template.writeToDatabase("overwrite", "original", true);
        template.writeToDatabase("overwrite", "updated", true);
        assertThat(template.readFromDatabase("overwrite")).isEqualTo("updated");
    }

    @Test
    void deleteNonExistentKey() {
        // Should not throw
        template.deleteFromDatabase("nonexistent");
    }

    @Test
    void getAllFromEmptyDatabase() throws Exception {
        ArrayList<String> keys = template.getAllFromDatabase();
        assertThat(keys).isEmpty();
    }

    @Test
    void writePrimitiveAndReadBack() {
        template.writePrimitiveDatabase("primKey", "primValue");
        // The value is stored using tuple binding
        String result = template.readFromDatabase("primKey");
        assertThat(result).isNotEmpty();
    }

    @Test
    void readPrimitiveFromEmptyDatabase() {
        // Should not throw even if key doesn't exist
        template.readPrimitiveDatabase("nonexistent");
    }

    @Test
    void getDatabaseReturnsCorrectInstance() {
        assertThat(template.getDatabase()).isSameAs(database);
    }

    @Test
    void setDatabaseToNewInstance() {
        Database newDb = environment.openDatabase(null, "newdb",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        template.setDatabase(newDb);
        assertThat(template.getDatabase()).isSameAs(newDb);
        newDb.close();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
