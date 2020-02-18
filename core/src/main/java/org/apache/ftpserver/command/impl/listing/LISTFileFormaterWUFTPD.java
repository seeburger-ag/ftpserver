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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LISTFileFormaterWUFTPD implements FileFormaterWUFTPD
{
    private final static char DELIM = ' ';
    private final static char[] NEWLINE = { '\r', '\n' };

    private boolean showOwnerColumn = false;
    private boolean showOnlyNameColumn = false;
    private boolean applyFormatingCF = false;
    private boolean flagFileNames = false;
    private boolean addTotalLine = false;

    protected static final Integer FORMAT_TYPE_LIST_HIDE_OWNER = 11;
    protected static final Integer FORMAT_TYPE_SHOW_ONLY_NAME = 12;
    protected static final Integer FORMAT_TYPE_NLST_SHOW_OWNER = 14;
    protected static final Integer FORMAT_TYPE_CF = 15;

    protected static final Integer SORT_TYPE_BY_LASTMODIFIED = 21;

    private static final int MAX_LINE_3_OR_MORE_COLUMNS_CF = 65;
    private static final int MAX_LINE_1_OR_2_COLUMNS_CF = 80;
    private static final int MAX_FILE_NAME_SIZE_LESS_3_COL_CF = 38;
    private static final int MAX_COLUMNS_CF = 11;
    private static final int MIN_SEPARATORS_NUMBER_CF = 3;
    private static final String MIN_SEPARATORS_STRING_CF = "   ";

    /***
     *
     * @param formatType
     */
    public LISTFileFormaterWUFTPD(Integer formatType, boolean flagFileNames)
    {
        if (formatType.equals(FORMAT_TYPE_LIST_HIDE_OWNER))
        {
            this.addTotalLine = true;
            showOwnerColumn = false;
        }
        if (formatType.equals(FORMAT_TYPE_SHOW_ONLY_NAME))
        {
            showOnlyNameColumn = true;
        }
        else if (formatType.equals(FORMAT_TYPE_NLST_SHOW_OWNER))
        {
            this.addTotalLine = true;
            showOwnerColumn = true;
        }
        else if (formatType.equals(FORMAT_TYPE_CF))
        {
            applyFormatingCF = true;
        }

        this.flagFileNames = flagFileNames;
    }

    /***
     *
     * @param showOwnerColumn
     * @param data
     * @param columnsMaxSize
     * @param nameConstant
     * @return
     */
    private String formatByColumnSize(boolean showOwnerColumn, FtpFileData data, ListColumnsMaxSize columnsMaxSize)
    {
        return format(String.format(createFormatStringLeftAlign(columnsMaxSize.fileNameMaxSize), data.getFileName()),
                      data.getPermissions(), data.getLinkCount(),
                      String.format(createFormatStringLeftAlign(columnsMaxSize.ownerNameMaxSize), data.getOwnerName()),
                      String.format(createFormatStringLeftAlign(columnsMaxSize.groupNameMaxSize), data.getGroupName()),
                      data.getLength(), data.getLastModified());
    }

    public String format(FtpFileData data)
    {
        return format(data.getFileName(), data.getPermissions(),
                      data.getLinkCount(),data.getOwnerName(),
                      data.getGroupName(), data.getLength(), data.getLastModified());
    }

    private String format(String fileName, char[] permissions, String linkCount, String ownerName, String groupName, String legth, String lastModified)
    {
        StringBuilder sb = new StringBuilder();

        if (showOnlyNameColumn)
        {
            sb.append(fileName);
            sb.append(NEWLINE);
        }
        else
        {
            sb.append(permissions);
            sb.append(DELIM);
            sb.append(DELIM);
            sb.append(DELIM);
            sb.append(linkCount);
            sb.append(DELIM);

            if (showOwnerColumn)
            {
                sb.append(ownerName);
                sb.append(DELIM);
            }

            sb.append(groupName);
            sb.append(DELIM);
            sb.append(legth);
            sb.append(DELIM);
            sb.append(lastModified);
            sb.append(DELIM);

            sb.append(fileName);
            sb.append(NEWLINE);
        }

        return sb.toString();
    }

    /***
     *
     * @see org.apache.ftpserver.command.impl.listing.FileFormaterWUFTPD#isApplyListFormatingCF()
     */
    public boolean isColumnarFormat()
    {
        return applyFormatingCF;
    }

    public String format(List<FtpFileData> ftpFiles, Integer sortType)
    {
        if (isColumnarFormat())
        {
           return formatListCF(ftpFiles, sortType);
        }
        else
        {
            return formatListAlignColumns(ftpFiles, sortType);
        }
    }

    private String formatListAlignColumns(List<FtpFileData> files, Integer sortType)
    {
        StringBuilder sb = new StringBuilder();

        List<FtpFileData> fillDataArray = new ArrayList<FtpFileData>();
        ListColumnsMaxSize columnsMaxSizeArray =  new ListColumnsMaxSize();

        if (files != null)
        {
            for (FtpFileData ftpFile : files)
            {
                fillDataAndCalculateMaxColumnSize(fillDataArray, columnsMaxSizeArray, ftpFile);
            }
        }

        if (SORT_TYPE_BY_LASTMODIFIED.equals(sortType))
        {
            sort(fillDataArray, sortType);
        }

        for (FtpFileData data : fillDataArray)
        {
            sb.append(formatByColumnSize(showOwnerColumn, data, columnsMaxSizeArray));
        }

        return sb.toString();
    }

    private void fillDataAndCalculateMaxColumnSize(List<FtpFileData> fillDataArray, ListColumnsMaxSize columnSize, FtpFileData file)
    {
       if (fillDataArray != null)
       {
            fillDataArray.add(file);

            if (columnSize != null)
            {
                if (compare(file.getFileName(), columnSize.fileNameMaxSize))
                {
                    columnSize.fileNameMaxSize = file.getFileName().length();
                }

                if (compare(file.getOwnerName(), columnSize.ownerNameMaxSize))
                {
                    columnSize.ownerNameMaxSize = file.getOwnerName().length();
                }

                if (compare(file.getGroupName(), columnSize.groupNameMaxSize))
                {
                    columnSize.groupNameMaxSize = file.getGroupName().length();
                }

                if (compare(file.getLength(), columnSize.lengthMaxSize))
                {
                    columnSize.lengthMaxSize = file.getLength().length();
                }
            }
       }
    }

    /***
     * Returns true if size of value is bigger than size
     * @param value
     * @param size
     * @return
     */
    private boolean compare(String value, int size)
    {
        if (value != null && value.length() > size)
        {
            return true;
        }
        return false;
    }

    class ListColumnsMaxSize
    {
        int ownerNameMaxSize;
        int groupNameMaxSize;
        int lengthMaxSize;
        int fileNameMaxSize;
    }

    private void sort(List<FtpFileData> dataArray, Integer sortType)
    {
        if (dataArray != null && dataArray.size() > 1)
        {
            if (SORT_TYPE_BY_LASTMODIFIED.equals(sortType))
            {
                Collections.sort(dataArray, new Comparator<FtpFileData>() {
                    public int compare(FtpFileData data1, FtpFileData data2) {
                        return data2.getLastModifiedUTC().compareTo(data1.getLastModifiedUTC());
                    }
                });
            }
        }
    }

    private String formatListCF(List<FtpFileData> files, Integer sortType)
    {
        StringBuilder sb = new StringBuilder();

        if (files != null)
        {
            if (SORT_TYPE_BY_LASTMODIFIED.equals(sortType))
            {
                sort(files, sortType);
            }

            Integer columnSize = getMaxFileNameSize(files);

            Integer numberOfColumns = calculateNumberOfColumnsCF(columnSize, MIN_SEPARATORS_NUMBER_CF);

            if (numberOfColumns == 1 || (numberOfColumns == 2 && hasFileNameBiggerThanSize(files, MAX_FILE_NAME_SIZE_LESS_3_COL_CF)))
            {
                for (FtpFileData fileName : files)
                {
                    sb.append(fileName.getFileName());
                    sb.append(NEWLINE);
                }
            }
            else
            {
                List<String> dataCF = createFormatedListCF(files, numberOfColumns, columnSize);

                for (String data : dataCF)
                {
                    sb.append(data);
                    sb.append(NEWLINE);
                }
            }
        }

        return sb.toString();
    }

    private static Integer calculateNumberOfColumnsCF(Integer columnSize, Integer separatorSize)
    {
        int maxLineSize = MAX_LINE_3_OR_MORE_COLUMNS_CF;

        int numberOfColumns;
        for (numberOfColumns = MAX_COLUMNS_CF; numberOfColumns > 1; numberOfColumns --)
        {
            if (numberOfColumns == 2)
            {
                maxLineSize = MAX_LINE_1_OR_2_COLUMNS_CF;
            }

            if ((separatorSize * (numberOfColumns-1)) + (numberOfColumns * columnSize) <= maxLineSize)
            {
                return numberOfColumns;
            }
        }

        return numberOfColumns;
    }

    private static List<String> createFormatedListCF(List<FtpFileData> dataList, Integer numberOfColumns, Integer columnNameSize)
    {
        List<String> dataArr = new ArrayList<String>();

        if (dataList != null && numberOfColumns > 0 && columnNameSize != null)
        {
            int elementsInColumn = dataList.size() / numberOfColumns;
            int restElements = dataList.size() % numberOfColumns;

            if (restElements > 0)
            {
                elementsInColumn ++;
            }

            for (int i = 0; i < elementsInColumn; i ++)
            {
                dataArr.add("");
            }

            int rowIndex = 0;

            for (FtpFileData data : dataList)
            {
                if (rowIndex == elementsInColumn)
                {
                    rowIndex = 0;
                }

                String rowData = dataArr.get(rowIndex);
                if (rowData != null && !rowData.isEmpty())
                {
                    rowData += MIN_SEPARATORS_STRING_CF;
                }

                dataArr.set(rowIndex, rowData + String.format(createFormatStringLeftAlign(columnNameSize), data.getFileName()));
                rowIndex++;
            }
        }

        return dataArr;
    }

    private static Integer getMaxFileNameSize(List<FtpFileData> dataList)
    {
        Integer columnSize = 0;
        for (FtpFileData data : dataList)
        {
            if (data.getFileName().length() > columnSize)
            {
                columnSize = data.getFileName().length();
            }
        }

        return columnSize;
    }

    private static String createFormatStringLeftAlign(int size)
    {
        return "%-" + size + "." + size + "s";
    }

    private static boolean hasFileNameBiggerThanSize(List<FtpFileData> fileNames, Integer size)
    {
        for (FtpFileData file : fileNames)
        {
            if (file.getFileName().length() > size)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isFlagFileNames()
    {
        return flagFileNames;
    }

    public boolean allowAddTotalLine()
    {
        return addTotalLine;
    }

    public void setAddTotalLine(boolean addTotalLine)
    {
        this.addTotalLine = addTotalLine;
    }
}
