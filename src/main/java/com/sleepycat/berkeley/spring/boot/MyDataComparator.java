package com.sleepycat.berkeley.spring.boot;
/*
 * Copyright (c) 2010-2020, wandalong (hnxyhcwdl1003@163.com).
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

import java.io.UnsupportedEncodingException;
import java.util.Comparator;

 

@SuppressWarnings("rawtypes")
public class MyDataComparator implements Comparator {


    public MyDataComparator() {}
 

    public int compare(Object d1, Object d2) {

 

        byte[] b1 = (byte[])d1;

        byte[] b2 = (byte[])d2;

 

        try {
			String s1 = new String(b1, "UTF-8");

			String s2 = new String(b2, "UTF-8");

			return s1.compareTo(s2);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return 0;

    }

}

 