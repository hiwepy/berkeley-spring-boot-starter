/*
 * Copyright (c) 2010-2020, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sleepycat.berkeley.spring.boot;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.sleepycat.je.DatabaseConfig;

@ConfigurationProperties(BerkeleyProperties.PREFIX)
public class BerkeleyProperties extends DatabaseConfig  {

	public static final String PREFIX = "berkeley.db";

	/**
	 * Enable Berkeley DB.
	 */
	private boolean enabled = false;
	
	
	private String homeDir; //是数据库存放的目录
	private String envHome;
	private String envDir = "dbEnv";//用户指定目录，存放数据文件和日志文件
	private String databaseName = "tt";//数据库名称
	private String catalogDatabaseName = "tt";//数据库名称
    

	public String getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getEnvDir() {
		return envDir;
	}

	public void setEnvDir(String envDir) {
		this.envDir = envDir;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getCatalogDatabaseName() {
		return catalogDatabaseName;
	}

	public void setCatalogDatabaseName(String catalogDatabaseName) {
		this.catalogDatabaseName = catalogDatabaseName;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEnvHome() {
		return envHome;
	}

	public void setEnvHome(String envHome) {
		this.envHome = envHome;
	}
	
	
	

}
