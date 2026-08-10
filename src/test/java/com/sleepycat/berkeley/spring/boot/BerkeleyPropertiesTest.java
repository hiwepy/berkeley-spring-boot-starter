package com.sleepycat.berkeley.spring.boot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BerkeleyProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyPropertiesTest {

    @Test
    void defaultValues() {
        BerkeleyProperties props = new BerkeleyProperties();
        assertThat(props.getHomeDir()).isNull();
        assertThat(props.getEnvHome()).isNull();
        assertThat(props.getEnvDir()).isEqualTo("dbEnv");
        assertThat(props.getDatabaseName()).isEqualTo("tt");
        assertThat(props.getCatalogDatabaseName()).isEqualTo("tt");
    }

    @Test
    void setAndGetHomeDir() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setHomeDir("/tmp/bdb");
        assertThat(props.getHomeDir()).isEqualTo("/tmp/bdb");
    }

    @Test
    void setAndGetEnvHome() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setEnvHome("file:/tmp/env");
        assertThat(props.getEnvHome()).isEqualTo("file:/tmp/env");
    }

    @Test
    void setAndGetEnvDir() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setEnvDir("customEnv");
        assertThat(props.getEnvDir()).isEqualTo("customEnv");
    }

    @Test
    void setAndGetDatabaseName() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setDatabaseName("mydb");
        assertThat(props.getDatabaseName()).isEqualTo("mydb");
    }

    @Test
    void setAndGetCatalogDatabaseName() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setCatalogDatabaseName("catalog");
        assertThat(props.getCatalogDatabaseName()).isEqualTo("catalog");
    }

    @Test
    void prefixConstant() {
        assertThat(BerkeleyProperties.PREFIX).isEqualTo("berkeley.db");
    }

    @Test
    void inheritedDatabaseConfigMethods() {
        BerkeleyProperties props = new BerkeleyProperties();
        props.setAllowCreate(true);
        assertThat(props.getAllowCreate()).isTrue();

        props.setTransactional(true);
        assertThat(props.getTransactional()).isTrue();

        props.setReadOnly(true);
        assertThat(props.getReadOnly()).isTrue();
    }
}
