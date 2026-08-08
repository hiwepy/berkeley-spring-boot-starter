package com.sleepycat.berkeley.spring.boot;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.sleepycat.berkeley.spring.boot.hooks.BerkeleyShutdownHook;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.dbi.RepConfigProxy;
import com.sleepycat.je.rep.RepInternal;
import com.sleepycat.je.rep.ReplicationConfig;

/**
 * Spring Boot auto-configuration for the Berkeley DB (JE) embedded key/value store.
 * <p>Berkeley DB is an embedded database suited to managing very large volumes (up to
 * 256&nbsp;TB) of simple data. Records are stored and managed as key/value pairs where
 * keys may be duplicated and values may be of arbitrary type; the underlying storage is
 * implemented with B+ trees or similar algorithms.</p>
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = BerkeleyProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({ BerkeleyProperties.class })
public class BerkeleyAutoConfiguration implements InitializingBean, ResourceLoaderAware {

	@Autowired
	private BerkeleyProperties properties;

	private ResourceLoader resourceLoader;

	/**
	 * Builds the Berkeley DB environment configuration from the bound properties.
	 * @return the configured environment configuration
	 */
    public EnvironmentConfig configEnvironment(){

    	EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(properties.getAllowCreate());//如果设置了true则表示当数据库环境不存在时候重新创建一个数据库环境，默认为false.
        envConfig.setTransactional(properties.getTransactional());//事务支持,如果为true，则表示当前环境支持事务处理，默认为false，不支持事务处理。
        envConfig.setReadOnly(properties.getReadOnly());//是否以只读方式打开，默认为false.
        envConfig.setCachePercent(50);//设置当前环境能够使用的RAM占整个JVM百分比
        envConfig.setCacheSize(102400);//设置当前环境能使用的最大RAM,单位为byte

        return envConfig;
    }

    /**
     * Creates a default replication configuration proxy.
     * @return a new replication configuration proxy
     */
    public RepConfigProxy repConfigProxy(){
    	return new ReplicationConfig();
    }

    /**
     * Creates the Berkeley DB {@link Environment} using the resolved environment home
     * resource together with the supplied environment and replication configurations.
     * @param repConfig the replication configuration
     * @param envConfig the environment configuration
     * @param repConfigProxy the replication configuration proxy
     * @return the created database environment
     * @throws DatabaseException if the environment cannot be opened
     * @throws IOException if the environment home cannot be resolved
     */
    public Environment environment(ReplicationConfig repConfig,EnvironmentConfig envConfig, RepConfigProxy repConfigProxy) throws DatabaseException, IOException{

    	Resource resource = resourceLoader.getResource(properties.getEnvHome());

    	RepInternal.createInternalEnvHandle(resource.getFile(), repConfig, envConfig);

    	RepInternal.createDetachedEnv(resource.getFile(), repConfig, envConfig);

        Environment myDbEnvironment = new Environment(resource.getFile(), envConfig);

        return myDbEnvironment;

    }

    /** Class catalog used for serialised object storage. */
    protected StoredClassCatalog catalog;
    /** The main Berkeley DB database. */
    protected Database database;
    /** Name of the class catalog database. */
    private static final String CLASS_CATALOG="java_class_catalog";
    /** Database used to store the class catalog. */
    protected Database catalogDatabase;





    /**
     * Opens the class catalog database used to store Java class metadata for
     * serialised bindings.
     * @param myDbEnvironment the database environment to open within
     * @return the opened catalog database
     */
    public Database catalogDatabase(Environment myDbEnvironment){

    	//配置创建完环境对象后，可以用它创建数据库
    	DatabaseConfig catalogDBConfig = properties.clone();
        catalogDBConfig.setAllowCreate(true);//如果设置了true则表示当数据库不存在时候重新创建一个数据库，默认为false.
        catalogDBConfig.setTransactional(true);//事务支持,如果为true，则表示当前数据库支持事务处理，默认为false，不支持事务处理。

        /*
        dbConfig.setBtreeComparator();//设置用于Btree比较的比较器，通常是用来排序
        dbConfig.setDuplicateComparator();//设置用来比较一个key有两个不同值的时候的大小比较器。
        dbConfig.setSortedDuplicates(true);//设置一个key是否允许存储多个值，true代表允许，默认false.
        dbConfig.setExclusiveCreate(true);//以独占的方式打开，也就是说同一个时间只能有一实例打开这个database。
        */

        Database catalogDatabase = myDbEnvironment.openDatabase(null, properties.getCatalogDatabaseName(), properties);

        System.out.println(catalogDatabase.getDatabaseName());

        return catalogDatabase;

    }

    /**
     * Opens the stored class catalog backed by the catalog database.
     * @return the stored class catalog
     */
    public StoredClassCatalog catalog(){
    	return new StoredClassCatalog(catalogDatabase);
    }


    /**
     * Opens the main Berkeley DB database within the given environment.
     * @param myDbEnvironment the database environment to open within
     * @return the opened database
     */
    public Database berkeleyDatabase(Environment myDbEnvironment){

    	//配置创建完环境对象后，可以用它创建数据库
    	DatabaseConfig dbConfig = properties.clone();
        dbConfig.setAllowCreate(true);//如果设置了true则表示当数据库不存在时候重新创建一个数据库，默认为false.
        dbConfig.setTransactional(true);//事务支持,如果为true，则表示当前数据库支持事务处理，默认为false，不支持事务处理。
        dbConfig.setReadOnly(false);//是否以只读方式打开，默认为false.

        /*
        dbConfig.setBtreeComparator();//设置用于Btree比较的比较器，通常是用来排序
        dbConfig.setDuplicateComparator();//设置用来比较一个key有两个不同值的时候的大小比较器。
        dbConfig.setSortedDuplicates(true);//设置一个key是否允许存储多个值，true代表允许，默认false.
        dbConfig.setExclusiveCreate(true);//以独占的方式打开，也就是说同一个时间只能有一实例打开这个database。
        */

        Database myDatabase = myDbEnvironment.openDatabase(null, properties.getDatabaseName(), dbConfig);

        System.out.println(myDatabase.getDatabaseName());

        return myDatabase;

    }

    /** The main database instance. */
    Database myDatabase;
    /** The database environment instance. */
    Environment myDbEnvironment;

	/**
	 * Registers a JVM shutdown hook that cleanly closes the database, class catalog and
	 * environment when the application exits.
	 * @throws Exception if the shutdown hook cannot be registered
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		/**
		 * 应用退出时，要调用shutdown来清理资源，关闭网络连接，从MetaQ服务器上注销自己
		 * 注意：我们建议应用在JBOSS、Tomcat等容器的退出钩子里调用shutdown方法
		 */
		Runtime.getRuntime().addShutdownHook(new BerkeleyShutdownHook(myDatabase, catalog, myDbEnvironment));


	}

	/**
	 * Sets the resource loader used to resolve the environment home location.
	 * @param resourceLoader the resource loader
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
