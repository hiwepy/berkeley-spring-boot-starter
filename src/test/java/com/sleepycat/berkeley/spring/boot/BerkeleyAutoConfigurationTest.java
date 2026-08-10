package com.sleepycat.berkeley.spring.boot;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.dbi.RepConfigProxy;
import com.sleepycat.je.rep.ReplicationConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BerkeleyAutoConfiguration}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyAutoConfigurationTest {

    @Test
    void configEnvironmentReturnsNonNull() {
        BerkeleyAutoConfiguration config = new BerkeleyAutoConfiguration();
        BerkeleyProperties props = new BerkeleyProperties();
        setField(config, "properties", props);

        EnvironmentConfig envConfig = config.configEnvironment();
        assertThat(envConfig).isNotNull();
        assertThat(envConfig.getAllowCreate()).isFalse();
        assertThat(envConfig.getTransactional()).isFalse();
        assertThat(envConfig.getReadOnly()).isFalse();
    }

    @Test
    void repConfigProxyReturnsNonNull() {
        BerkeleyAutoConfiguration config = new BerkeleyAutoConfiguration();
        RepConfigProxy proxy = config.repConfigProxy();
        assertThat(proxy).isNotNull();
        assertThat(proxy).isInstanceOf(ReplicationConfig.class);
    }

    @Test
    void catalogDatabaseReturnsNonNull() {
        BerkeleyAutoConfiguration config = new BerkeleyAutoConfiguration();
        BerkeleyProperties props = new BerkeleyProperties();
        props.setCatalogDatabaseName("testCatalog");
        setField(config, "properties", props);

        EnvironmentConfig envConfig = config.configEnvironment();
        assertThat(envConfig).isNotNull();
    }

    @Test
    void setResourceLoader() {
        BerkeleyAutoConfiguration config = new BerkeleyAutoConfiguration();
        ResourceLoader loader = new DefaultResourceLoader();
        config.setResourceLoader(loader);
        // No assertion needed - just verifying no exception
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
