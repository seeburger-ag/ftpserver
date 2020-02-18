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

import java.util.Arrays;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.util.DateUtils;

public class LISTFileFormaterUtils
{
    /**
     * Get size
     */
    protected static String getLength(FtpFile file) {

        String initStr = "            ";
        long sz = file.getSize();
        String szStr = String.valueOf(sz);
        if (szStr.length() > initStr.length()) {
            return szStr;
        }
        return initStr.substring(0, initStr.length() - szStr.length()) + szStr;
    }

    /**
     * Get last modified date string.
     */
    protected static String getLastModified(FtpFile file) {
        return DateUtils.getUnixDate(file.getLastModified());
    }

    protected long getLastModifiedUTC(FtpFile file) {
        return file.getLastModified();
    }

    /**
     * Get permission string.
     */
    protected static char[] getPermission(FtpFile file) {
        char permission[] = new char[10];
        Arrays.fill(permission, '-');

        permission[0] = file.isDirectory() ? 'd' : '-';
        permission[1] = file.isReadable() ? 'r' : '-';
        permission[2] = file.isWritable() ? 'w' : '-';
        permission[3] = file.isDirectory() ? 'x' : '-';
        return permission;
    }
}
