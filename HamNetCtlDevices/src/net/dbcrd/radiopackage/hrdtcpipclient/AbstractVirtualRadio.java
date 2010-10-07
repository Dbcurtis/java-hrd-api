package net.dbcrd.radiopackage.hrdtcpipclient;

import java.io.IOException;
import java.net.InetAddress;
import net.dbcrd.radiopackage.RadioIdentification;
import net.dbcrd.radiopackage.RadioStatus;


/**
 * Abstract class for CAT control of a virtual radio.  This class is a foundation for
 * virtual radio CAT access (such as accessing a radio
 * through Ham Radio Deluxe, Omni-Rig, or the like).
 *
 * @author Daniel B. Curtis N6WN
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
 */
 abstract class AbstractVirtualRadio extends net.dbcrd.radiopackage.AbstractRadio {


    InetAddress inetAddress=null;
    int port = 0;
    /** the NioClient object that performs TCP/IP communication with the server */
    protected NioClient client= null;
    /** a boolean that indicates whether the specified vertualID device is, in fact, virtual vs physical */
    protected boolean portExists=false;

    AbstractVirtualRadio(final RadioIdentification vertualID){

        super(vertualID); 
        if(!vertualID.isVirtual()){
            portExists=false;
            throw new IllegalArgumentException(vertualID.toString()+" is not a virtual radio device");
        }
        portExists=true;
        commProps.put(RadioIdentification.HRD, vertualID.toString());
        putStatus(RadioStatus.RADIO_NAME, "AbstractVirtualRadio");
        return;
    }


    /**
     *
     * @throws IOException
     */
    protected void connectToRadioServer() throws IOException{
         client = new NioClient(inetAddress, port);
    }
}
