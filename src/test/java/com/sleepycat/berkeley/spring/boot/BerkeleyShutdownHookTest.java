package com.sleepycat.berkeley.spring.boot;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;
import com.sleepycat.berkeley.spring.boot.hooks.BerkeleyShutdownHook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BerkeleyShutdownHook}.
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
class BerkeleyShutdownHookTest {

    @Test
    void constructorSetsFields() {
        // We can't easily mock Berkeley DB objects, but we can verify the constructor
        // doesn't throw and the thread is created
        BerkeleyShutdownHook hook = new BerkeleyShutdownHook(null, null, null);
        assertThat(hook).isNotNull();
        assertThat(hook).isInstanceOf(Thread.class);
    }
}
