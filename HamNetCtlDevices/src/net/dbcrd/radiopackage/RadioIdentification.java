
package net.dbcrd.radiopackage;

import java.util.regex.Pattern;

/**
 * Specifies which radios are supported and how.
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")

public enum RadioIdentification {
    /**
     * HRD - Ham Radio Deluxe Server - Radio is connected to a TCP/IP link to Ham Radio Deluxe, which controls the physical radio.
     */
    HRD("Ham Radio Deluxe Server", true,".*","RadioHRD",true),
    /**
     * Omni Rig server, not yet implemented
     */
    OMNI_RIGS("Omni Rig Server", true,".*","RadioOmniRig",false),
    /**
     * Omni Rig physical -- Physical radio is connected via a BlkServer and defined by an OmniRig radio.ini deffinition file.  It is thread safe.
     */
    OMNI_RIGP("Omni Rig Physical", false,".*","OmniRigMakeRadio",true),
    /**
     * Yaesu FT8x7 -  A Yaesu FT-8x7 type radio that is physically connected and that has an rs-232 connection.  It is not thread safe.
     */
    //YAESU_FT8X7("FT-8x7", false, "FT\\s*-?\\s*8\\d7\\s*D","RadioFT897",true),
   /**
    * Unknown
    */
    UNKNOWN ("Unknown",false,".*","Unknown",false);

    private final String description;
    private final boolean virtual;
    private final Pattern pattern;
    private final String classname;
    private final boolean enabled;

    /**
     * enum setup
     *
     * @param description a String with a human readable description
     * @param virtual a boolean, true specifies tcp/ip, false is a direct connection
     * @param regex a Pattern (case insensitive) for recognizing the radio ID
     * @param classname a String to specify which class is the driver
     * @param enabled a boolean to specify whether the radio is available
     */
    private RadioIdentification(final String description,final boolean virtual, final String regex,
            final String classname,final boolean enabled) {
        this.description=description;
        this.virtual=virtual;
        this.pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        this.classname=classname;
        this.enabled=enabled;
    }

   /**
    * Get the name of the class associated with the enum
    * @return a String that represents the name of the class used to implement the CAT
    */
    public String getClassName(){
        return new StringBuilder(classname).toString();
    }
    /**
     *
     * @return a String that is a copy of the description
     */
    @Override
    public String toString() {
        return new StringBuilder(description).toString();
    }
    /**
     *
     * @return a boolean, true if the enum is available.
     */
    public boolean isEnabled(){
        return enabled;
    }
    /**
     *
     * @return a boolean, true if a tcp/ip connection
     */
    public boolean isVirtual(){
        return virtual;
    }
    /**
     * 
     * @return a Pattern used to recognize the radio type
     */
    public Pattern getPattern(){      
        return pattern;
    }
}
