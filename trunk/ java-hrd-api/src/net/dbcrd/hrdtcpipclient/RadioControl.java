package net.dbcrd.hrdtcpipclient;

import java.util.Properties;

/**
 *  A CAT radio control interface
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
 */
public interface RadioControl {


   /**
    * return the properties related to the CAT communications.
    * @return a Properties containing the communication parameters
    */
    Properties getCommunicationInfo();

    /**
     * Sets the radio's "lock" control.
     * @param lock a boolean, if true, sets the lock, if false releases it
     * @return a boolean indicating whether the operation succeeded.
     */
     boolean setLock(final boolean lock);

   /**
    * Returns the status of the radio's "lock" control
     *
     * @return  boolean true if radio is locked, false if not locked.
     */
     boolean isLock();

    /**
     * Starts transmitting.
     * @return a boolean to show command success
     */
     boolean pttPush();

    /**
     * Stops transmitting.
     * @return a boolean to show command success
     */
     boolean pttRelease();

//    /**
//     * causes toggles the transmission
//     * @return true if radio is now transmitting and false if radio is now not transmitting
//     */
//     boolean pttHold();

    /**
     * Sets the radio's PL tone.
     * Some implementations do not support this.
     * @param pltone a string that is one of the CTCSS frequencies
     * @return  a boolean to show command success
     */
     boolean setPlTone(final String pltone);

    /**
     *
     * Sets a repeater offset and repeater offset direction.
     * Some implementations do not support the setting of the offset, but still support the offset direction.
     *
     * @param repeaterOffset a long that is the repeater offset 600000 means 600 Khz
     * will also set the Negative or Positive offset, if 0 will set simplex
     * for virtual radios, the offset itself is not set, but the simplex, positive, and negative offset will
     * be set appropratly.
     * @return a boolean to indicate whether the operation completed successfully
     */
     
     boolean setRepeaterOffset(final long repeaterOffset);

    /**
     * Get status information about the radio.
     * @return a RadioStatus that contains radio's properties and settings.
     */
     RadioStatus getStatus();

     /**
      * Gets a radio and CAT mode indicator.  For example, if conntected to a Yeasu 897 via HRD would return
      * something like FT-897 (HRD).
      * @return a String identifying the radio and CAT communication mode
      */
     String getRadioID();

    /**
     * Indicates whether the radio is under CAT control.
     * @return a boolean that indicates that the radio is the expected radio and under CAT control.
     */
     boolean isThisRadioControlled();

    /**
     * Sets the radio's frequency
     * @param freq a long that is the desired frequency (for example, 10000000 is 10MHz)
     * @return a boolean to show successful completion
     */
     boolean setFreq(long freq);

    /**
     * Gets the radio's current frequency.
     * @return a long that is the current receive frequency
     */
     long getFreq();

    /**
     * Sets the radio's mode.
     * @param mode a Mode that specifies what mode the radio is to be set to
     * @return true if the mode was sucssessfully set.
     */
     boolean setMode(Mode mode);

    /**
     * Gets the radio's current operating mode.
     * @return a Mode that represents the radio's current mode
     */
     Mode getMode();


}
