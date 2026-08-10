package com.sleepycat.berkeley.spring.boot;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Additional tests for {@link BerkeleyTemplate} to cover otherMethod and edge cases.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyTemplateOtherMethodTest {

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
    void writeToDatabaseAndReadBack() {
        template.writeToDatabase("testKey", "testValue", true);
        String result = template.readFromDatabase("testKey");
        assertThat(result).isEqualTo("testValue");
    }

    @Test
    void writeToDatabaseNoOverwriteExistingKey() {
        template.writeToDatabase("dupKey", "first", false);
        template.writeToDatabase("dupKey", "second", false);
        // The second write should not overwrite
        String result = template.readFromDatabase("dupKey");
        assertThat(result).isEqualTo("first");
    }

    @Test
    void readFromDatabaseNonExistentKey() {
        String result = template.readFromDatabase("doesNotExist");
        assertThat(result).isEmpty();
    }

    @Test
    void deleteFromDatabaseExistingKey() {
        template.writeToDatabase("toDelete", "value", true);
        template.deleteFromDatabase("toDelete");
        String result = template.readFromDatabase("toDelete");
        assertThat(result).isEmpty();
    }

    @Test
    void getAllFromDatabaseWithData() throws Exception {
        template.writeToDatabase("x", "1", true);
        template.writeToDatabase("y", "2", true);
        var keys = template.getAllFromDatabase();
        assertThat(keys).contains("x", "y");
    }

    @Test
    void getDatabaseReturnsSetDatabase() {
        assertThat(template.getDatabase()).isEqualTo(database);
    }

    @Test
    void setDatabaseChangesDatabase() {
        Database newDb = environment.openDatabase(null, "newdb", new DatabaseConfig().setAllowCreate(true));
        template.setDatabase(newDb);
        assertThat(template.getDatabase()).isEqualTo(newDb);
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
