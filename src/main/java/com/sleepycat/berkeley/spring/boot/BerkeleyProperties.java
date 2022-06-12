/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.sleepycat.je.DatabaseConfig;

@ConfigurationProperties(BerkeleyProperties.PREFIX)
@Data
public class BerkeleyProperties extends DatabaseConfig  {

	public static final String PREFIX = "berkeley.db";

	private String homeDir; //是数据库存放的目录
	private String envHome;
	private String envDir = "dbEnv";//用户指定目录，存放数据文件和日志文件
	private String databaseName = "tt";//数据库名称
	private String catalogDatabaseName = "tt";//数据库名称

}
