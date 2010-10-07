package net.dbcrd.radiopackage.omnirigserial;

import java.awt.Point;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dbcrd.radiopackage.RadioControl;
import net.dbcrd.radiopackage.omnirigserial.OmniRigCmdData.ReplyType;

/**
 * Reads OmniRig .ini files and generates a RadioControl object.
 * @author dbcurtis
 *
 * see http://www.dxatlas.com/OmniRig/IniStru.txt
 */
 class OmniRigIniReader {

    /**
     * Patterns for decoding Omni-Rig ini files
     */
    //  private static final Preferences RADIO_SEL_PREFS=Preferences.userRoot().node(OmniRigIniReader.class.getName());
    private static final Logger THE_LOGGER=Logger.getLogger(OmniRigIniReader.class.getName());
    static final Pattern PRENS=Pattern.compile("([(].*?[)])+");
    static final Pattern COMMENT=Pattern.compile(";(?!\\)).*$", Pattern.MULTILINE);
    static final Pattern CMD_PAT=Pattern.compile("Command\\s*=\\s*(.*)", Pattern.CASE_INSENSITIVE);
    static final Pattern INIT_PAT=Pattern.compile("\\[INIT(\\d+)?\\]", Pattern.CASE_INSENSITIVE);
    static final Pattern STATUS_PAT=Pattern.compile("\\[STATUS(\\d+)?\\]", Pattern.CASE_INSENSITIVE);
    static final Pattern FREQ_PAT=Pattern.compile("\\[pmFreq\\]", Pattern.CASE_INSENSITIVE); //  operating frequency
    static final Pattern FREQA_PAT=Pattern.compile("\\[pmFreqA\\]", Pattern.CASE_INSENSITIVE);// VFO A frequency
    static final Pattern FREQB_PAT=Pattern.compile("\\[pmFreqB\\]", Pattern.CASE_INSENSITIVE);// VFO B frequency
    static final Pattern PITCH_PAT=Pattern.compile("\\[pmPitch\\]", Pattern.CASE_INSENSITIVE);// CW pitch frequency
    static final Pattern RIT_FREQ_PAT=Pattern.compile("\\[pmRitOffset\\]", Pattern.CASE_INSENSITIVE);// RIT offset frequency
    static final Pattern RIT_0_PAT=Pattern.compile("\\[pmRit0\\]", Pattern.CASE_INSENSITIVE);// Clear RIT - this is a write-only parameter
    static final Pattern RATA_PAT=Pattern.compile("\\[pmVfoAA\\]", Pattern.CASE_INSENSITIVE);// receive and transmit on VFO A
    static final Pattern RATB_PAT=Pattern.compile("\\[pmVfoAB\\]", Pattern.CASE_INSENSITIVE);// receive on VFO A, transmit on VFO B
    static final Pattern RBTA_PAT=Pattern.compile("\\[pmVfoBA\\]", Pattern.CASE_INSENSITIVE);// receive on VFO B, transmit on VFO A
    static final Pattern RBTB_PAT=Pattern.compile("\\[pmVfoBB\\]", Pattern.CASE_INSENSITIVE);// receive and transmit on VFO B
    static final Pattern RA_PAT=Pattern.compile("\\[pmVfoA\\]", Pattern.CASE_INSENSITIVE);// receive on VFO A, transmit VFO unknown
    static final Pattern RB_PAT=Pattern.compile("\\[pmVfoB\\]", Pattern.CASE_INSENSITIVE);// receive on VFO B, transmit VFO unknown
    static final Pattern CR2T_PAT=Pattern.compile("\\[pmVfoEqual\\]", Pattern.CASE_INSENSITIVE);// copy the frequency of the receive VFO to the transmit VFO
    static final Pattern SRT_PAT=Pattern.compile("\\[pmVfoSwap\\]", Pattern.CASE_INSENSITIVE);// swap frequencies of the receive and transmit VFO's
    static final Pattern SPLIT_YES_PAT=Pattern.compile("\\[pmSplitOn\\]", Pattern.CASE_INSENSITIVE);// enable split operation
    static final Pattern SPLIT_NO_PAT=Pattern.compile("\\[pmSplitOff\\]", Pattern.CASE_INSENSITIVE);// disable split operation
    static final Pattern RIT_ON_PAT=Pattern.compile("\\[pmRitOn\\]", Pattern.CASE_INSENSITIVE);// enable RIT
    static final Pattern RIT_OFF_PAT=Pattern.compile("\\[pmRitOff\\]", Pattern.CASE_INSENSITIVE);// disable RIT
    static final Pattern XIT_ON_PAT=Pattern.compile("\\[pmXitOn\\]", Pattern.CASE_INSENSITIVE);// enable XIT
    static final Pattern XIT_OFF_PAT=Pattern.compile("\\[pmXitOff\\]", Pattern.CASE_INSENSITIVE);// disable XIT
    static final Pattern REC_EN_PAT=Pattern.compile("\\[pmRx\\]", Pattern.CASE_INSENSITIVE);// enable receive mode
    static final Pattern TRN_EN_PAT=Pattern.compile("\\[pmTx\\]", Pattern.CASE_INSENSITIVE);// enable transmit mode
    static final Pattern CW_USB_PAT=Pattern.compile("\\[pmCW_U\\]", Pattern.CASE_INSENSITIVE);// CW mode, upper sideband
    static final Pattern CW_LSB_PAT=Pattern.compile("\\[pmCW_L\\]", Pattern.CASE_INSENSITIVE);// CW mode, lower sideband
    static final Pattern USB_PAT=Pattern.compile("\\[pmSSB_U\\]", Pattern.CASE_INSENSITIVE);// USB mode
    static final Pattern LSB_PAT=Pattern.compile("\\[pmSSB_L\\]", Pattern.CASE_INSENSITIVE);// LSB mode
    static final Pattern DIG_USB_PAT=Pattern.compile("\\[pmDIG_U\\]", Pattern.CASE_INSENSITIVE);// Digital mode ("\\[RTTY, FSK, etc.), upper sideband
    static final Pattern DIG_LSB_PAT=Pattern.compile("\\[pmDIG_L\\]", Pattern.CASE_INSENSITIVE);// Digital mode, lower sideband
    static final Pattern AM_PAT=Pattern.compile("\\[pmAM\\]", Pattern.CASE_INSENSITIVE);// AM mode
    static final Pattern FM_PAT=Pattern.compile("\\[pmFM\\]", Pattern.CASE_INSENSITIVE);// FM mode
    static final Pattern VAL_PAT=Pattern.compile("(Validate\\s*=\\s*(.*?\\|)?(.*))", Pattern.CASE_INSENSITIVE);//validate
    static final Pattern REPL_LEN_PAT=Pattern.compile("((Reply)(length|end))\\s*=\\s*(.*)", Pattern.CASE_INSENSITIVE);
    static final Pattern VALUE_PAT=Pattern.compile("(value\\s*=\\s*)(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)(\\|(.*))?$",
                                                   Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    static final Pattern VALUEN_PAT=Pattern.compile("(value(\\d+)\\s*=\\s*)(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*)",
                                                    Pattern.CASE_INSENSITIVE);
    static final Pattern FLAGN_PAT=Pattern.compile(
            "(flag(\\d+)\\s*=\\s*)((([0-9a-f.]+)\\|)?([()0-9a-f.]+)(\\|([a-z_]+)))", Pattern.CASE_INSENSITIVE);
    static final Pattern FLAG_PM_PAT=Pattern.compile(
            "(?<!\\[(\\s){0,5})pm((ssb|dig|cw)_(u|l)|am|fm|rx|tx|(split|rit|xit)o(n|ff)|vfo(a|b))",
            Pattern.CASE_INSENSITIVE);
    static final Pattern TEXT_PAT=Pattern.compile("\\([a-z.0-9;:,-]+\\)", Pattern.CASE_INSENSITIVE);
    static final Pattern HEX_PAT=Pattern.compile("(?<==\\s{0,5})[0-9a-f.]{2,}", Pattern.CASE_INSENSITIVE);
    static final Pattern FMT_PAT=Pattern.compile("vf(text|binL|binB|bcdLU|bcdLS|bcdBS|yaesu)", Pattern.CASE_INSENSITIVE);
    static final Pattern PM_COD_PAT=Pattern.compile("(\\[(pm[a-z_]+)\\])|(\\[STATUS(\\d+)?\\])|(\\[INIT(\\d+)?\\])|(\\[--END--\\])",
                                                    Pattern.CASE_INSENSITIVE);
    private NumericValues defaultFmt=null;

    /**
     * An Enum for associating commands and patterns (if not true, the command is not enabled)
     */
    private enum PattSel {

        CMD(CMD_PAT, true),
        RPLY_LEN(REPL_LEN_PAT, true),
        TEXT(TEXT_PAT, true),
        HEX(HEX_PAT, true),
        FMT(FMT_PAT, true),
        INIT(INIT_PAT, true),
        STATUS(STATUS_PAT, true),
        FREQ(FREQ_PAT, true),
        FREQA(FREQA_PAT, false),
        FREQB(FREQB_PAT, false),
        PITCH(PITCH_PAT, false),
        RIT_FREQ(RIT_FREQ_PAT, false),
        RIT_0(RIT_0_PAT, false),
        RATA(RATA_PAT, true),
        RATB(RATB_PAT, false),
        RBTA(RBTA_PAT, false),
        RBTB(RBTB_PAT, false),
        FLAG_PM(FLAG_PM_PAT, false),
        RA(RA_PAT, false),
        RB(RB_PAT, false),
        CR2T(CR2T_PAT, false),
        SRT(SRT_PAT, false),
        SPLIT_YES(SPLIT_YES_PAT, false),
        SPLIT_NO(SPLIT_NO_PAT, false),
        RIT_ON(RIT_ON_PAT, false),
        RIT_OFF(RIT_OFF_PAT, false),
        XIT_ON(XIT_ON_PAT, false),
        XIT_OFF(XIT_OFF_PAT, false),
        REC_EN(REC_EN_PAT, true),
        TRN_EN(TRN_EN_PAT, true),
        CW_USB(CW_USB_PAT, false),
        CW_LSB(CW_LSB_PAT, false),
        USB(USB_PAT, true),
        LSB(LSB_PAT, true),
        DIG_USB(DIG_USB_PAT, false),
        DIG_LSB(DIG_LSB_PAT, false),
        AM(AM_PAT, true),
        FM(FM_PAT, true),
        VALUE(VALUE_PAT, true),
        VALUEN(VALUEN_PAT, true),
        VDATE(VAL_PAT, true),
        PMCODE(PM_COD_PAT, true),
        FLAGN(FLAGN_PAT, true);
        private final Pattern pat;
        private final boolean usable;

        private PattSel(final Pattern pat, final boolean usable) {
            this.pat=pat;
            this.usable=usable;
        }

        Pattern getPat() {
            return this.pat;
        }

        boolean isUsable() {
            return usable;
        }
    }
//    /**
//     *
//     */
//    static enum NumericValues {
//        /*
//         * These examples show the values of 123 and -123 converted to
//         * a sequence of 4 bytes according to different formats:
//         *
//         *
//         *  Value:       123          -123
//         *  ----------------------------------
//         *  vfText   30.31.32.33   2D.31.32.33
//         *  vfBinL   7B.00.00.00   85.FF.FF.FF
//         *  vfBinB   00.00.00.7B   FF.FF.FF.85
//         *  vfBcdLU  23.01.00.00   n/a
//         *  vfBcdLS  23.01.00.00   23.01.00.FF
//         *  vfBcdBU  00.00.01.23   n/a
//         *  vfBcdBS  00.00.01.23   FF.00.01.23
//         *  vfYaesu  00.00.00.7B   80.00.00.7B
//         */
//
//        vfText, //asc codes of digits
//        vfBinL, //integer, little endian
//        vfBinB, //integer, big endian
//        vfBcdLU, //BCD, little endian, unsigned
//        vfBcdLS, //BCD, little endian, signed; the sign is in the MSB byte (0x00 or 0xFF)
//        vfBcdBU, //big endian, unsigned
//        vfBcdBS,//big endian, signed
//        vfYaesu;//special format used by Yaesu
//    }
    /**
     *
     */
    private Set<OmniRigCmdData> cmds;
    /**
     *
     */
    private final PattSel[] VALUE_CMD={PattSel.VALUE, PattSel.RPLY_LEN, PattSel.VDATE};
    /**
     *
     */
    // private static final Set<PattSel> VALUE_CMD_SET=new HashSet<PattSel>();
    private static final Set<PattSel> VALUE_CMD_SET=EnumSet.noneOf(PattSel.class);

    {
        VALUE_CMD_SET.addAll(Arrays.asList(VALUE_CMD));
    }
    /**
     *
     */
    private static final Set<PattSel> CMD_SET=EnumSet.noneOf(PattSel.class);//new HashSet<PattSel>();

    {
        CMD_SET.addAll(VALUE_CMD_SET);
        CMD_SET.remove(PattSel.VALUE);
    }
    /**
     *
     */
    private final NavigableMap<PattSel, NavigableSet<Integer>> pattern2LocS=
            new ConcurrentSkipListMap<PattSel, NavigableSet<Integer>>();
    /**
     *
     */
    private final NavigableMap<Integer, EnumSet<PattSel>> loc2Pat=
            new ConcurrentSkipListMap<Integer, EnumSet<PattSel>>();
    /**
     *
     */
    private final Map<PattSel, Matcher> pat2Mx=new EnumMap<PattSel, Matcher>(PattSel.class);//new HashMap<PattSel, Matcher>();
    /**
     *
     */
    private final String radiofile; //radio file name sans .ini
    /**
     *
     */
    Set<String> fmts=new HashSet<String>();

    /**
     * Processes the radio.ini data, removes all comments,
     * Inserts a "[--END--]" at the end of the inSB.
     * @param filename a String containing the filename ending in .ini that was the source of the
     * text in inSB
     * @param inSB a StringBuilder containing data read in from the specified filename.
     */
     OmniRigIniReader(final String filename, final StringBuilder inSB) {
        super();
        this.radiofile=filename.replace(".ini", "");
        {
            final Matcher prenm=PRENS.matcher(inSB);
            NavigableMap<Integer, Integer> notcomm=new ConcurrentSkipListMap<Integer, Integer>();
            while (prenm.find()) {
                notcomm.put(prenm.start(), prenm.end());
            }

            Deque<Point> st=new LinkedList<Point>();
            final Matcher cmtMx=COMMENT.matcher(inSB);
            int loc=0;

            while (cmtMx.find()) {
                Integer sss=cmtMx.start();
                Map.Entry<Integer, Integer> val=notcomm.floorEntry(sss);
                if (null != val && val.getKey() <= sss && sss <= val.getValue()) {
                    loc=val.getValue();
                    continue;
                }
                st.push(new Point(cmtMx.start(), cmtMx.end()));
            }
            while (!st.isEmpty()) {
                Point pt=st.pop();
                inSB.delete(pt.x, pt.y);
            }

            inSB.append("[--END--]");
        }
        for (PattSel cmdPat : PattSel.values()) {  // setup the matchers.
            pat2Mx.put(cmdPat, cmdPat.getPat().matcher(inSB));
        }
        NavigableMap<Integer, Set<String>> loc2str=new ConcurrentSkipListMap<Integer, Set<String>>();
        //locate all matcher hits and populate the maps
        for (Map.Entry<PattSel, Matcher> ent : pat2Mx.entrySet()) {
            final Set<Integer> tempInts=new HashSet<Integer>();
            final Matcher myMx=ent.getValue();

            int loc=0;
            while (myMx.find(loc)) {
                tempInts.add(myMx.start());
                if (!loc2Pat.containsKey(myMx.start())) {
                    loc2Pat.put(myMx.start(), EnumSet.noneOf(PattSel.class));//new ConcurrentSkipListSet<PattSel>());

                }
                loc2Pat.get(myMx.start()).add(ent.getKey());
                if (!loc2str.containsKey(myMx.start())) {
                    loc2str.put(myMx.start(), new HashSet<String>());
                }
                loc2str.get(myMx.start()).add(myMx.group());
                loc=myMx.end();
            }
            pattern2LocS.put(ent.getKey(), new ConcurrentSkipListSet<Integer>(tempInts));
        }

        checkForAndSetDefaultFormat(inSB);


        cmds=extractCommands(inSB);

    }

    /**
     *
     * @param inSB a StringBuilder
     */
    private void checkForAndSetDefaultFormat(
            final StringBuilder inSB) {

        Matcher myMx=pat2Mx.get(PattSel.FMT);
        Set<Integer> locs=pattern2LocS.get(PattSel.FMT);
        fmts.clear();
        fmts=new HashSet<String>();
        myMx.reset(inSB);
        for (int myloc : locs) {
            myMx.find(myloc);
            fmts.add(myMx.group());
        }
        if (fmts.size() == 1) {
            defaultFmt=NumericValues.valueOf(fmts.iterator().next().trim());

        } else {
            THE_LOGGER.warning("multiple numeric values used");
        }
    }

    /**
     * for testing purposes
     * @return an Iterator for OmniRigCmdData
     */
    Iterator<OmniRigCmdData> getCmds() {
        return cmds.iterator();
    }

    /**
     * Get the RadioControl corrosponding to the ini file.
     * @param client2serverQ A BlockingQueue of PktInfo to receive work for the radio
     * @return a RadioControl as per the OmniRig.ini file.
     */
    public RadioControl getRadio(final BlockingQueue<PktInfo> client2serverQ) {
        RadioControl radio=new OmniRigStruct(radiofile, cmds, client2serverQ);
        return radio;
    }

    /**
     * Each command has a response.  This routine finds all command response blocks
     * @return a Set of OmniRigCmdData objects
     */
    private Set<OmniRigCmdData> extractCommands(final StringBuilder inSB) {
        // inSB is ONLY used for debugging here.

        final Set<OmniRigCmdData> result=new HashSet<OmniRigCmdData>();
        if (pattern2LocS.get(PattSel.CMD).size() != pattern2LocS.get(PattSel.RPLY_LEN).size()) {
//            if (false){
//
//                StringBuilder mysb = new StringBuilder();
//                NavigableSet<Integer> cmdLocs = pattern2LocS.get(PattSel.CMD);
//                NavigableSet<Integer> rplLocs = pattern2LocS.get(PattSel.RPLY_LEN);
//                Iterator<Integer> cmdIt = cmdLocs.iterator();
//                Iterator<Integer> rplIt = rplLocs.iterator();
//                int cnt = (cmdLocs.size()<rplLocs.size())?cmdLocs.size():rplLocs.size();
//                int lastcmd = 0;
//                int lastlst=0;
//                int start=0;
//                int end=0;
//                try{
//                for(int i=0;i<cnt;i++){
//                    start = (cmdIt.hasNext())?cmdIt.next():lastcmd;
//                    lastcmd=start;
//                      end = (rplIt.hasNext())?rplIt.next():lastlst;
//                     end = (end>inSB.length()-1)?inSB.length()-1:end;
//                    lastlst=end;
//
//                    mysb.append("------------").append(inSB.substring(start, end)).append("----");
//                }
//                }catch(Throwable th ){
//                    int ii=0;
//                    ii++;
//                }
//            }

            THE_LOGGER.severe("Unbalanced CMD and Reply *****************");
            throw (new IllegalArgumentException());

        }




        final Iterator<Integer> cmdLocIt=pattern2LocS.get(PattSel.CMD).iterator();
        final Iterator<Integer> rplyLocIt=pattern2LocS.get(PattSel.RPLY_LEN).iterator();
        //  final NavigableSet<Integer> cmdblkboundry=pattern2LocS.get(PattSel.PMCODE);
        final Queue<NavigableMap<Integer, EnumSet<PattSel>>> cmdblocks=
                new LinkedList<NavigableMap<Integer, EnumSet<PattSel>>>();

        while (cmdLocIt.hasNext()) {
            //  final int cmdloc=cmdLocIt.next();
            //  final int rplloc=rplyLocIt.next();
            final Map.Entry<Integer, EnumSet<PattSel>> cmdEnt=loc2Pat.floorEntry(cmdLocIt.next() - 1);
            final Map.Entry<Integer, EnumSet<PattSel>> repEnt=loc2Pat.ceilingEntry(rplyLocIt.next() + 1);
            //Integer nextblk=cmdblkboundry.ceiling(repEnt.getKey());
            Integer nextblk=pattern2LocS.get(PattSel.PMCODE).ceiling(repEnt.getKey());
            boolean last=false;
            if (null == nextblk) {
                nextblk=repEnt.getKey();
                last=true;
            }

            boolean copy=true;
            if (cmdEnt.getValue().size() == 1) {
                final PattSel loc=cmdEnt.getValue().iterator().next();
                copy=loc.isUsable();
            } else {
                final EnumSet<PattSel> locs=cmdEnt.getValue();
                if (locs.size() > 2) {
                    THE_LOGGER.severe("*****************logic error1");
                    assert false;
                }
                for (PattSel loc : locs) {
                    if (PattSel.PMCODE.equals(loc)) {
                        continue;
                    }
                    copy=loc.isUsable();
                }
            }
            if (copy) {
                cmdblocks.add(loc2Pat.subMap(cmdEnt.getKey(), true, nextblk, last));
            }
        }

        result.addAll(processCommandBlocks(cmdblocks));
        return result;
    }

    /**
     *
     * @param orCmd an OmniRigCmdData or child thereof that is filled out.
     * @param ent an Entry<Integer, NaviableSed<PattSel>>
     * @param key an Integer 
     */
    private void processCMDArgs(final OmniRigCmdData orCmd,
                                final Entry<Integer, EnumSet<PattSel>> ent,
                                final Integer key) {

        final Matcher myMx=pat2Mx.get(ent.getValue().iterator().next()).reset();
        if (!myMx.find(key)) {
            THE_LOGGER.severe("Assert false");
            assert false;
        }
        if (ent.getValue().contains(PattSel.HEX) || ent.getValue().contains(PattSel.TEXT)) {
            orCmd.setCommand(myMx.group());//String temp=myMx.group();

        }
    }

    /**
     * Generate OmniRigCmdData from the commandblocks.
     * @param cmdblocks a queue of strings that have commands
     * @return a Set of OmniRigCmdData resulting from the processed commandBlocks.
     */
    private Set<OmniRigCmdData> processCommandBlocks(
            final Queue<NavigableMap<Integer, EnumSet<PattSel>>> cmdblocks) {

        final Set<OmniRigCmdData> result=new HashSet<OmniRigCmdData>();
        try {
            for (NavigableMap<Integer, EnumSet<PattSel>> cmdblk : cmdblocks) { //for each command block
                label:
                for (Map.Entry<Integer, EnumSet<PattSel>> cmdEnt : cmdblk.entrySet()) {

                    PattSel cmd=null;
                    assert cmdEnt.getValue().size() < 3;
                    for (PattSel cmda : cmdEnt.getValue()) {
                        if (PattSel.PMCODE.equals(cmda)) {
                            continue;
                        }
                        cmd=cmda;
                    }

                    if (null != cmd) {
                        OmniRigCmdData orCmd=null;
                        switch (cmd) {
                            case FREQ:
                                try {
                                    orCmd=new OmniRigSetFreqCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processFreq(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case REC_EN:
                                try {
                                    orCmd=new OmniRigSetRecEnCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processRecEN(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case TRN_EN:
                                try {
                                    orCmd=new OmniRigSetTrnEnCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processTranEN(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case USB:
                                try {
                                    orCmd=new OmniRigSetUSBCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processUSB(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case LSB:
                                try {
                                    orCmd=new OmniRigSetLSBCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processLSB(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case AM:
                                try {
                                    orCmd=new OmniRigSetAMCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processAM(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case FM:
                                try {
                                    orCmd=new OmniRigSetFMCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processFM(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case STATUS:
                                try {
                                    orCmd=new OmniRigGetStatusCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);
                                    processStatus(orCmd, cmdblk);
                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            case INIT:
                                try {
                                    orCmd=new OmniRigInitCmd(cmd.toString(), this.radiofile);
                                    orCmd.setDefaultBV(defaultFmt);

                                    processInit(orCmd, cmdblk);

                                    result.add(orCmd);
                                } catch (Throwable th) {
                                    int i=0;
                                    i++;
                                }
                                break label;
                            default:
                                break;
                        }
                    }
                }
            }

        } catch (Throwable th) {
            int i=0;
            i++;
        }
        return result;
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processStatus(
            final OmniRigCmdData orCmd,
            final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.STATUS)) {
                // StatusCmdInfo tempinfo = new StatusCmdInfo(cmdblk);
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd an OmniRigCmdData
     * @param cmdblk a CaanigableMap- integer to havigableSet of PattSel
     */
    private void processInit(final OmniRigCmdData orCmd, final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {
        try {
            Integer cmdBlkVal=0;
            Map.Entry<Integer, EnumSet<PattSel>> ent=null;
            while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
                if (ent.getValue().contains(PattSel.INIT)) {
                    try {
                        cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                    } catch (Throwable th) {
                        int i=0;
                        i++;
                    }
                    if (0 == cmdBlkVal) {
                        THE_LOGGER.severe("Assert false");
                        assert false;
                    }
                    continue;
                }

                if (ent.getValue().contains(PattSel.CMD)) {
                    try {
                        cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                        continue;
                    } catch (Throwable th) {
                        int i=0;
                        i++;
                    }
                }

            }
        } catch (Throwable th) {
            int i=0;
            i++;
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processFreq(final OmniRigCmdData orCmd, final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.FREQ)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processRecEN(final OmniRigCmdData orCmd, final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.REC_EN)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processFM(final OmniRigCmdData orCmd,
                           final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.FM)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processAM(final OmniRigCmdData orCmd,
                           final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.AM)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processLSB(final OmniRigCmdData orCmd,
                            final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.LSB)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd 
     * @param cmdblk
     */
    private void processUSB(final OmniRigCmdData orCmd,
                            final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.USB)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param orCmd
     * @param cmdblk
     */
    private void processTranEN(final OmniRigCmdData orCmd,
                               final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        Integer cmdBlkVal=0;
        Map.Entry<Integer, EnumSet<PattSel>> ent=null;
        while (null != (ent=cmdblk.ceilingEntry(cmdBlkVal + 1))) {
            if (ent.getValue().contains(PattSel.TRN_EN)) {
                cmdBlkVal=generateCmdObjects(ent.getKey(), cmdblk);
                if (0 == cmdBlkVal) {
                    THE_LOGGER.severe("Assert false");
                    assert false;
                }
                continue;
            }

            if (ent.getValue().contains(PattSel.CMD)) {
                cmdBlkVal=processCMD(orCmd, ent.getKey(), cmdblk);
                continue;
            }
        }
    }

    /**
     *
     * @param key
     * @param cmdblk
     * @return
     */
    private Integer generateCmdObjects(
            final Integer key,
            final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        return cmdblk.ceilingKey(key);
    }

    /**
     *
     * @param orCmd
     * @param key
     * @param cmdblk
     * @return
     */
    private Integer processCMD(
            final OmniRigCmdData orCmd,
            final Integer key,
            final NavigableMap<Integer, EnumSet<PattSel>> cmdblk) {

        final NavigableMap<Integer, EnumSet<PattSel>> mycmdblk=cmdblk.tailMap(key, true);
        final Iterator<Map.Entry<Integer, EnumSet<PattSel>>> cmdblkIt=mycmdblk.entrySet().iterator();
        while (cmdblkIt.hasNext()) {
            final Map.Entry<Integer, EnumSet<PattSel>> ent=cmdblkIt.next();

            if (defineCmd(ent, orCmd, cmdblkIt)) {
                continue;
            }

            if (defineValue(ent, orCmd)) {
                continue;
            }

            if (defineValues(ent, orCmd, cmdblkIt)) {
                continue;
            }

            if (defineReplyLen(ent, orCmd)) {
                continue;
            }

            if (defineValidate(ent, orCmd, cmdblkIt)) {
                continue;
            }

            if (defineFlag(ent, orCmd, cmdblkIt)) {
                continue;
            }
        }

        try {
            orCmd.makeTemplate();
        } catch (Throwable th) {
            int i=0;
            i++;
        }
        return 10000000;  // force end of block
    }

    /**
     *
     * @param ent a Map.Entry<Integer, NavigableSet<PattSel>> where Integer is the position in sbin and the set is the patterns
     * @param orCmd OmniRigCmdData
     * @return a boolean true if ent contains a CMD
     */
    private boolean defineCmd(
            final Map.Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd,
            final Iterator<Map.Entry<Integer, EnumSet<PattSel>>> it) {

        if (ent.getValue().contains(PattSel.CMD)) {
            final Map.Entry<Integer, EnumSet<PattSel>> myent=it.next();
            if (myent.getValue().contains(PattSel.HEX) || myent.getValue().contains(PattSel.TEXT)) {
                processCMDArgs(orCmd, myent, ent.getKey());
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param ent a Map.Entry of Integer, EnumSet of PattSel
     * @param orCmd a OmniRigCmdData
     * @return a boolean true if 
     */
    private boolean defineReplyLen(
            final Map.Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd) {

        if (ent.getValue().contains(PattSel.RPLY_LEN)) {
            final Matcher myRPLMx=pat2Mx.get(ent.getValue().iterator().next()).reset();
            if (!myRPLMx.find(ent.getKey())) {
                THE_LOGGER.severe("Assert false");
                assert false; //TODO fix
            }
            //    String temp = myRPLMx.group(4);
            if ("end".equalsIgnoreCase(myRPLMx.group(3))) {
                orCmd.repLenSpecified=false;
                orCmd.reply=myRPLMx.group(4);
                orCmd.wait4Reply=!orCmd.reply.trim().isEmpty();
                if (orCmd.reply.trim().startsWith("(")) {
                    orCmd.replyType=ReplyType.TEXT;
                } else {
                    orCmd.replyType=ReplyType.HEX;
                }

            }
            if ("length".equalsIgnoreCase(myRPLMx.group(3))) {
                orCmd.repLenSpecified=true;
                orCmd.reply=myRPLMx.group(4);
                orCmd.replLen=Integer.parseInt(orCmd.reply);
                orCmd.wait4Reply=0 != orCmd.replLen;
                orCmd.replyType=ReplyType.LENGTH;
            }
            return true;
        }
        return false;
    }

    /**
     * 
     * @param ent
     * @param orCmd
     * @param cmdblkIt
     * @return
     */
    private boolean defineValidate(
            final Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd,
            final Iterator<Map.Entry<Integer, EnumSet<PattSel>>> cmdblkIt) {

        if (ent.getValue().contains(PattSel.VDATE)) {
            final Matcher myRPLMx=pat2Mx.get(ent.getValue().iterator().next()).reset();

            if (!myRPLMx.find(ent.getKey())) {
                assert false;//TODO fix
            }

            orCmd.setValidate(new String[]{myRPLMx.group(2), myRPLMx.group(3)});
            return true;
        }
        return false;
    }

    /**
     *
     * @param ent
     * @param orCmd
     * @return
     */
    private boolean defineValue(
            final Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd) {

        if (ent.getValue().contains(PattSel.VALUE)) {
            final Matcher myValMx=pat2Mx.get(ent.getValue().iterator().next()).reset();
            if (!myValMx.find(ent.getKey())) {
                THE_LOGGER.severe("Assert false");
                assert false; //TODO fix
            }

            Queue<String> fifo=new LinkedList<String>();
            for (int j=2; j < myValMx.groupCount(); j++) {
                if (null == myValMx.group(j)) {
                    break;
                }
                fifo.add(myValMx.group(j).replace('|', ' ').trim());
            }
            final String[] jj=fifo.toArray(new String[fifo.size()]);
            orCmd.setValues(jj);
            return true;
        }
        return false;
    }

    /**
     *
     * @param ent
     * @param orCmd
     * @param cmdblkIt
     * @return
     */
    private boolean defineValues(
            final Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd,
            final Iterator<Map.Entry<Integer, EnumSet<PattSel>>> cmdblkIt) {

        if (ent.getValue().contains(PattSel.VALUEN)) {
            final Matcher myValMx=pat2Mx.get(ent.getValue().iterator().next()).reset();
            if (!myValMx.find(ent.getKey())) {
                THE_LOGGER.severe("Assert false");
                assert false;//TODO fix
            }

            orCmd.setValues(
                    new String[]{
                        myValMx.group(2),
                        myValMx.group(3),
                        myValMx.group(4),
                        myValMx.group(5),
                        myValMx.group(6),
                        myValMx.group(7),
                        myValMx.group(8)});

            while (cmdblkIt.hasNext()) {
                if (cmdblkIt.next().getValue().contains(PattSel.FLAG_PM)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param ent
     * @param orCmd
     * @param cmdblkIt
     * @return
     */
    private boolean defineFlag(
            final Entry<Integer, EnumSet<PattSel>> ent,
            final OmniRigCmdData orCmd,
            final Iterator<Map.Entry<Integer, EnumSet<PattSel>>> cmdblkIt) {

        if (ent.getValue().contains(PattSel.FLAGN)) {
            final Matcher myRPLMx=pat2Mx.get(ent.getValue().iterator().next()).reset();
            if (!myRPLMx.find(ent.getKey())) {
                THE_LOGGER.severe("Assert false");
            }
            orCmd.setFlags(new String[]{myRPLMx.group(2), myRPLMx.group(5), myRPLMx.group(6), myRPLMx.group(8)});
            Map.Entry<Integer, EnumSet<PattSel>> tempEnt=ent;
            while (tempEnt != null && !tempEnt.getValue().contains(PattSel.FLAG_PM)) { // skip to the next flag
                tempEnt=(cmdblkIt.hasNext()) ? cmdblkIt.next() : null;
            }
            return true;
        }
        return false;
    }
}
