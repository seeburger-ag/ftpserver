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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * This class prints file listing.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DirectoryLister {

    private String traverseFiles(final List<FtpFile> files, boolean nlstSkipFolders,
            final FileFilter filter, final FileFormater formater) {
        StringBuilder sb = new StringBuilder();

        if (!nlstSkipFolders)
        {
            sb.append(traverseFiles(files, filter, formater, true));
        }
        sb.append(traverseFiles(files, filter, formater, false));

        return sb.toString();
    }

    public String listFiles(final ListArgument argument,
            final FileSystemView fileSystemView, final FileFormater formater, boolean nlstSkipFolders)
            throws IOException {

        StringBuilder sb = new StringBuilder();

        // get all the file objects
        List<FtpFile> files = listFiles(fileSystemView, argument.getFile());
        if (files != null) {
            FileFilter filter = null;
            if (!argument.hasOption('a')) {
                filter = new VisibleFileFilter();
            }
            if (argument.getPattern() != null) {
                filter = new RegexFileFilter(argument.getPattern(), filter);
            }

            sb.append(traverseFiles(files, nlstSkipFolders, filter, formater));
        }

        return sb.toString();
    }

    public String listFiles(final ListArgument argument,
                            final FileSystemView fileSystemView, final FileFormater formater)
                            throws IOException {

        return listFiles(argument, fileSystemView, formater, false);
    }

    private String traverseFiles(final List<FtpFile> files, final FileFilter filter, final FileFormater formater, boolean matchDirs)
    {
        StringBuilder sb = new StringBuilder();
        for (FtpFile file : files)
        {
            if (file == null)
            {
                continue;
            }

            if (filter == null || filter.accept(file))
            {
                if (file.isDirectory() == matchDirs)
                {
                    sb.append(formater.format(file));
                }
            }
        }

        return sb.toString();
    }

    private List<FtpFile> listFiles(FileSystemView fileSystemView, String file)
    {
        List<FtpFile> files = null;
        try
        {
            FtpFile virtualFile = fileSystemView.getFile(file);
            if (virtualFile.isFile())
            {
                files = new ArrayList<FtpFile>();
                files.add(virtualFile);
            }
            else
            {
                files = virtualFile.listFiles();
            }
        }
        catch (FtpException ex)
        {}
        return files;
    }
}
