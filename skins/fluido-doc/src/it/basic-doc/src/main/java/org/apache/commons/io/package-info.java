/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <p>
 * This package defines utility classes for working with streams, readers,
 * writers and files. The most commonly used classes are described here:
 * </p>
 * <p>
 * <b>IOUtils</b> is the most frequently used class.
 * It provides operations to read, write, copy and close streams.
 * </p>
 * <p>
 * <b>FileUtils</b> provides operations based around the JDK File class.
 * These include reading, writing, copying, comparing and deleting.
 * </p>
 * <p>
 * <b>FilenameUtils</b> provides utilities based on filenames.
 * This utility class manipulates filenames without using File objects.
 * It aims to simplify the transition between Windows and Unix.
 * Before using this class however, you should consider whether you should
 * be using File objects.
 * </p>
 * <p>
 * <b>FileSystemUtils</b> allows access to the filing system in ways the JDK
 * does not support. At present this allows you to get the free space on a drive.
 * </p>
 * <p>
 * <b>EndianUtils</b> swaps data between Big-Endian and Little-Endian formats.
 * </p>
 */
package org.apache.commons.io;
