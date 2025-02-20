/*
 * Copyright 2025 SEEBURGER AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ftpserver.ftplet;


public class PolicyFileNameViolationException extends RuntimeException
{
    /** field <code>serialVersionUID</code> */
    private static final long serialVersionUID = 7962722209195878037L;

    public static String MESSAGE = "File name not allowed: ";

    public PolicyFileNameViolationException()
    {
        super();
    }

    public PolicyFileNameViolationException(String message)
    {
        super(message);
    }


    public PolicyFileNameViolationException(Exception cause)
    {
        super(cause);
    }


    public PolicyFileNameViolationException(Throwable cause)
    {
        super(cause);
    }


    public PolicyFileNameViolationException(String message, Exception cause)
    {
        super(message, cause);
    }
}
