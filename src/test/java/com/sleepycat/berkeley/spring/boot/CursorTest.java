package com.sleepycat.berkeley.spring.boot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Cursor}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class CursorTest {

    @TempDir
    File tempDir;

    @Test
    void configEnvironmentCreatesEnvironment() {
        Cursor cursor = new Cursor();
        // Set the envDir to use temp directory
        setField(cursor, "envDir", tempDir.getAbsolutePath());
        cursor.configEnvironment();
        // Verify environment was created
        Object env = getField(cursor, "myDbEnvironment");
        assertThat(env).isNotNull();
        // Clean up
        cursor.closeDatabase();
    }

    @Test
    void createDatabaseCreatesDatabase() {
        Cursor cursor = new Cursor();
        setField(cursor, "envDir", tempDir.getAbsolutePath());
        cursor.configEnvironment();
        cursor.createDatabase();
        // Verify database and cursor were created
        Object db = getField(cursor, "myDatabase");
        Object crsr = getField(cursor, "myCursor");
        assertThat(db).isNotNull();
        assertThat(crsr).isNotNull();
        // Clean up
        cursor.closeDatabase();
    }

    @Test
    void closeDatabaseHandlesNullFields() {
        Cursor cursor = new Cursor();
        // Should not throw when fields are null
        cursor.closeDatabase();
    }

    @Test
    void closeDatabaseClosesResources() {
        Cursor cursor = new Cursor();
        setField(cursor, "envDir", tempDir.getAbsolutePath());
        cursor.configEnvironment();
        cursor.createDatabase();
        // Close should not throw
        cursor.closeDatabase();
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
