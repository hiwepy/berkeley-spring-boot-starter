package com.sleepycat.berkeley.spring.boot;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage-focused tests for {@link Cursor}.
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
class CursorCoverageTest {

    @TempDir
    File tempDir;

    private Cursor cursor;

    @BeforeEach
    void setUp() {
        cursor = new Cursor();
        setField(cursor, "envDir", tempDir.getAbsolutePath());
        cursor.configEnvironment();
        cursor.createDatabase();
    }

    @AfterEach
    void tearDown() {
        cursor.closeDatabase();
    }

    @Test
    void configEnvironmentCreatesEnv() {
        Object env = getField(cursor, "myDbEnvironment");
        assertThat(env).isNotNull();
    }

    @Test
    void createDatabaseCreatesDbAndCursor() {
        assertThat(getField(cursor, "myDatabase")).isNotNull();
        assertThat(getField(cursor, "myCursor")).isNotNull();
    }

    @Test
    void addDataByCursor() throws UnsupportedEncodingException {
        cursor.addDataByCursor("key1", "value1");
        cursor.addDataByCursor("key2", "value2");
    }

    @Test
    void searchByCursor() throws UnsupportedEncodingException {
        cursor.addDataByCursor("searchKey", "searchValue");
        cursor.searchByCursor("searchKey", "searchValue");
    }

    @Test
    void searchByCursorNotFound() throws UnsupportedEncodingException {
        cursor.addDataByCursor("existing", "data");
        cursor.searchByCursor("nonexistent", "data");
    }

    @Test
    void searchMultipleByCursor() throws UnsupportedEncodingException {
        cursor.addDataByCursor("multi", "val1");
        cursor.searchMultipleByCursor("multi");
    }

    @Test
    void getAllByCursorWithData() throws UnsupportedEncodingException {
        cursor.addDataByCursor("a", "1");
        cursor.addDataByCursor("b", "2");
        cursor.getAllByCursor();
    }

    @Test
    void changeCursor() throws UnsupportedEncodingException {
        cursor.addDataByCursor("changeKey", "oldValue");
        cursor.changeCursor("changeKey");
    }

    @Test
    void deleteDataByCursor() throws UnsupportedEncodingException {
        cursor.addDataByCursor("delKey", "delValue");
        cursor.deleteDataByCursor("delKey");
    }

    @Test
    void closeDatabaseHandlesNonNull() {
        cursor.closeDatabase();
        // Create new one to avoid double-close in tearDown
        cursor = new Cursor();
        setField(cursor, "envDir", tempDir.getAbsolutePath());
    }

    @Test
    void getAllByCursorWithMultipleEntries() throws UnsupportedEncodingException {
        cursor.addDataByCursor("x", "1");
        cursor.addDataByCursor("y", "2");
        cursor.addDataByCursor("z", "3");
        cursor.getAllByCursor();
    }

    @Test
    void searchMultipleByCursorWithData() throws UnsupportedEncodingException {
        cursor.addDataByCursor("multi", "val1");
        cursor.addDataByCursor("multi2", "val2");
        cursor.searchMultipleByCursor("multi");
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

    private Object getField(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
