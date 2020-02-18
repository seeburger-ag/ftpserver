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

import org.apache.ftpserver.ftplet.FtpFile;

public class FtpFileData
{
    private char[] permissions;
    private String linkCount;
    private String ownerName;
    private String groupName;
    private String length;
    private String lastModified;
    private long lastModifiedUTC;
    private String fileName;

    public FtpFileData(FtpFile file, String nameConstant, boolean flagFileName)
    {
        permissions = LISTFileFormaterUtils.getPermission(file);
        linkCount = String.valueOf(file.getLinkCount());
        ownerName = file.getOwnerName();
        groupName = file.getGroupName();
        length = LISTFileFormaterUtils.getLength(file);
        lastModified = LISTFileFormaterUtils.getLastModified(file);
        lastModifiedUTC = file.getLastModified();
        fileName = nameConstant == null ? file.getName() : nameConstant;

        if (flagFileName)
        {
            fileName = fileName + "/";
        }
    }

    public FtpFileData(FtpFile file, boolean flagFileNames)
    {
        this(file, null, flagFileNames);
    }

    public String getFileName()
    {
        return fileName;
    }

    public char[] getPermissions()
    {
        return permissions;
    }

    public String getLinkCount()
    {
        return linkCount;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public String getLength()
    {
        return length;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public Long getLastModifiedUTC()
    {
        return lastModifiedUTC;
    }
}



