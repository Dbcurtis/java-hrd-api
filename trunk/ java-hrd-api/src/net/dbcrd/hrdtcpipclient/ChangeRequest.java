
package net.dbcrd.hrdtcpipclient;

import java.nio.channels.SocketChannel;

/**
 * A change request
 * see <link>http://rox-xmlrpc.sourceforge.net/niotut/</link>
 * some modificaitons by Dan Curtis 
 */
public class ChangeRequest {

    public static final int REGISTER=1;
    public static final int CHANGEOPS=2;
    protected final SocketChannel socket;
    protected final int type;
    protected final int ops;

    public ChangeRequest(final SocketChannel socket, final int type, final int ops) {
        this.socket=socket;
        this.type=type;
        this.ops=ops;
    }
}

