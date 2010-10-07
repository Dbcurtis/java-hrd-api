package net.dbcrd.radiopackage;

import java.text.DecimalFormat;
import java.util.Properties;

/**
 * A subclass of properties that contains any radio information of interest.
 * Do not use in collections.
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
public final class RadioStatus extends Properties {

    static final long serialVersionUID=287347672939492L;
    /**
     *
     */
    public static final String FREQ="pmFreq";
    /**
     *
     */
    public static final String MODE="Mode";
    /**
     * key for for obtaining the radio name from rStatus
     */
    public final static String RADIO_NAME="RadioName";
    /**
     *
     */
    public final static String REPT="Repeater";
    /**
     *
     */
    public final static String THREAD="ClientThread";
    /**
     *
     */
    public final static String DEAMON="Daemon";
    /**
     *
     */
    public final static String SERVER="Server";
    /**
     *
     */
    public final static String SVERSION="SVersion";
    /**
     *
     */
    public final static String SRADIOS="ServedRadios";
    /**
     *
     */
    public final static String CMD_STATUS="CMD_STAT";
    /**
     *
     */
    public final static String ROP1="Radio_Opt1";
    /**
     *
     */
    public final static String ROP2="Radio_Opt2";
    /**
     *
     */
    public final static String CATTYPE="CatType";
    /**
     *
     */
    public final static String ACTIVATED="Activated";
    /**
     *
     */
    DecimalFormat dfmt=new DecimalFormat(); // cannot use multiple threads with this.

    {
        dfmt.setGroupingSize(3);
        dfmt.setGroupingUsed(true);
    }

    RadioStatus() {
        super();
        setProperty(CATTYPE, "No Connect");
        setProperty(RADIO_NAME, "None");
        setProperty(FREQ, "0");
        setProperty(MODE, "??");
    }

    /**
     * Get a clone of the RadioStatus.
     * @return a RadioStatus that is a clone of the current status.
     */
    public synchronized RadioStatus getCopy() {
        return (RadioStatus) super.clone();
    }



    /**
     *
     * @return a StringBuilder of the form cat: radio,  xxx,xxx,xxx - (md)
     */
    public synchronized StringBuilder getStatusSB() {
        final StringBuilder result=new StringBuilder().
                append(super.getProperty(CATTYPE)).
                append(": ").append(super.getProperty(RADIO_NAME)).
                append(", ").
                append(dfmt.format(Long.parseLong(super.getProperty(FREQ)))).
                append(" - (").append(super.getProperty(MODE)).append(
                ')');
        return result;
    }
    @Override
    public synchronized String  getProperty(final String key){
      return  super.getProperty(key);
    }
    @Override
    public synchronized String getProperty(final String key, final String defaultValue){
        return super.getProperty(key,defaultValue);
    }
    @Override
    public synchronized Object setProperty(final String key, final String value){
        return super.setProperty(key, value);
    }
}
