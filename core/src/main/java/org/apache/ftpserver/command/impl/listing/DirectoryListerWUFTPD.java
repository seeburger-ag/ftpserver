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
public class DirectoryListerWUFTPD {

    public static final String COMMAND_LIST = "LIST";
    public static final String COMMAND_NLST = "NLST";

    protected final static char[] NEWLINE = { '\r', '\n' };

    private static final String DEFAULT_ROOT = "/";

    private String traverseFiles(final List<FtpFile> files, boolean skipFolders, final FileFilter filter, final FileFormaterWUFTPD formater, FtpFile current, FtpFile parent, Integer sortType)
    {
        StringBuilder sb = new StringBuilder();

        if (skipFolders)
        {
            sb.append(traverseFiles(files, filter, formater, false, current, parent, sortType));
        }
        else
        {
            sb.append(traverseFiles(files, filter, formater, current, parent, sortType));
        }

        return sb.toString();
    }

    private FileFormaterWUFTPD createFormatter(final ListArgument argument, final String command)
    {
        Integer formatType = null;

        String optionsString = String.valueOf(argument.getOptions());

        boolean formatCF = argument.hasOptions('C') &&
                           (!argument.hasOption('l') ||
                            argument.hasOptions('f') ||
                            (optionsString.lastIndexOf("C") > optionsString.lastIndexOf("l")));

        boolean addTotalLine = false;
        if (formatCF)
        {
            formatType = LISTFileFormaterWUFTPD.FORMAT_TYPE_CF;
        }
        else if (argument.hasOption('f'))
        {
            formatType = LISTFileFormaterWUFTPD.FORMAT_TYPE_SHOW_ONLY_NAME;
        }
        else if (command.equals(COMMAND_LIST))
        {
            formatType = LISTFileFormaterWUFTPD.FORMAT_TYPE_LIST_HIDE_OWNER;
            addTotalLine = !argument.hasOption('d');
        }
        else if (command.equals(COMMAND_NLST))
        {
            formatType = argument.hasOption('l') ? LISTFileFormaterWUFTPD.FORMAT_TYPE_NLST_SHOW_OWNER : LISTFileFormaterWUFTPD.FORMAT_TYPE_SHOW_ONLY_NAME;
            addTotalLine = argument.hasOption('l') || argument.hasOptions('l','a');
        }

        LISTFileFormaterWUFTPD formater = new LISTFileFormaterWUFTPD(formatType,
                                                                     argument.hasOption('F') && !argument.hasOption('f'));

        formater.setAddTotalLine(addTotalLine);

        return formater;
    }

    public String listFiles(final ListArgument argument,
            final FileSystemView fileSystemView, final String command, FtpFile listingFile) throws IOException
    {
        boolean isDirectory = listingFile != null && listingFile.isDirectory();

        FileFormaterWUFTPD formater = createFormatter(argument, command);

        if (isDirectory && argument.hasOption('d') && !argument.hasOptions('C') && !argument.hasOption('f'))
        {
            return formater.format(new FtpFileData(listingFile, ".", formater.isFlagFileNames()));
        }
        else
        {
            List<FtpFile> files = listFiles(fileSystemView, argument.getFile());
            String result = "";

            if (files != null)
            {
                FileFilter filter = null;
                if (!argument.hasOption('a')) {
                    filter = new VisibleFileFilter();
                }
                if (argument.getPattern() != null) {
                    filter = new RegexFileFilter(argument.getPattern(), filter);
                }

                boolean skipFolders = command.equals(COMMAND_NLST) && (argument.getOptions() == null || argument.getOptions().length == 0);

                Integer sortType = null;

                if (argument.hasOption('t') && !argument.hasOption('f'))
                {
                    sortType = LISTFileFormaterWUFTPD.SORT_TYPE_BY_LASTMODIFIED;
                }

                boolean appendParentFoldersInfo = false;

                if (isDirectory && isNullOrEmpty(argument.getPattern()))
                {
                    appendParentFoldersInfo = command.equals(COMMAND_LIST) && (hasAnyOfOptions(argument, 'a','f') || argument.hasOptions('a','l'));
                    appendParentFoldersInfo = appendParentFoldersInfo || (command.equals(COMMAND_NLST) && (argument.hasOptions('a','l') || hasAnyOfOptions(argument, 'a', 'f')));
                }

                if (appendParentFoldersInfo)
                {
                    FtpFile parentFile = null;

                    try
                    {
                        parentFile = DEFAULT_ROOT.equals(listingFile.getParentPath()) ? listingFile : fileSystemView.getFile(listingFile.getParentPath());
                    }
                    catch(Exception ex)
                    {
                    }

                    result = traverseFiles(files, skipFolders, filter, formater, listingFile, parentFile, sortType);
                }
                else
                {
                    result = traverseFiles(files, skipFolders, filter, formater, null, null, sortType);
                }
            }

            String dummyLine = "";
            if (isDirectory && isNullOrEmpty(argument.getPattern()) && formater.allowAddTotalLine())
            {
                dummyLine = createDummyLine(files == null || files.isEmpty());
            }

            StringBuffer sb = new StringBuffer();

            if (dummyLine != null && !dummyLine.isEmpty())
            {
                sb.append(dummyLine);
                sb.append(NEWLINE);
            }

            sb.append(result);

            return sb.toString();
        }
    }

    private boolean isNullOrEmpty(String value)
    {
        return (value == null || value.trim().isEmpty());
    }

    private String createDummyLine(boolean isEmptyFolder)
    {
        if (isEmptyFolder)
        {
            return "total 0";
        }
        else
        {
            return "total 16";
        }
    }

    private String traverseFiles(final List<FtpFile> files, final FileFilter filter, final FileFormaterWUFTPD formater, FtpFile current, FtpFile parent, Integer sortType)
    {
        return traverseFiles(files, filter, formater, false, current, parent, false, sortType);
    }

    private String traverseFiles(final List<FtpFile> files, final FileFilter filter, final FileFormaterWUFTPD formater, boolean matchDirs, FtpFile current, FtpFile parent, Integer sortType)
    {
        return traverseFiles(files, filter, formater, matchDirs, current, parent, true, sortType);
    }

    private String traverseFiles(final List<FtpFile> files, final FileFilter filter, final FileFormaterWUFTPD formater, boolean matchDirs, FtpFile current, FtpFile parent, boolean checkIsDir, Integer sortType)
    {
        List<FtpFileData> ftpFiles = new ArrayList<FtpFileData>();

        for (FtpFile file : files)
        {
            if (file == null)
            {
                continue;
            }

            boolean flagFileName = file.isDirectory() && formater.isFlagFileNames();

            if (filter == null || filter.accept(file))
            {
                if (!checkIsDir || file.isDirectory() == matchDirs)
                {
                    ftpFiles.add(new FtpFileData(file, flagFileName));
                }
            }
        }

        if (parent != null)
        {
            ftpFiles.add(0, new FtpFileData(parent, "..", formater.isFlagFileNames()));
        }

        if (current != null)
        {
            ftpFiles.add(0, new FtpFileData(current, ".", formater.isFlagFileNames()));
        }

        return formater.format(ftpFiles, sortType);
    }

    private boolean hasAnyOfOptions(ListArgument argument, char... options) {

        for (char option : options)
        {
            if (argument.hasOption(option))
            {
                return true;
            }
        }

        return false;
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
