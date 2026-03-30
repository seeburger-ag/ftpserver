/*
 * Copyright 2023-2025 SEEBURGER AG
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

package org.apache.ftpserver.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.ftpserver.command.impl.AUTH;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.ssl.DisableEncryptWriteRequest;
import org.apache.mina.filter.ssl.EncryptedWriteRequest;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.filter.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPSslFilterCustom  extends SslFilter
{
    private final Logger LOG = LoggerFactory.getLogger(FTPSslFilterCustom.class);

    public static final String SSL_FILTER_SESSION_CLOSED_HANDLED = FTPSslFilterCustom.class.getName() + ".CLOSED_HANDLED";

    private static final String SSL_FILTER_EXCEPTION_CAUGHT = FTPSslFilterCustom.class.getName() + ".EXCEPTION_CAUUGHT";

    public FTPSslFilterCustom(SSLContext sslContext)
    {
        super(sslContext);
    }


    @Override
    public void filterWrite(NextFilter next, IoSession session, WriteRequest request)
        throws Exception
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("session {} write {}", session, request);
        }

        if (request instanceof EncryptedWriteRequest ||
            request instanceof DisableEncryptWriteRequest ||
            session.containsAttribute(AUTH.DISABLE_ENCRYPTION_ONCE))
        {
            session.removeAttribute(AUTH.DISABLE_ENCRYPTION_ONCE);
            next.filterWrite(session, request);
        }
        else
        {
            SslHandler sslHandler = SslHandler.class.cast(session.getAttribute(SSL_HANDLER));
            sslHandler.write(next, request);
        }
    }

    @Override
    public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause)
        throws Exception
    {
        if (cause instanceof SSLException)
        {
            session.setAttribute(SSL_FILTER_EXCEPTION_CAUGHT, Boolean.TRUE);
        }

        super.exceptionCaught(nextFilter, session, cause);
    }


    @Override
    public void sessionClosed(NextFilter next, IoSession session)
        throws Exception
    {
        if (session.getAttribute(FtpIoSession.ATTRIBUTE_USER) == null &&
            session.getAttribute(SSL_FILTER_EXCEPTION_CAUGHT) != null &&
            session.getAttribute(SSL_FILTER_SESSION_CLOSED_HANDLED) == null)
        {
                // in case of tls error this code not invoked in TailFilter
                session.getHandler().sessionClosed(session);

                session.setAttribute(SSL_FILTER_SESSION_CLOSED_HANDLED, Boolean.TRUE);

                LOG.debug("executed session closed");
        }

        super.sessionClosed(next, session);
    }
}



