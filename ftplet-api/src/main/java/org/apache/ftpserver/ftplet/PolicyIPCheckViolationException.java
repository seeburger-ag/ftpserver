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

public class PolicyIPCheckViolationException extends RuntimeException
{
    /** field <code>serialVersionUID</code> */
    private static final long serialVersionUID = -2928011881922262582L;

    public PolicyIPCheckViolationException()
    {
        super();
    }

    public PolicyIPCheckViolationException(String message)
    {
        super(message);
    }


    public PolicyIPCheckViolationException(Exception cause)
    {
        super(cause);
    }


    public PolicyIPCheckViolationException(Throwable cause)
    {
        super(cause);
    }


    public PolicyIPCheckViolationException(String message, Exception cause)
    {
        super(message, cause);
    }
}
