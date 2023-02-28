/*
 * FTPSslFilterCustom.java
 *
 * created at 2023-02-28 by t.nalbantova <YOURMAILADDRESS>
 *
 * Copyright (c) SEEBURGER AG, Germany. All Rights Reserved.
 */
package org.apache.ftpserver.ssl;

import javax.net.ssl.SSLContext;

import org.apache.ftpserver.command.impl.AUTH;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.ssl.DisableEncryptWriteRequest;
import org.apache.mina.filter.ssl.EncryptedWriteRequest;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.filter.ssl.SslHandler;

public class FTPSslFilterCustom  extends SslFilter
{
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
}



