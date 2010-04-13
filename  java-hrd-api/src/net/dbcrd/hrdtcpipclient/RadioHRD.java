package net.dbcrd.hrdtcpipclient;

import com.vladium.utils.PropertyLoader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for CAT control of a radio through Ham Radio Deluxe.
 *
 * @author Daniel B. Curtis N6WN
 *
 * Code based on <a href="http://www.ham-radio-deluxe.com/Support/Interfaces/HRD50TCPIPInterface.aspx"> Ham Radio Deluxe TCP/IP</a> and conversations
 * with Simon Brown.
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
 */
public class RadioHRD extends AbstractVirtualRadio {
    /** used to indicate that HRD is not communicating correctly.*/
    public static final String HRD_NOT_CONNECTED = "HRD Radio not connected";
    private static final String LOCK = "lock";
    private static final String FREQ = "Freq";
    private static final Logger THE_LOGGER=Logger.getLogger(RadioHRD.class.getName());
    private static final long UNSIGNED_INT_MASK=0xFFFFFFFFL;
    private static final long UNSIGNED_BYTE_MASK=0XFF;
    private static final long SANITY1=0x1234ABCD&UNSIGNED_INT_MASK;
    private static final long SANITY2=0xABCD1234&UNSIGNED_INT_MASK;

    //context insensitive commands
    private final Command getRadio=new Command("Get Radio");
    private final Command getContext=new Command("Get Context");
    //HRD context --- Current implementation only uses the first context
    private String hrdContext="";
    //context sensitive commands
    private CtxCommand getFreqCmd=null;
    private CtxCommand getLockCmd=null;
    private CtxCommand pushPttCmd=null;
    private CtxCommand releasePttCmd=null;
    private CtxCommand setLockCmd=null;
    private CtxCommand releaseLockCmd=null;
    private CtxCommand getModeCmd=null;
    private CtxCommand setSimplexCmd=null;
    private CtxCommand setPosOffCmd=null;
    private CtxCommand setNegOffCmd=null;
    private CtxCommand setBlockOnCmd=null;
    private CtxCommand setBlockOffCmd=null;
    // thread for the HRD client
    Thread clientThread=null;
    // set of radio buttons from the get buttons command
    final private Set<String> radioButtons=new HashSet<String>();
    // map of dropdown names and values for the dropdown
    private final Map<String, List<String>> radioDropDowns=new HashMap<String, List<String>>();
    // used for special processing in CtxCommand to validate the button-select command
    private static final Pattern BUT_SEL=Pattern.compile(
            "(?<=\\s{0,10}button-select\\s{0,10})\\w+", Pattern.CASE_INSENSITIVE);
    // used for special processing in CtxCommand to complete the set dropdown XXX YY # command
    private static final Pattern SET_DROPDN=Pattern.compile(
            "(?<=\\s{0,10}set\\s{1,10}dropdown\\s{0,10})(\\w+)\\s+([A-Z'+~_-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CTX_RADIO_PAT=Pattern.compile("(\\d+):([A-Za-z0-9 _-]+),?");
    // private static final Pattern FREQ_CTZ_PAT=Pattern.compile("(\\d+)-(\\d+),?");
    // map of contexts to radio names --- only one context implemented
    private final Map<String, String> context2RadioMap=new HashMap<String, String>();
    private final Map<String, String> radio2ContextMap=new HashMap<String, String>();

    private final void initiCtxCommands() {
        getFreqCmd=new CtxCommand("get frequency");
        setLockCmd=new CtxCommand("set button-select Lock 1");
        releaseLockCmd=new CtxCommand("set button-select Lock 0");
        getLockCmd=new CtxCommand("get button-select Lock");
        pushPttCmd=new CtxCommand("set button-select TX 1");
        releasePttCmd=new CtxCommand("set button-select TX 0");
        getModeCmd=new CtxCommand("get dropdown-text {Mode}");
        setSimplexCmd=new CtxCommand("set dropdown repeater simplex");
        setPosOffCmd=new CtxCommand("set dropdown repeater '+'~shift");
        setNegOffCmd=new CtxCommand("set dropdown repeater '-'~shift");
        setBlockOnCmd=new CtxCommand("set block");
        setBlockOffCmd=new CtxCommand("set unblock");
    }

    //   private final Map<String, String> context2FreqMap=new HashMap<String, String>();

    private String doCmd(final CtxCommand cmd) {
        final RspHandler handler=new RspHandler();
        cmd.send(handler);
        return handler.waitForResponse();
    }

    private String doCmd(final Command cmd) {
        final RspHandler handler=new RspHandler();
        cmd.send(handler);
        return handler.waitForResponse();
    }

    /**
     * Instanitate a RadioHRD object.
     * @param computer a String used to identify a computer ("localhost" etc.)
     * @param port an int that is the port number to be used.
     */
    public RadioHRD(final String computer, final int port) {
        super(RadioIdentification.HRD);
        if(initalizeObject(computer, port)){ // TODO do something with the return
            return;
        }
    }

    /**
     * Instanitate a RadioHRD object.
     * Uses the property file to get the HRDserver name, and port and trys to connect.
     * May wait up to 9 sec for the conection to take place.  Then will try the
     * default localhost 7809 and may wait up to 9 more seconds for the connection.
     * Should attempt an operation (like set mode) to see if a connection was made.
     */
    public RadioHRD() {
        super(RadioIdentification.HRD);
        final Properties appProps=PropertyLoader.loadProperties("net.dbcrd.hrdtcpipclient.HRDServerInfo.properties");

        if(initalizeObject(
                appProps.getProperty("computer", "localhost"),
                Integer.valueOf(appProps.getProperty("port", "7809")))){
            return;
        } else{
            if(initalizeObject("localhost", 7809)){
                return; //TODO do something with the return
            }
        }
    }

    /**
     *
     * @param hostname
     * @param port
     * @return
     */
    private boolean initalizeObject(final String hostname, final int port) {
        this.port=port;
        boolean allOK=false;
        try{
            this.inetAddress=InetAddress.getByName(hostname);
            connectToRadioServer();
        } catch(UnknownHostException uhe){
            THE_LOGGER.log(Level.SEVERE, hostname+" not known********", uhe);
            return false;
        } catch(IOException ioe){
            THE_LOGGER.log(Level.SEVERE, "Cannot Connect to HRD server", ioe);
            return false;
        }
        try{
            if(initialSetup()){
                return false;
            }
            hrdContext="1";
//            String tempstr=doCmd(new Command("Get frequencies"));
//            mymx=FREQ_CTZ_PAT.matcher(tempstr);
//            idx=0;
//            while(mymx.find(idx)){
//                final String freq=mymx.group(1).trim();
//                final String contextst=mymx.group(2).trim(); //TODO not really sure what the -x does... cannot be the context tho
//                idx=mymx.end();
//                context2FreqMap.put(contextst, freq); //TODO multiple contexts not yet implemented
//            }
            String radio=doCmd(getRadio);

            if("error".equals(radio)||"".equals(radio)){
                THE_LOGGER.warning("HRD did not return a radio");
                return false;
            }
            rStatus.put(RADIO_NAME, radio);
            hrdContext=doCmd(getContext);
            if("error".equals(hrdContext)||"".equals(hrdContext)){
                return false;
            }
            setupButtonsDropdowns();
            initiCtxCommands();
            try{
                doCmd(setBlockOnCmd);
                int cnt=5;
                while(cnt-->=0){
                    doCmd(setLockCmd); //TODO what if incorrect response?
                    if("1".equals(doCmd(getLockCmd))){
                        break;
                    }
                }

                if(cnt<0){
                    return false;
                }
                rStatus.put(LOCK, "on");
                final String freqSt=doCmd(getFreqCmd);
                if("0".equals(freqSt)){
                    return false;
                }
                rStatus.put(FREQ, freqSt);
                allOK=true;

                return true;
            } finally{
                doCmd(setBlockOffCmd);
            }
        } finally{
            if(allOK){
                isRadio=true;
            } else{
                abortClientThread=true;
            }
        }
    }

    private final boolean initialSetup() {
        rStatus.put(FREQ, "unknown");
        clientThread=new Thread(client);
        clientThread.setName("HRD Client");
        rStatus.put("clientThread", "HRD Client");
        final ThreadGroup mygroup=Thread.currentThread().getThreadGroup();
        clientThread.setPriority(mygroup.getMaxPriority());
        clientThread.setDaemon(true);
        clientThread.start();
        rStatus.put("Daemon", "yes");
        final String serverID=doCmd(new Command("Get ID"));
        rStatus.put("Server", serverID);
        if("error".equalsIgnoreCase(serverID)){
            THE_LOGGER.severe("Incorrect response from HRD... ");
            return true;
        }
        final String serverVersion=doCmd(new Command("Get version"));
        rStatus.put("Server_version", serverVersion);
        final String tempstr=doCmd(new Command("Get radios"));
        rStatus.put("Served_radios", tempstr);
        final Matcher mymx=CTX_RADIO_PAT.matcher(tempstr);
        int idx=0;
        while(mymx.find(idx)){
            final String contextSt=mymx.group(1).trim();
            final String radioSt=mymx.group(2).trim();
            idx=mymx.end();
            context2RadioMap.put(radioSt, contextSt); //TODO multiple contexts not yet implemented
            radio2ContextMap.put(contextSt, radioSt);
        }
        return false;
    }

    private final void setupButtonsDropdowns() {
        radioButtons.clear();
        radioButtons.addAll(Arrays.asList(doCmd(new CtxCommand("Get Buttons")).toLowerCase().replace(" ", "~").
                split(",")));
        rStatus.put("radio_Options1", radioButtons.toString());
        for(String str:radioButtons){
            rStatus.put(str, "state unknown");
        }
        radioDropDowns.clear();
        final String[] vals=doCmd(new CtxCommand("Get Dropdowns")).toLowerCase().replace(" ", "~").split(",");
        for(String st:vals){
            final StringBuilder sbTemp=new StringBuilder("get dropdown-list {");
            sbTemp.append(st).append('}');
            radioDropDowns.put(st,
                               new LinkedList<String>(Arrays.asList(doCmd(new CtxCommand(sbTemp.toString())).
                    toLowerCase().replace(" ", "~").split(","))));
        }
        rStatus.put("radio_Options2", radioDropDowns.toString());
        for(String str:radioDropDowns.keySet()){
            rStatus.put(str, "state unknown");
        }
    }
    final static Pattern EXTRACT_MODE_PAT=Pattern.compile("(?<=mode:.{0,4})[A-Z]+", Pattern.CASE_INSENSITIVE);

    /**
     *
     * @param modestr
     * @return
     */
    private Mode decodeMode(final String modestr) {
        String modest;
        final Matcher mymx=EXTRACT_MODE_PAT.matcher(modestr);
        if(mymx.find()){
            modest=mymx.group().trim().toUpperCase();
        } else{
            return Mode.UNSPECIFIED;
        }
        return Mode.valueOf(modest);
    }

    @Override
    public boolean pttPush() {
        boolean result=false;
        if(isRadio){
            doCmd(pushPttCmd);
            result=true;
        }
        return result;
    }

    @Override
    public boolean pttRelease() {
        boolean result=false;
        if(isRadio){
            doCmd(releasePttCmd);
            result=true;
        }
        return result;
    }

    /**
     *
     * @param freq a long that is the frequency to set the radio to in hz
     * @return true if freq is 0 or if the frequency was set and the same frequency was read back from the radio.
     *
     */
    @Override
    public boolean setFreq(final long freq) {
        if(!isRadio){
            return false;
        }
        if(freq==0L){
            return true;
        }
      
        try{
            boolean finalResult=false;
            final StringBuilder sbTemp=new StringBuilder("set frequency-hz ");
            final String freqst=Long.toString(freq);
            sbTemp.append(freqst);
            doCmd(setBlockOffCmd);
            doCmd(new CtxCommand(sbTemp.toString()));
            doCmd(setBlockOnCmd);
            
            if(freqst.equals(doCmd(getFreqCmd))){
                finalResult=true;
            }
            rStatus.put("Freq", freqst);
            return finalResult;
        } finally{
            doCmd(setBlockOffCmd);
        }
    }

    /**
     * Set the radio's mode.
     * @param mode a Mode
     * @return boolean false if radio not recognized or mode not able to be set after 10 attempts, otherwise true
     */
    @Override
    public boolean setMode(final Mode mode) {
        if(isRadio){
            try{
                CtxCommand cmd;
                switch(mode){
                    case FM:
                        cmd=new CtxCommand("set dropdown Mode FM");
                        break;
                    case AM:
                        cmd=new CtxCommand("set dropdown Mode AM");
                        break;
                    case CW:
                        cmd=new CtxCommand("set dropdown Mode CW");
                        break;
                    case CWR:
                        cmd=new CtxCommand("set dropdown Mode CWR");
                        break;
                    case DIG:
                        cmd=new CtxCommand("set dropdown Mode DIG");
                        break;
                    case USB:
                        cmd=new CtxCommand("set dropdown Mode USB");
                        break;
                    case LSB:
                        cmd=new CtxCommand("set dropdown Mode LSB");
                        break;
                    default:
                        return false;
                }
                int i = 10;
                while(i-->=0){
                    doCmd(setBlockOffCmd);
                    doCmd(cmd);
                    doCmd(setBlockOnCmd);
                    if(decodeMode(doCmd(getModeCmd)).equals(mode)){
                        break;
                    }
                    try{
                        Thread.sleep(300);
                    } catch(InterruptedException ex){
                        Logger.getLogger(RadioHRD.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(i>=0){
                rStatus.put("mode", mode.toString());
                return true;
                }
            } finally{
                doCmd(setBlockOffCmd);
            }
        }
        return false;
    }

    /**
     * Get the radio's frequency.
     * @return a long representing the current frequency of the radio, or 0 if the radio is not recognized
     */
    @Override
    public long getFreq() {
        if(isRadio){
            try{
                doCmd(setBlockOnCmd);
                final String freq=doCmd(getFreqCmd);
                rStatus.put(FREQ, freq);
                return Long.valueOf(freq);
            } finally{
                doCmd(setBlockOffCmd);
            }
        }
        return 0L;
    }

    /**
     * Gets the radio's mode.
     * @return a Mode (or Mode.UNSPECIFIED if radio is not recognized).
     */
    @Override
    public Mode getMode() {
        if(isRadio){          
            doCmd(setBlockOnCmd);
            try{
                final Mode mymode=decodeMode(doCmd(getModeCmd).toUpperCase().replace(" ", "~").trim());
                rStatus.put("mode", mymode.toString());
                return mymode;
            } finally{
                doCmd(setBlockOffCmd);
            }
        }
        return Mode.UNSPECIFIED;
    }

    /**
     * Identifies the radio and CAT mode.  The CAT mode is (HRD).  The radio is the id from HRD.
     * @return a String containing the radioID or HRD_NOT_CONNECTED if radio is not recognized.
     */
    @Override
    public String getRadioID() {
        if(isRadio){
            return rStatus.getProperty(RADIO_NAME)+" (HRD)";
        }
        return "HRD Radio not connected";
    }

    /**
     * If the radio recognised, returns whether the radio is locked.  If the radio is not recognised, returns false.
     * @return a boolean true if the radio recognised and is locked, false if the radio is not recognised, or recognised and not locked
     */
    @Override
    public boolean isLock() {       
        if(isRadio){
            try{
                doCmd(setBlockOnCmd);
                final boolean locked="1".equals(doCmd(getLockCmd));
                rStatus.put(LOCK, locked?"yes":"no");
                return locked;
            } finally{
                doCmd(setBlockOffCmd);
            }
        }
        return false;
    }

    /**
     * Returns whether radio is currently under CAT control.
     * @return a boolean true if under CAT control, false otherwise.
     */
    @Override
    public boolean isThisRadioControlled() {
        final boolean first=isRadio&&!AbstractRadio.abortClientThread;
        boolean second;
        if(client==null){
            second=false;
        } else{
            second=client.isActive();
        }

        return first&&second;
    }

    /**
     * Not implemented. Logs a severe error if anything other than "0" specified.
     * @param pltone a String.
     * @return true if pltone is "0", false otherwise.
     */
    @Override
    public boolean setPlTone(final String pltone) {
        if(isRadio){
            rStatus.put("PLtone", "unknown");
            if(pltone.isEmpty()||"0".equals(pltone)){
                return true;
            } else{
                THE_LOGGER.severe("Unknown how to implemenmt setPLTone");
                return false;
            }
        }
        return false;
    }

 /**
  * Sets the radio's "lock" control. Will try 5 times to set and confirm.
  * @param lock a boolean: true to lock, false to unlock. 
  * @return  a boolean indicates that the operation succeeded.
  */
    @Override
    public boolean setLock(final boolean lock) {
        final CtxCommand cmd=(lock)?setLockCmd:releaseLockCmd;

        int trys=5;
        while(trys-->0){
            if(isRadio){
                try{
                    while(true){
                        doCmd(setBlockOffCmd);
                        doCmd(cmd);
                        try{
                            Thread.sleep(500);
                        } catch(InterruptedException ex){
                            Logger.getLogger(RadioHRD.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        doCmd(setBlockOnCmd);
                        final String res=doCmd(getLockCmd);
                        if(lock){
                            if("1".equals(res)){
                                rStatus.put(LOCK, "yes");
                                return true;
                            }
                        } else{
                            if("0".equals(res)){
                                rStatus.put(LOCK, "no");
                                return true;
                            }
                        }
                    }
                } finally{
                    doCmd(setBlockOffCmd);
                }
            }
        }

        THE_LOGGER.severe("Lock did not change state");
        return false;
    }

    /**
     * Sets a repeater offset and repeater offset direction. Does not see the repeater offset, but will set
     * the negative/positive/simplex operation responsive to repeaterOffset
     * @param repeaterOffset a long: if 0 radio is set to simplex, if negative to negative repeater offset, and
     * if positive to positive repeater offset.
     * @return a boolean true - always.
     */
    @Override
    public boolean setRepeaterOffset(final long repeaterOffset) { //TODO add confirm and retry
        final RspHandler handler=new RspHandler();
        if(repeaterOffset==0L){
            rStatus.put("repeater", "simplex");
            setSimplexCmd.send(handler);
        } else{
            if(repeaterOffset<0L){
                setNegOffCmd.send(handler);
                rStatus.put("repeater", "negative");
            } else{
                setPosOffCmd.send(handler);
                rStatus.put("repeater", "positive");
            }
        }
        handler.waitForResponse(); //TODO check the response
        return true;
    }

    /*
     * --------------------------------------------------------------------------
     */

    /**
     * Class for HRD commands that require a context
     */
    private class CtxCommand {

        private String commandText="";
        final private Command myCommand;
        private boolean valid=true;

        /**
         *
         * @param messageTxt  a String that contains the command, the context will be automatically added
         * If a button or dropdown is specified, it is checked to make sure it is known by HRD. Where needed, it
         * will also set the index.
         * If incorrect, will throw an IllegalArgumentException
         */
        CtxCommand(final String messageTxt) {

            commandText=messageTxt.trim();
            Matcher cmdTextMx=BUT_SEL.matcher(commandText);
            if(cmdTextMx.find()){
                final String button=cmdTextMx.group();
                if(!radioButtons.contains(button.toLowerCase())){
                    valid=false;
                    THE_LOGGER.severe("*************** Missing button "+button);
                    throw new IllegalArgumentException();
                }
            }
            cmdTextMx=SET_DROPDN.matcher(commandText);
            if(cmdTextMx.find()){
                final String arg1=cmdTextMx.group(1);
                final String arg2=cmdTextMx.group(2);
                if(!radioDropDowns.containsKey(arg1.toLowerCase())){
                    valid=false;
                    THE_LOGGER.severe("*************** Missing dropdown "+arg1);
                    throw new IllegalArgumentException();
                }
                final List<String> arg2vals=radioDropDowns.get(arg1.toLowerCase());
                int idx=0;
                for(String sg:arg2vals){
                    if(sg.equalsIgnoreCase(arg2)){
                        break;
                    }
                    idx++;
                }
                if(idx>=arg2vals.size()){
                    valid=false;
                    THE_LOGGER.severe("*************** Missing dropdown argument "+arg1);
                    throw new IllegalArgumentException();
                }
                commandText=commandText+" "+Integer.toString(idx);
            }

            final StringBuilder sbTemp=new StringBuilder();
            sbTemp.append("[").append(hrdContext).append("] ").append(commandText);
            myCommand=new Command(sbTemp.toString());
        }

        @Override
        public String toString() {
            if(null==myCommand){
                return "EMPTY";
            }
            return myCommand.toString();
        }

        boolean send(final RspHandler handler) {
            if(!valid){
                return false;
            }
            return myCommand.send(handler);
        }
    }

    /**
     * Class for HRD commands that do not require a context or that already have the context in the string.
     */
    private class Command {

        private final long nSize; //unsigned 32 bit int.  0xFFFFFFFF
        private final long nSanity1;
        private final long nSanity2;
        private final long nChecksum;
        private final StringBuilder szTextsb=new StringBuilder();

        /**
         *
         * @param messageTxt a String that contains the command.
         */
        Command(final String messageTxt) {
            nSanity1=SANITY1;
            nSanity2=SANITY2;
            nChecksum=0L;
            szTextsb.append(messageTxt);
            nSize=setSize();
        }

        protected void storeUnsignedInt(final byte[] result, final int idxin, final long inSize) {
            int idx=idxin;
            result[idx++]=(byte) ((inSize)&UNSIGNED_BYTE_MASK);
            result[idx++]=(byte) ((inSize>>8)&UNSIGNED_BYTE_MASK);
            result[idx++]=(byte) ((inSize>>16)&UNSIGNED_BYTE_MASK);
            result[idx++]=(byte) ((inSize>>24)&UNSIGNED_BYTE_MASK);
        }

        long getSize() {
            return nSize;
        }

        final long setSize() {
            int result=0;
            result+=4*4;
            result+=2*szTextsb.length();
            result+=6;
            return (UNSIGNED_INT_MASK&result);
        }

        boolean send(final RspHandler handler) {
            boolean result=false;
            int idx=0;
            byte[] temp=new byte[(int) nSize];
            storeUnsignedInt(temp, idx, nSize);
            idx+=4;
            storeUnsignedInt(temp, idx, nSanity1);
            idx+=4;
            storeUnsignedInt(temp, idx, nSanity2);
            idx+=4;
            storeUnsignedInt(temp, idx, nChecksum);
            idx+=4;

            final char[] text=szTextsb.toString().toCharArray();
            for(int i=0; i<text.length; i++){  //pack the characters
                final int intOfChar=(int) text[i];
                temp[idx++]=(byte) (intOfChar);
                temp[idx++]=(byte) (intOfChar>>8);
            }
            for(int i=0; i<6; i++){
                temp[idx++]=0;
            }

            try{
                client.send(temp, handler);
                result=true;
            } catch(IOException ioe){
                THE_LOGGER.severe("io exception when writing *******************");
            }
            return result;
        }

        String getStringData() {
            return szTextsb.toString();
        }

        @Override
        public String toString() {
            return szTextsb.toString();
        }
    }
}
