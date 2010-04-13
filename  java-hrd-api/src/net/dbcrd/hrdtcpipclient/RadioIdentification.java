/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dbcrd.hrdtcpipclient;

/**
 * An enum that idenifies supported virtual and physical radios.
 *
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
 *
 */
public enum RadioIdentification {

    /** a virtual radio through HRD  */
    HRD("Ham Radio Deluxe Server", true),
    /** a virtual radio through OMNI_RIG, not implemented  */
    OMNI_RIG("Omni Rig Server", true),
    /** a physical radio through a CAT cable, not implemented  */
    YAESU_FT8x7("FT-8x7", false);
    private final String description;
    private final boolean virtual;

    private RadioIdentification(final String description, final boolean virtual) {
        this.description=description;
        this.virtual=virtual;
    }

    /**
     *
     * @return a String that is the description of the enum.
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Indicates whether the radio is a virtual or physical radio.
     * @return true if virtual, false otherwise.
     */
    public boolean isVirtual() {
        return virtual;
    }
}
