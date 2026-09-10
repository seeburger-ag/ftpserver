/*
 * AccountLockedException.java
 *
 * created at 2026-09-10 by t.nalbantova <YOURMAILADDRESS>
 *
 * Copyright (c) SEEBURGER AG, Germany. All Rights Reserved.
 */
package org.apache.ftpserver.ftplet;


public class UserAccountLockedException extends RuntimeException
{
    /** field <code>serialVersionUID</code> */
    private static final long serialVersionUID = 6878477471330992617L;

    /**
     * Default constructor.
     */
    public UserAccountLockedException() {
        super();
    }

    /**
     * Constructs a <code>AuthenticationFailedException</code> object with a
     * message.
     *
     * @param msg
     *            A description of the exception
     */
    public UserAccountLockedException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>AuthenticationFailedException</code> object with a
     * <code>Throwable</code> cause.
     *
     * @param th
     *            The original cause
     */
    public UserAccountLockedException(Throwable th) {
        super(th);
    }

    /**
     * Constructs a <code>AuthenticationFailedException</code> object with a
     * <code>Throwable</code> cause.
     * @param msg A description of the exception
     *
     * @param th
     *            The original cause
     */
    public UserAccountLockedException(String msg, Throwable th) {
        super(msg, th);
    }
}



