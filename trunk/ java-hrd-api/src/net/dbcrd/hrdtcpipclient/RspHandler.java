package net.dbcrd.hrdtcpipclient;

import java.util.logging.Logger;

/**
 * A class receives responses from requests to the server.
 *
 * @author Daniel B. Curtis N6WN
 *
 *
 * Copyright 2010 Daniel B. Curtis
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * based on <link>http://rox-xmlrpc.sourceforge.net/niotut/</link>
 */
public class RspHandler {

    static final int BYTE_MASK=0xFF;
    static final long TEN_SEC=10000L;
     static final long FIVE_SEC=5000L;
    static final String ERROR="error";

    private static final Logger THE_LOGGER=Logger.getLogger(
            RspHandler.class.getName());

    private byte[] rsp=null;
    private int length;

    /**
     * Copies the rsp data into the object, and notifies this thread that data has arrived.
     * @param rsp a byte[] that is the received data.
     * @return a boolean true
     */
    public synchronized boolean handleResponse(final byte[] rsp) {
        this.rsp=rsp.clone();
        this.notify();
        return true;
    }


    /**
     * Receives data from the server, validates the packet format, and extracts and trims the packet payload.
     * @return a String containing the response to the prior command or
     *  "error" if the wait timed out (>4 sec) or was interrupted.
     */
    public synchronized String waitForResponse() {
        while(this.rsp==null){
            try{
                final long starttime = System.currentTimeMillis();
                this.wait(FIVE_SEC);
                final long waittime = System.currentTimeMillis()-starttime;
                if (waittime > 4000){
                    THE_LOGGER.warning("Response delayed > 4 secs");
                    return ERROR;
                }
            } catch(InterruptedException e){
                return ERROR;
            }
        }

        if(rsp.length==(BYTE_MASK&rsp[0])){
            length=BYTE_MASK&rsp[0];
        } else{
            if(rsp.length>3){
                length=0;
                length+=BYTE_MASK&rsp[0];
                length+=(BYTE_MASK&rsp[1])<<8;
                length+=(BYTE_MASK&rsp[2])<<16;
                length+=(BYTE_MASK&rsp[3])<<24;
                if(rsp.length!=length){
                    THE_LOGGER.severe("partial packet 1******************");
                    return ERROR;
                }
            } else{
                THE_LOGGER.severe("partial packet 2******************");
                return ERROR;
            }
        }
        if(validatePacket()){
            return (extractData());
        }
        THE_LOGGER.severe("Invalid packet *****************");
        return ERROR;
    }

    /**
     * 
     * @return a boolean indicating whether the packet is well formed
     */
    private boolean validatePacket() {
        boolean result=true;
        result=result&&rsp[4]==(byte) 0xcd;
        result=result&&rsp[5]==(byte) 0xab;
        result=result&&rsp[6]==(byte) 0x34;
        result=result&&rsp[7]==(byte) 0x12;
        result=result&&rsp[8]==(byte) 0x34;
        result=result&&rsp[9]==(byte) 0x12;
        result=result&&rsp[10]==(byte) 0xcd;
        result=result&&rsp[11]==(byte) 0xab;
        for(int i=length-1; i>length-7; i--){
            result=result&&rsp[i]==0;
        }
        return result;
    }

    /**
     *
     * @return a String containing the szText as a Java String (trimed)
     */
    private String extractData() {
        final byte[] result=new byte[length-22];
        System.arraycopy(rsp, 16, result, 0, length-22);
        final StringBuilder sbTemp=new StringBuilder();
        int idx=0;
        for(int i=0; i<(result.length>>1); i++){
            final char mychar=(char) result[idx++];
            idx++;
            sbTemp.append(mychar);
        }
        return sbTemp.toString().trim();
    }
}
