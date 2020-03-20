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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * This class prints file listing.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DirectoryListerWUFTPD {

    private final Logger LOG = LoggerFactory.getLogger(DirectoryListerWUFTPD.class);

    public static final String COMMAND_LIST = "LIST";
    public static final String COMMAND_NLST = "NLST";

    protected final static char[] NEWLINE = { '\r', '\n' };

    private static final String DEFAULT_ROOT = "/";

    private FileFormaterWUFTPD createFormatter(final ListArgument argument, final String command, boolean addRelativePath)
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
        formater.setAddRelativePath(addRelativePath);

        return formater;
    }

    /***
     *
     * @param argument
     * @param command
     * @param fileSystemView
     * @param listingFile
     * @return
     * @throws FtpException
     */
    private String calculateRelativePath(final ListArgument argument, final String command, final FileSystemView fileSystemView, final FtpFile listingFile) throws FtpException
    {
        FtpFile workingDir = null;

        String relativePath = "";

        if (argument.getFile() != null)
        {
            workingDir = fileSystemView.getWorkingDirectory();

            String workingDirAbsolutePath  = workingDir != null  ? workingDir.getAbsolutePath() : "";
            String listingFileAbsolutePath = listingFile != null ? listingFile.getAbsolutePath() : "";

            boolean isListingWorkingDir = workingDirAbsolutePath.equals(listingFileAbsolutePath);

            boolean hasWildcard = argument.getPattern() != null &&
                                 (argument.getPattern().contains("*") || argument.getPattern().contains("?"));

            boolean argFileContainsPath = argument.getFile().contains("/") && !argument.getFile().startsWith("./");

            boolean addRelativePathNLST = (!isListingWorkingDir || argFileContainsPath) &&
                                          (command.equals(COMMAND_NLST) &&
                                          isNullOrEmpty(argument.getOptions()));

            boolean addRelativePathLIST = (!isListingWorkingDir || argFileContainsPath) &&
                                          command.equals(COMMAND_LIST) &&
                                          (hasWildcard || argument.isOriginalRequestContainsWildcard());

            if (addRelativePathLIST || addRelativePathNLST)
            {
                if (listingFile.isDirectory())
                {
                    relativePath = listingFileAbsolutePath;
                }
                else
                {
                    relativePath = listingFile.getParentPath();
                }

                if (!argument.getFile().startsWith("/"))
                {
                    if (relativePath.startsWith(workingDirAbsolutePath))
                    {
                        relativePath = relativePath.substring(workingDirAbsolutePath.length());
                    }

                    if (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }
                }
            }
        }

        return relativePath;
    }

    public String listFiles(final ListArgument argument,
            final FileSystemView fileSystemView, final String command, final FtpFile listingFile) throws IOException
    {
        String relativePath = "";

        try
        {
            relativePath = calculateRelativePath(argument, command, fileSystemView, listingFile);
        }
        catch(Exception ex)
        {
            LOG.debug("Error when calculating relative path");
        }

        FileFormaterWUFTPD formater = createFormatter(argument, command, (relativePath != null && !relativePath.isEmpty()));

        boolean isDirectory = listingFile != null && listingFile.isDirectory();
        if (isDirectory && argument.hasOption('d') && !argument.hasOptions('C') && !argument.hasOption('f'))
        {
            return formater.format(new FtpFileData(listingFile, ".", formater.isFlagFileNames(), relativePath));
        }
        else
        {
            List<FtpFile> files = listFiles(fileSystemView, argument.getFile());
            String result = "";

            if (files != null)
            {
                FileFilter filter = null;
                if (!argument.hasOption('a') && !command.equals(COMMAND_LIST)) {
                    filter = new VisibleFileFilter();
                }

                if (argument.getPattern() != null) {
                    filter = new RegexFileFilter(argument.getPattern(), filter);
                }

                boolean skipFolders = command.equals(COMMAND_NLST) && isNullOrEmpty(argument.getOptions());

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
                        parentFile = DEFAULT_ROOT.equals(listingFile.getAbsolutePath()) ? listingFile : fileSystemView.getFile(listingFile.getParentPath());
                    }
                    catch(Exception ex)
                    {
                    }

                    result = traverseFiles(files, filter, formater, skipFolders, listingFile, parentFile, sortType, relativePath);
                }
                else
                {
                    result = traverseFiles(files, filter, formater, skipFolders, null, null, sortType, relativePath);
                }
            }

            boolean isEmpty = result.isEmpty() && isNullOrEmpty(files);
            String dummyLine = "";

            if (formater.allowAddTotalLine() &&
                isDirectory &&
                (isEmpty || !result.isEmpty()) &&
                isNullOrEmpty(argument.getPattern()))
            {
                dummyLine = createDummyLine(isEmpty);
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

    private String createDummyLine(boolean emptyDir)
    {
        if (emptyDir)
        {
            return "total 0";
        }
        else
        {
            return "total 16";
        }
    }

    private String traverseFiles(final List<FtpFile> files, final FileFilter filter, final FileFormaterWUFTPD formater, boolean skipFolders, FtpFile current, FtpFile parent, Integer sortType, String relativePath)
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
                boolean isDirectory = file.isDirectory();
                if (!isDirectory  || (isDirectory && !skipFolders))
                {
                    ftpFiles.add(new FtpFileData(file, flagFileName, relativePath));
                }
            }
        }

        if (parent != null)
        {
            ftpFiles.add(0, new FtpFileData(parent, "..", formater.isFlagFileNames(), ""));
        }

        if (current != null)
        {
            ftpFiles.add(0, new FtpFileData(current, ".", formater.isFlagFileNames(), ""));
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

    private boolean isNullOrEmpty(List data)
    {
        return (data == null || data.size() == 0);
    }

    private boolean isNullOrEmpty(char[] argument)
    {
        return (argument == null || argument.length == 0);
    }
}
