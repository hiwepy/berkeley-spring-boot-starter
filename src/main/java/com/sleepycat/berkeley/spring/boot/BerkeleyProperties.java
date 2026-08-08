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

/**
 * Configuration properties for the Berkeley DB (JE) integration, bound under the
 * {@value #PREFIX} prefix.
 * <p>Extends {@link DatabaseConfig} so that database-level options inherit directly
 * from the bound configuration.</p>
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
@ConfigurationProperties(BerkeleyProperties.PREFIX)
@Data
public class BerkeleyProperties extends DatabaseConfig  {

	/** Configuration property prefix for Berkeley DB options. */
	public static final String PREFIX = "berkeley.db";

	/** Directory where the database files are stored. */
	private String homeDir;
	/** Location of the Berkeley DB environment home. */
	private String envHome;
	/** User-defined directory holding the data and log files. */
	private String envDir = "dbEnv";
	/** Name of the main database. */
	private String databaseName = "tt";
	/** Name of the class catalog database. */
	private String catalogDatabaseName = "tt";

}
