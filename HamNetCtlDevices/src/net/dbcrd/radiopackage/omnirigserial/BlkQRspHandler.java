package net.dbcrd.radiopackage.omnirigserial;

import java.io.IOException;

/**
 *
 * @param <T>
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class BlkQRspHandler <T extends OmniRigCmdData> {

    private PktInfo respPkt=null;
    private final boolean expectRadioReply;

   @Deprecated
   BlkQRspHandler(){
        super();
        expectRadioReply=true;

    }
      BlkQRspHandler(final boolean expreply){
        super();
        expectRadioReply=expreply;

    }
     BlkQRspHandler(final T cmdData){
        super();
        expectRadioReply=cmdData.wait4Reply;

    }


    /**
     * Receives PktInfo
     * @param rsp PktInfo a result packet to return
     * @return  A boolean false
     */
    synchronized boolean handleResponse(final PktInfo rsp) {
        if (expectRadioReply) {
            this.respPkt=rsp;
            this.notify(); //TODO maybe fix
        } else {
            this.respPkt=rsp;
        }
        return false;
    }



   /**
    * returns the PktInfo
    * @return a PktInfo of the returned packet or null after a 2sec. timeout
    */
    synchronized PktInfo waitForResponse() throws IOException {
        if (expectRadioReply){
        boolean tryMe = true;
        while(this.respPkt==null&tryMe){
            try{
                this.wait(15000);
                tryMe=false;
            } catch(InterruptedException e){
            }
        }
       PktInfo pkt = respPkt;
       if (null==pkt){
                throw new IOException("Radio not responding");
       }
        return pkt;
        }else{
            return null;
        }
    }
}
