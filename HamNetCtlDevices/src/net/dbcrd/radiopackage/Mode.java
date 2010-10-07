
package net.dbcrd.radiopackage;

import net.dbcrd.radiopackage.omnirigserial.ClassPreamble;

/**
 * Mode enum for specifying radio operation modes.
 * This matches the modes available with the FT-8x7 radio.  Should work for
 * most others.
 * 
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
public enum Mode {

    /**
     * unknown mode
     */
    UNSPECIFIED,
    /**
     * AM - mode
     */
    AM,
    /**
     * FM - mode
     */
    FM,
    /**
     * CW - mode
     */
    CW_U,
    /**
     * CWR - mode
     */
    CW_L,
    /**
     * USB - mode
     */
    USB,
    /**
     * LSB - mode
     */
    LSB,
    /**
     * WFM -mode
     */
    WFM,
    /**
     * DIG - mode
     */
    DIG_U,
    DIG_L;

}
