/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ftpserver.command.impl.listing;

import java.util.List;

/***
 *
 * Format the list of files in style as WUFTPD
 * <p>
 *
 *
 * @author t.nalbantova
 */
public interface FileFormaterWUFTPD
{
    /***
     * Format the file
     * @param file
     * @return
     */
    String format(FtpFileData file);

    /***
     * Format the files in specific format - aligned columns or CF style
     * @param files
     * @param sortType
     * @return
     */
    String format(List<FtpFileData> files, Integer sortType);

    /***
     * true if format type is columnar Unix style -C
     * @return
     */
    boolean isColumnarFormat();

    /***
     * When true, filenames are with flag
     * @return
     */
    boolean isFlagFileNames();

    /***
     * True if should be added total line
     * @return
     */
    boolean allowAddTotalLine();

    /***
     * When true, name contains path relative to the working directory
     * @return
     */
    boolean isAddPath();
}
