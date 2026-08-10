package com.sleepycat.berkeley.spring.boot;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.LockMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BerkeleyTemplate}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyTemplateTest {

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
    void writeToDatabaseWithOverwrite() {
        template.writeToDatabase("key1", "value1", true);
        String result = template.readFromDatabase("key1");
        assertThat(result).isEqualTo("value1");
    }

    @Test
    void writeToDatabaseWithoutOverwrite() {
        template.writeToDatabase("key2", "value2", false);
        String result = template.readFromDatabase("key2");
        assertThat(result).isEqualTo("value2");
    }

    @Test
    void readFromDatabaseReturnsEmptyForMissingKey() {
        String result = template.readFromDatabase("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test
    void deleteFromDatabase() {
        template.writeToDatabase("key3", "value3", true);
        template.deleteFromDatabase("key3");
        String result = template.readFromDatabase("key3");
        assertThat(result).isEmpty();
    }

    @Test
    void getAllFromDatabaseReturnsKeys() throws UnsupportedEncodingException {
        template.writeToDatabase("a", "1", true);
        template.writeToDatabase("b", "2", true);
        ArrayList<String> keys = template.getAllFromDatabase();
        assertThat(keys).contains("a", "b");
    }

    @Test
    void writePrimitiveDatabase() {
        template.writePrimitiveDatabase("primKey", "primValue");
        // Read it back using regular read
        String result = template.readFromDatabase("primKey");
        assertThat(result).isNotEmpty();
    }

    @Test
    void readPrimitiveDatabase() {
        template.writePrimitiveDatabase("primKey2", "primValue2");
        // This prints to stdout, just verify no exception
        template.readPrimitiveDatabase("primKey2");
    }

    @Test
    void getAndSetDatabase() {
        BerkeleyTemplate t = new BerkeleyTemplate();
        t.setDatabase(database);
        assertThat(t.getDatabase()).isEqualTo(database);
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
