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
 * Coverage-focused tests for {@link BerkeleyTemplate}.
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyTemplateCoverageTest {

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
    void writeToDatabaseOverwriteTrue() {
        template.writeToDatabase("key1", "value1", true);
        assertThat(template.readFromDatabase("key1")).isEqualTo("value1");
    }

    @Test
    void writeToDatabaseOverwriteFalse() {
        template.writeToDatabase("key2", "first", false);
        template.writeToDatabase("key2", "second", false);
        assertThat(template.readFromDatabase("key2")).isEqualTo("first");
    }

    @Test
    void readFromDatabaseReturnsEmptyForMissing() {
        assertThat(template.readFromDatabase("missing")).isEmpty();
    }

    @Test
    void deleteFromDatabaseExisting() {
        template.writeToDatabase("del", "val", true);
        template.deleteFromDatabase("del");
        assertThat(template.readFromDatabase("del")).isEmpty();
    }

    @Test
    void deleteFromDatabaseNonExisting() {
        // Covers the KEYEMPTY path
        template.deleteFromDatabase("nonexistent");
    }

    @Test
    void getAllFromDatabaseWithData() throws Exception {
        template.writeToDatabase("a", "1", true);
        template.writeToDatabase("b", "2", true);
        ArrayList<String> keys = template.getAllFromDatabase();
        assertThat(keys).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getAllFromEmptyDatabase() throws Exception {
        ArrayList<String> keys = template.getAllFromDatabase();
        assertThat(keys).isEmpty();
    }

    @Test
    void writeAndReadPrimitive() {
        template.writePrimitiveDatabase("pk", "pv");
        template.readPrimitiveDatabase("pk");
    }

    @Test
    void readPrimitiveNonExistent() {
        template.readPrimitiveDatabase("nokey");
    }


    @Test
    void getAndSetDatabase() {
        BerkeleyTemplate t = new BerkeleyTemplate();
        t.setDatabase(database);
        assertThat(t.getDatabase()).isSameAs(database);
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
