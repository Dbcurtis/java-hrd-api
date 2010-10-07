
package net.dbcrd.radiopackage.omnirigserial;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class PktInfo implements Serializable, Comparable<PktInfo> {

    private static final long serialVersionUID=73474747447L;
    private long pktseqnum=0;
    private long pktSrcnum=0;
    final private BlkQRspHandler<OmniRigCmdData> handler;
    String portId;
    Object payload;
    /** each packet get a unique long number when it is instantiated */
    private final static AtomicLong PKT_NUM=new AtomicLong(1);

    BlkQRspHandler<OmniRigCmdData> getHandler() {
        return handler;
    }


    long getPktSrcnum() {
        return pktSrcnum;
    }

    void setPktSrcnum(final long pktSrcnum) {
        if (this.pktSrcnum==0){this.pktSrcnum=pktSrcnum;}
    }

    long getPktseqnum() {
        return pktseqnum;
    }

    private void setPktseqnum(final long pktseqnum) {
         if (this.pktseqnum==0){this.pktseqnum=pktseqnum;}
    }

    /**
     * generate a PktInfo from the first packet in the byte array
     * @param portId 
     * @param payload
     * @param handler
     */
    PktInfo(final String portId,final Object payload, final BlkQRspHandler<OmniRigCmdData> handler) {
        super();
        this.portId=portId;
        this.payload=payload;
        this.handler=handler;
        this.setPktseqnum(PKT_NUM.getAndIncrement());
    }

   /**
    * Used to send commands to the server
    * @param payload
    */
    PktInfo(final BlkServerCmd payload) {
        super();
        this.portId="BlkServerCmd";
        this.payload=payload;
        this.handler=null;
         this.setPktseqnum(PKT_NUM.getAndIncrement());
    }

    /**
     * Used to send a response to a packet back.
     * @param srcPkt
     * @param payload
     */
    PktInfo(final PktInfo srcPkt, final Object payload) {
        super();
        this.payload=payload;
        this.pktSrcnum=srcPkt.pktseqnum;
        this.handler=srcPkt.handler;
        this.portId=srcPkt.portId;
        this.setPktseqnum(PKT_NUM.getAndIncrement());
    }

    @Override
    public String toString() {
        return new StringBuilder(portId).
                append("s:sum:repnum=").
                append(":-:").
                append(pktseqnum).
                append(':').append(pktSrcnum).toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj==null){
            return false;
        }
        if(getClass()!=obj.getClass()){
            return false;
        }
        final PktInfo other=(PktInfo) obj;
        if(this.pktseqnum!=other.pktseqnum){
            return false;
        }
        if(this.pktSrcnum!=other.pktSrcnum){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash=7;
        hash=59*hash+(int) (this.pktseqnum^(this.pktseqnum>>>32));
        return hash;
    }
    private static final int BEFORE=-1;
    private static final int EQUAL=0;
    private static final int AFTER=1;

    @Override
    public int compareTo(final PktInfo aThat) {
        if(this==aThat){
            return EQUAL;
        }
        if(this.pktseqnum<aThat.pktseqnum){
            return BEFORE;
        }
        if(this.pktseqnum>aThat.pktseqnum){
            return AFTER;
        }
        return EQUAL;
    }
}
