package net.dbcrd.hrdtcpipclient;

import java.util.Properties;

/**
 * Abstract class for radio CAT control.
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
public abstract class AbstractRadio implements RadioControl {


    /** key for obtaining the portname from the commProps */
    protected final static String RADIO_IDENTIFICATION="Radioident";
    /** key for for obtaining the radio name from rStatus */
    protected final static String RADIO_NAME="RadioName";
    /**
     * a volatile boolean abortClientThread is set true to terminate threads communicating with
     * the CAT controlled radio.
     */
    protected static volatile boolean abortClientThread=false;
    /**
     *  a RadioStatus that gathers and semi-maintains status about the radio.
     */
    protected final RadioStatus rStatus=new RadioStatus();
    /** a boolean that indicates if a radio has been identified */
    protected boolean isRadio=false;
    /** a Properties that contains the communication parameters of relevance to the client */
    protected Properties commProps=new Properties();

    /**
     * Get a clone of the communications related information
     * @return a Properties that has comunication related information.
     */
    public Properties getCommunicationInfo() {
        return (Properties) commProps.clone();
    }

    /**
     * Returns the currenly known status of the radio.  May not track user manipulation of the radio and may
     * be out of date by a couple of seconds.
     * @return a RadioStatus (a subclass of Properties) with currently known status.
     */
    public final RadioStatus getStatus() {
        if(isRadio){
            return rStatus.getProperties();
        }
        return new RadioStatus();
    }


    /**
     *
     * @param radioComId a String that specifies the comm port if a physical radio.
     */
    AbstractRadio(final String radioComId) {
        super();
        if(radioComId.isEmpty()){
         
            commProps.put(RADIO_IDENTIFICATION, "");
            rStatus.put(RADIO_NAME, "abstractRadio");
            return;
        }
        commProps.put(RADIO_IDENTIFICATION, radioComId.trim().toUpperCase());
    }


    /**
     *
     * @param radioComId a RadioIdentification 
     */
    AbstractRadio(final RadioIdentification radioComId) {
        super();
        commProps.put(RADIO_IDENTIFICATION, radioComId.toString());
        rStatus.put(RADIO_NAME, "abstractRadio");
    }
}
