package net.dbcrd.radiopackage;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;
import net.dbcrd.radiopackage.omnirigserial.ClassPreamble;

/**
 * Abstract radio.  Base for implementations of RadioControl
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010", lastModified="8/14/2010")
 public abstract class AbstractRadio implements RadioControl {
private static final Logger THE_LOGGER=Logger.getLogger(AbstractRadio.class.getName());
    static final Integer ZERO_INT=0;
    /** key for obtaining the portname from the commProps */
    protected final static String PORT_NAME="PortName";
    /** indicates what radio is being driven*/
    protected RadioIdentification radioId=RadioIdentification.UNKNOWN;
    /**
     * a volatile boolean abortClientThread is set true to terminate threads communicating with
     * the CAT controlled radio.
     */
    private static volatile boolean abortClientThread=false;
    private static final Object LOCK1=new Object();

    /**
     *
     * @return a boolean that, if true, indicates that the client thread is to be aborted.
     */
     public static boolean isAbortClientThread() {
        synchronized (LOCK1) {
            return abortClientThread;
        }
    }

    /**
     *
     * @param abortClientThread
     */
     public static void setAbortClientThread(final boolean abortClientThread) {
        synchronized (LOCK1) {
            AbstractRadio.abortClientThread=abortClientThread;
        }
    }
    /**
     *  a RadioStatus that gathers and semi-maintains status about the radio.
     */
    protected final RadioStatus rStatus=new RadioStatus();
    /** a boolean that indicates if a radio has been identified */
    protected boolean isRadio=false;
    /** a Properties that contains the communication parameters of relevance to the client */
    protected Properties commProps=new Properties();
    /** true if radio is connected vie TCP/IP */
    protected boolean virtual;
    /** true if the communication was successfully established */
    CommSetup commSetup;

    /**
     * Get a clone of the communications related information
     * @return a Properties that has comunication related information.
     */
    @Override
    public final Properties getCommunicationInfo() {
        return (Properties) commProps.clone();
    }

    /**
     * Returns the currenly known status of the radio.  May not track user manipulation of the radio and may
     * be out of date by a couple of seconds.
     * @return a RadioStatus (a subclass of Properties) with currently known status.
     */
    @Override
    public final RadioStatus getStatus() {
        if (isRadio) {
            return rStatus.getCopy();
        }
        return new RadioStatus();
    }

    /**
     * save communication preferences
     * @param comdata a CommSetup that ??
     */
    public abstract void saveCommPrefs(final CommSetup comdata);

    /**
     * @deprecated
     * 
     * Sets the radioId to UNKNOWN.
     * @param portName a String that specifies the port name, if "", then
     * the radio name is set to "abstractRadio" and the Port is "".
     * If the portName is not "", it is made uppercase and trimed and stored on CommProps.
     */
     AbstractRadio(final String portName) {
        super();
        if (portName.isEmpty()) {
            //  this.portName="";
            commProps.put(PORT_NAME, "");
            putStatus(RadioStatus.RADIO_NAME, "abstractRadio");
           // rStatus.setProperty(RadioStatus.RADIO_NAME, "abstractRadio");
           // rStatus.setProperty(RadioStatus.CATTYPE, "NoCAT");
            putStatus(RadioStatus.CATTYPE, "NoCAT");
            //TODO should radioId be updated?
            return;
        }
        commProps.put(PORT_NAME, portName.trim().toUpperCase());

    }
     public final void putStatus(final String key, final String val){
         rStatus.setProperty(key,val);
     }

    /**
     *
     * @param radioID a RadioIdentification
     */
    public AbstractRadio(final RadioIdentification radioID) {
        super();
        commProps.put(PORT_NAME, radioID.toString());
//        rStatus.setProperty(RadioStatus.RADIO_NAME, "abstractRadio");
//        rStatus.setProperty(RadioStatus.CATTYPE, "NoCAT");
        putStatus(RadioStatus.RADIO_NAME, "abstractRadio");
        putStatus(RadioStatus.CATTYPE, "NoCAT");
        radioId=radioID;
        AbstractRadio.setAbortClientThread(false);
        THE_LOGGER.severe("PROBLEM in the set status stuff, you always return a copy!!!!, that cannot be set find all and use setStatus of this routine");
    }

    /**
     *
     *
     * @return a RadioIdentification
     */
    @Override
    public RadioIdentification getRadioIdentification() {
        return RadioIdentification.UNKNOWN;
    }

    /**
     * do not call on the AWT Thread
     * used to change the cat parametrs
     */
    @Override
    public void editSelf() {
        final String[] args={virtual ? "virtual" : "comm"};
        commSetup=CommSetupInput.main(args);
        this.saveCommPrefs(commSetup);
    }

    /**
     *
     * @param val an integer that holds the value that is to be converted to BCD
     * @param numChar the number of BCD characters that are to be returned.
     * @return an array of bytes of BCD characters
     */
     public static byte[] int2BCD(final int val, final int numChar) {
        final String vals=Integer.toString(val);
        final int strlen=vals.length();
        final int numBytes=(numChar + 1) >> 1;
        if (numChar < 1 || numChar < strlen) {
            return new byte[]{0};
        }
        final byte[] result=new byte[numBytes];
        for (int i=0; i < numBytes; i++) {
            result[i]=0;
        }
        final byte[] strbytes=vals.getBytes();
        final Deque<Integer> charStack=new LinkedList<Integer>();
        for (int i=strbytes.length - 1; i >= 0; i--) {
            charStack.offerFirst(Integer.valueOf(strbytes[i]));
        }
        final int lmit=(numBytes << 1) - charStack.size();
        for (int i=0; i < lmit; i++) {
            charStack.offerFirst(ZERO_INT);
        }
        assert charStack.size() == numBytes << 1;
        for (int i=0; i < numBytes; i++) {
            final int upperVal=((charStack.pop().intValue() - '0') & 0xF) << 4;
            final int lowerVal=((charStack.pop().intValue() - '0') & 0xF);
            final int value=upperVal | lowerVal;
            result[i]=(byte) value;
        }
        return result;
    }

    /**
     *
     * @param data
     * @return
     */
     static String printHex(final byte[] data) {
        if (data.length == 0) {
            return "";
        }

        final StringBuilder hexline=new StringBuilder();
        for (byte byt : data) {
            hexline.append(String.format("%02x ", byt));
        }
        return hexline.toString().trim().toUpperCase();
    }

    /**
     *
     * @param bcd a byte in bcd format. Throws an IllegalArgumentException if the value in the byte is not a valid BCD value
     *
     * @return an integer represnetation of the bcd value.
     */
     static int bcdByte2Int(final byte bcd) {
        final int topi=(bcd >> 4) & 0xF;
        final int boti=bcd & 0xF;
        if (topi > 9 || boti > 9) {
            throw new IllegalArgumentException();
        }
        return topi * 10 + boti;
    }

    /**
     *
     * @param inbyte a binary byte.  If inbyte is >99 or <0 then an IllegalArgumentException is thrown
     * @return a BCD byte
     */
     static byte byte2BcdByte(final byte inbyte) {
        if (inbyte > 99 || inbyte < 0) {
            throw new IllegalArgumentException();
        }
        final int top=inbyte / 10;
        final int bot=inbyte - top * 10;
        return (byte) ((top << 4) + bot);
    }
}
