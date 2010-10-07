package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.AbstractRadio;
import net.dbcrd.radiopackage.Mode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.dbcrd.radiopackage.RadioIdentification;
import net.dbcrd.radiopackage.RadioStatus;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class OmniRigStruct extends AbstractRadio {

    private static final Logger THE_LOGGER= Logger.getLogger(OmniRigStruct.class.getName());
    private final static Preferences PREFS= Preferences.userRoot().node(OmniRigStruct.class.getName());
    private BlockingQueue<PktInfo> client2serverQ;
    /**
     *
     */
     String commID;
    /**
     * command data to turn the PTT on
     */
     OmniRigSetTrnEnCmd pttOn;
    /**
     * command data  sent to turn the PTT off
     */
     OmniRigSetRecEnCmd pttOff;
    /**
     * command data  sent to set the radio lock on
     */
     OmniRigCmdData lockOn;
    /**
     * command data  sent to set the radio lock off
     */
     OmniRigCmdData lockOff;
    /**
     * command data  sent to turn the radio mode to FM
     */
     OmniRigSetFMCmd setModeFM;
    /**
     * command data  sent to set the radio mode to AM
     */
     OmniRigSetAMCmd setModeAM;
    /**
     * command data  sent to set the radio mode to CW upper
     */
     OmniRigCmdData setModeCW_U;
    /**
     * command data  sent to set the radio mode to CW lower (CWR)
     */
     OmniRigCmdData setModeCW_L;
    /**
     *command data  sent to set the radio mode to DIG
     */
     OmniRigCmdData setModeDIG_U;
    /**
     *command data  sent to set the radio mode to DIG
     */
     OmniRigCmdData setModeDIG_L;
    /**
     * command data  sent to set the radio mode to USB
     */
     OmniRigSetUSBCmd setModeUSB;
    /**
     * command data  sent to set the radio mode to LSB
     */
     OmniRigSetLSBCmd setModeLSB;
    /**
     *
     */
     OmniRigSetFreqCmd setFreq;
    /** */
     OmniRigInitCmd initRadio;
    /**
     *
     */
     Set<OmniRigGetStatusCmd> getStatus=new HashSet<OmniRigGetStatusCmd>();

/**
 *
 * @param radiofile a String
 * @param cmds a Set of OmniRigCmdData
 * @param client2serverQ a BlockingQueue that holds PktInfo entries
 */
    OmniRigStruct(
            final String radiofile,
            final Set<OmniRigCmdData> cmds,
            final BlockingQueue<PktInfo> client2serverQ) {

        super(RadioIdentification.OMNI_RIGP);
        this.client2serverQ=client2serverQ;
        lockOn=null;
        lockOff=null;


//        rStatus.setProperty(RadioStatus.CATTYPE, "OmniRig");
//        rStatus.setProperty(RadioStatus.ACTIVATED, "no");
//        rStatus.setProperty(RadioStatus.RADIO_NAME, radiofile);
        putStatus(RadioStatus.CATTYPE, "OmniRig");
        putStatus(RadioStatus.ACTIVATED, "no");
        putStatus(RadioStatus.RADIO_NAME, radiofile);

        for (OmniRigCmdData cmd : cmds) {
            if ("TRN_EN".equals(cmd.pmcmd)) {
                pttOn=(OmniRigSetTrnEnCmd) cmd;
                continue;
            }
            if ("REC_EN".equals(cmd.pmcmd)) {
                pttOff=(OmniRigSetRecEnCmd) cmd;
                continue;
            }
            if ("FM".equals(cmd.pmcmd)) {
                setModeFM=(OmniRigSetFMCmd) cmd;
                continue;
            }
            if ("AM".equals(cmd.pmcmd)) {
                setModeAM=(OmniRigSetAMCmd) cmd;
                continue;
            }
            if ("CW_U".equals(cmd.pmcmd)) {
                setModeCW_U=cmd;
                continue;
            }
            if ("CW_L".equals(cmd.pmcmd)) {
                setModeCW_L=cmd;
                continue;
            }
            if ("DIG_U".equals(cmd.pmcmd)) {
                setModeDIG_U=cmd;
                continue;
            }
            if ("DIG_L".equals(cmd.pmcmd)) {
                setModeDIG_L=cmd;
                continue;
            }
            if ("USB".equals(cmd.pmcmd)) {
                setModeUSB=(OmniRigSetUSBCmd) cmd;
                continue;
            }
            if ("LSB".equals(cmd.pmcmd)) {
                setModeLSB=(OmniRigSetLSBCmd) cmd;
                continue;
            }
            if ("FREQ".equals(cmd.pmcmd)) {
                setFreq=(OmniRigSetFreqCmd) cmd;
                continue;
            }
            if ("STATUS".equals(cmd.pmcmd)) {
                getStatus.add((OmniRigGetStatusCmd) cmd);
                continue;
            }
            if ("INIT".equals(cmd.pmcmd)){
                initRadio=(OmniRigInitCmd) cmd;
                continue;
            }

        }
    }

    /**
     * Activate the radio
     * @param args an array of objects. Must have args[0] being an instance of CommSetup
     * @return a boolean
     */
    @Override
    public  boolean activate(final Object... args) {
        if (args.length == 0 || (!(args[0] instanceof CommSetup))) {
            return false;
        }
        doActivate();
        CommSetup comm=(CommSetup) args[0];
        commID=comm.getSerialPortStr();
        try {
            if (null != this.initRadio) {
                try {
                    initRadio.doCmd(this, client2serverQ);
                } catch (IOException ex) {
                  THE_LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            getStatusProperties();
        } catch (IOException ioe) {
            THE_LOGGER.log(Level.SEVERE, null, ioe);

            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!getStatus().containsKey(RadioStatus.FREQ)) {
            return false;
        }

        commProps.setProperty("Baud", comm.getBaud());
        commProps.setProperty("Host", comm.getHostStr());
        commProps.setProperty("SerialPort", comm.getSerialPortStr());
        commProps.setProperty("TCPIPPort", comm.getTcpipPortStr());
        commProps.setProperty("Virtual", (comm.isVirtual()) ? "TRUE" : "FALSE");
       // rStatus.setProperty(RadioStatus.ACTIVATED, "yes");
        putStatus(RadioStatus.ACTIVATED, "yes");
        getMode();
        getFreq();
        isRadio=true;
        return true;
    }

/**
 *  initializes the controls
 */
    private void doActivate() {

        if (null != pttOn) {
            pttOn.initialize();
        }
        if (null != pttOff) {
            pttOff.initialize();
        }
        if (null != setModeFM) {
            setModeFM.initialize();
        }
        if (null != setModeAM) {
            setModeAM.initialize();
        }
        if (null != setModeCW_L) {
            setModeCW_L.initialize();
        }

        if (null != setModeCW_U) {
            setModeCW_U.initialize();
        }
        if (null != setModeDIG_L) {
            setModeDIG_L.initialize();
        }
        if (null != setModeDIG_U) {
            setModeDIG_U.initialize();
        }
        if (null != setModeUSB) {
            setModeUSB.initialize();
        }
        if (null != setModeLSB) {
            setModeLSB.initialize();
        }
        if (null != setFreq) {
            setFreq.initialize();
        }
        if (null != initRadio) {
            initRadio.initialize();
        }
        if (null != getStatus) {
            for (OmniRigGetStatusCmd cmd : getStatus) {
                cmd.initialize();
            }
        }
    }

    /**
     *
     * @return a RadioIdentification
     */
    @Override
    public RadioIdentification getRadioIdentification() {
        return this.radioId;
    }


    /**
     *
     * @param lock
     * @return a boolean always false
     */
    @Override
    public boolean setLock(final boolean lock) {
        THE_LOGGER.fine("setLock is not supported for OmniRig Radios");
        return false;
    }

    /**
     *
     * @return a boolean always false
     */
    @Override
    public boolean isLock() {
        THE_LOGGER.fine("isLock is not supported for OmniRig Radios");
        return false;
    }

    /**
     *
     * @param pltone
     * @return a boolean always false
     */
    @Override
    public boolean setPlTone(final String pltone) {
        THE_LOGGER.fine("setPlTone is not supported for OmniRig Radios");
        return false;
    }

    /**
     *
     * @param repeaterOffset
     * @return a boolean always false
     */
    @Override
    public boolean setRepeaterOffset(final long repeaterOffset) {
        THE_LOGGER.fine("setRepeaterOffset is not supported for OmniRig Radios");
        return false;
    }

    /**
     *
     * @return a String that contains the RADIO_NAME
     */
    @Override
    public String getRadioID() {
        return new StringBuilder().append(commID).append(": ").
                append(getStatus().getProperty(RadioStatus.RADIO_NAME)).toString();

    }

    /**
     *
     * @return a boolean that is True if the radio is activated, and the frequency returned from the radio is not zero
     */
    @Override
    public boolean isThisRadioControlled() {
        if ("yes".equals(getStatus().getProperty(RadioStatus.ACTIVATED))) {
            long freq=getFreq();
            return 0L != freq;
        } else {
            return false;
        }
    }

    /**
     *
     * @param freq a long that has the frequency to set the radio to.
     * @return a boolean, always true
     */
    @Override
    public boolean setFreq(final long freq) {
        Properties props=new Properties();
        try {
            props=setFreq.doCmd(this, client2serverQ, freq);
        } catch (IOException ex) {
            Logger.getLogger(OmniRigStruct.class.getName()).log(Level.SEVERE, null, ex);
        }
        rStatus.putAll(props); //TODO check that this is OK
        return true;
    }

    /**
     *
     * @return a long that is the frequency.
     */
    @Override
    public long getFreq() {
        try {
            rStatus.putAll(getStatusProperties());//TODO check that this is OK
        } catch (IOException ioe) {
            THE_LOGGER.log(Level.SEVERE, null, ioe);

            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        return Long.parseLong(rStatus.getProperty(PMValues.pmFreq.name(), "0"));
    }


   /**
    * 
    * @return a Properties 
    * @throws IOException
    */
    private Properties getStatusProperties() throws IOException {
        for (OmniRigGetStatusCmd statuscmd : getStatus) {
            rStatus.putAll(statuscmd.getStatus(this, client2serverQ)); //TODO varify that this is ok
        }
        modeFromStatus();
        return rStatus.getCopy();
 
    }

    /**
     *
     * @param mode a Mode to set the radio to.
     * @return a boolean true if
     */
    @Override
    public boolean setMode(final Mode mode) {
      //  BlkQRspHandler handler;
        Properties props;
        try {
            switch (mode) {
                case AM:
                    props=setModeAM.doCmd(this, client2serverQ);
                    break;
                case FM:
                    props=setModeFM.doCmd(this, client2serverQ);
                    break;
                case CW_U:
                    props=setModeCW_U.doCmd(this, client2serverQ);
                    break;
                case CW_L:
                    props=setModeCW_L.doCmd(this, client2serverQ);
                    break;
                case DIG_U:
                    props=setModeDIG_U.doCmd(this, client2serverQ);
                    break;
                case DIG_L:
                    props=setModeDIG_L.doCmd(this, client2serverQ);
                    break;
                case USB:
                    props=setModeUSB.doCmd(this, client2serverQ);
                    break;
                case LSB:
                    props=setModeLSB.doCmd(this, client2serverQ);
                    break;
                case UNSPECIFIED:
                default:
                    return false;

            }
            getStatusProperties();
        } catch (IOException ioe) {
            THE_LOGGER.log(Level.SEVERE, null, ioe);

            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

        switch (mode) {
            case AM:
                if (("TRUE".equals(getStatus().get("pmAM")))) {
                    putStatus(RadioStatus.MODE, "AM");
                    return true;
                }
                return false;
            case FM:
                if (("TRUE".equals(getStatus().get("pmFM")))) {
                    putStatus(RadioStatus.MODE, "FM");
                    return true;
                }
                return false;
            case CW_U:
                if (("TRUE".equals(getStatus().get("pmCW_U")))) {
                    putStatus(RadioStatus.MODE, "CW_U");
                    return true;
                }
                return false;
            case CW_L:
                if (("TRUE".equals(getStatus().get("pmCW_L")))) {
                    putStatus(RadioStatus.MODE, "CW_L:");
                    return true;
                }
                return false;
            case DIG_U:
                if (("TRUE".equals(getStatus().get("pmDIG_U")))) {
                    putStatus(RadioStatus.MODE, "DIG_U");
                    return true;
                }
                return false;
            case DIG_L:
                if (("TRUE".equals(getStatus().get("pmDIG_L")))) {
                    putStatus(RadioStatus.MODE, "DIG_L");
                    return true;
                }
                return false;
            case USB:
                if (("TRUE".equals(getStatus().get("pmSSB_U")))) {
                    putStatus(RadioStatus.MODE, "USB");
                    return true;
                }
                return false;
            case LSB:
                if (("TRUE".equals(getStatus().get("pmSSB_L")))) {
                    putStatus(RadioStatus.MODE, "LSB");
                    return true;
                }
                return false;
            case UNSPECIFIED:
            default:
                return false;

        }
    }

    /**
     *
     * @return a Mode from the radio properties
     */
    @Override
    public Mode getMode() {
        try {
            getStatusProperties();
        } catch (IOException ioe) {
            THE_LOGGER.log(Level.SEVERE, null, ioe);
            final JFrame window=null; // error dialog box centers on this window if present
            final String msg="Radio did not respond";
            JOptionPane.showMessageDialog(window, msg, ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        return modeFromStatus();
    }

    /**
     * Not supported yet
     * @param vargs an Object...
     * @return a boolean
     */
    @Override
    public boolean setDefaultComm(final Object... vargs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return a boolean true if no error, false if an IOException was caught
     */
    @Override
    public boolean pttPush() {
        try {
            pttOn.doCmd(this, client2serverQ);

        } catch (IOException ex) {
            Logger.getLogger(OmniRigStruct.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     *
     * @return a boolean true if no error, false if an IOException was caught
     */
    @Override
    public boolean pttRelease() {
        try {
            pttOff.doCmd(this, client2serverQ);
        } catch (IOException ex) {
            Logger.getLogger(OmniRigStruct.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     *  just mark the radio as no longer activated.
     *  To actually stop the radio the comm port need to be de-registered from the server.
     *  This is done by the OmniRigMakeRadio.stop();
     */
    @Override
    public void stop() {
        putStatus(RadioStatus.ACTIVATED, "no");  //causes isThisRadioControlled to be false
    }

   /**
    * Returns a Mode from the rStatus.
    * @return a Mode determined from the rStatus
    */
    private Mode modeFromStatus() {

        if ("TRUE".equals(rStatus.get("pmAM"))) {
            putStatus(RadioStatus.MODE, "AM");
            return Mode.AM;
        }
        if ("TRUE".equals(rStatus.get("pmFM"))) {
           putStatus(RadioStatus.MODE, "FM");
            return Mode.FM;
        }
        if ("TRUE".equals(rStatus.get("pmWFM"))) {
            putStatus(RadioStatus.MODE, "FM");
            return Mode.WFM;
        }
        if ("TRUE".equals(rStatus.get("pmCW_U"))) {
            putStatus(RadioStatus.MODE, "CWH");
            return Mode.CW_U;
        }
        if ("TRUE".equals(rStatus.get("pmCW_L"))) {
            putStatus(RadioStatus.MODE, "CWL");
            return Mode.CW_L;
        }
        if ("TRUE".equals(rStatus.get("pmDIG_U"))) {
            putStatus(RadioStatus.MODE, "DIG_U");
            return Mode.DIG_U;
        }
        if ("TRUE".equals(rStatus.get("pmDIG_L"))) {
            putStatus(RadioStatus.MODE, "DIG_L");
            return Mode.DIG_L;
        }
        if ("TRUE".equals(rStatus.get("pmSSB_U"))) {
           putStatus(RadioStatus.MODE, "USB");
            return Mode.USB;
        }
        if ("TRUE".equals(rStatus.get("pmSSB_L"))) {
            putStatus(RadioStatus.MODE, "LSB");
            return Mode.LSB;
        }
        putStatus(RadioStatus.MODE, "Unspecified");
        return Mode.UNSPECIFIED;
    }

    /**
     *
     * @param comdata
     */
    @Override
    public void saveCommPrefs(final CommSetup comdata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
