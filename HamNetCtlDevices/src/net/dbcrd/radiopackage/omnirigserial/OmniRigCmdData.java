package net.dbcrd.radiopackage.omnirigserial;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dbcurtis
 */
@ClassPreamble(date="4/6/2010")
class OmniRigCmdData {

    private static final String FLG_PAT_RGX="";
    private static final Pattern FLG_PAT=Pattern.compile(FLG_PAT_RGX);
    private static final Logger THE_LOGGER= Logger.getLogger(OmniRigCmdData.class.getName());

    private static String makeHexMask(String mask, String bits) {
        //HEX
        /*
         * 7.1. If <bits> is in the hexadecimal format and <mask> is omitted,
        then <bits> is copied to <mask> and all non-zero bytes are replaced in <mask>
        with 0xFF.
        Example:
        Validate=FEFE0064FBFD  is equivalent to  Validate=FFFF00FFFFFF|FEFE0064FBFD
         */

        if (mask.isEmpty()) {
            final StringBuilder newMask=new StringBuilder(bits);
            final Matcher mx=Pattern.compile("[0-9a-f]{2,2}", Pattern.CASE_INSENSITIVE).matcher(newMask);
            while (mx.find()) {
                final String temps=mx.group(0);
                if ("00".equals(mx.group())) {
                    continue;
                }
                newMask.replace(mx.start(), mx.end(), "FF");
            }
            mask=newMask.toString();
        }
        return mask;
    }

    protected NumericValues defaultFmt=null;

     void setDefaultBV(final NumericValues  defaultFmt) {
        this.defaultFmt=defaultFmt;
    }
    enum ReplyType {

        UNSPECIFIED,
        LENGTH,
        TEXT,
        HEX
    }
    protected static String pmfmt="";
    /**
     *
     */
    protected final String pmcmd;
    /**
     *
     */
    protected final String radioID;
    /**
     *
     */
    protected String command;
    /**
     * from the replylength or replyend argument
     */
    protected String reply="";
    /**
     *
     */
    private String[] validate=new String[0];
    /**
     *
     */
    protected List<String[]> values=new LinkedList<String[]>();
    /** */
    private List<String[]> flagList=new LinkedList<String[]>();
    /**
     *
     */
    protected boolean repLenSpecified=false;
    protected int replLen=0;

    boolean isRepLenSpecified() {
        return repLenSpecified;
    }

    boolean isRepEndSpecified() {
        return !repLenSpecified;
    }
    protected boolean wait4Reply=false;

    boolean isReplyNeeded() {
        return wait4Reply;
    }
    ReplyType replyType=ReplyType.UNSPECIFIED;
    /**
     *
     */
    protected byte[] cmdData;
    private byte[] templateData;
    /**
     * a BinaryValue
     */
    protected BinaryValue binaryVal=null;
    private final Set<Flag> flags=new HashSet<Flag>();

    /**
     *
     * @param pmcmd
     * @param radioID
     * @param command
     * @param reply
     * @param validate
     * @param values
     */
    OmniRigCmdData(final String pmcmd,
                          final String radioID,
                          final String command,
                          final String reply,
                          final String[] validate,
                          final List<String[]> values) {
        super();
        this.pmcmd=pmcmd;
        this.radioID=radioID;
        this.command=command;
        this.reply=reply;
        this.values=values;
        this.validate=new String[validate.length];
        System.arraycopy(validate, 0, this.validate, 0, validate.length);
        normalizeMaskBits(validate);
    }

    String[] normalixeMaskBitsTest(final String[] maskbit) { //for testingonly
        String[] temp=new String[2];
        temp[0]=new StringBuilder(maskbit[0]).toString();
        temp[1]=new StringBuilder(maskbit[1]).toString();
        normalizeMaskBits(temp);
        return temp;

    }

    private static String convertText2Hex(final String text) {
        StringBuilder result=new StringBuilder();
        byte[] strbyt=new byte[0];
        try {
            String tmp=text.trim();
            strbyt=tmp.substring(1, tmp.length() - 1).getBytes("US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            THE_LOGGER.severe("illegal text 2 hex");
        }
        for (byte b : strbyt) {
            if (b == (byte) '.') {
                result.append("00");
            } else {
                result.append(Integer.toString(b, 16));
            }
        }
        return result.toString().toUpperCase();
    }

    private static void normalizeMaskBits(final String[] maskbit) {
        /*
         * 7. MASK FORMATS

        Bit extraction and validation rules are specified in the Validate and FlagN
        entries as <mask>|<bits>. The formats of <mask> and <bits> fields are described
        in Section 6. The <mask> field is optional; if omitted, it will be constructed
        from <bits> as follows.
         */
        String mask=maskbit[0];
        if (null == mask) {
            mask="";
        }
        mask=mask.trim();
        String bits=maskbit[1].trim();
        if (!mask.isEmpty() && !mask.startsWith("(")) {
            maskbit[0]=maskbit[0].replaceAll("\\.", "");
            maskbit[1]=maskbit[1].replaceAll("\\.", "");
            return;
        }
        final boolean txtfmt=bits.startsWith("(");
        if (txtfmt) {//TEXT
            /*
             * 7.2. If <bits> is in the text format and <mask> is omitted, then <bits>
            is copied to <mask>, all "." characters in <mask> are replaced with 0xFF,
            and all "." characters in <bits> are replaced with the ASC(0) character.

            Example:

            Validate=(PT..;)  is equivalent to   Validate=FFFF0000FF|505400003B
             */
            bits=convertText2Hex(bits);
            if (mask.isEmpty()) {
                mask=makeHexMask(mask, bits);
            } else {
                mask=convertText2Hex(mask);
            }

        } else { //HEX
            /*
             * 7.1. If <bits> is in the hexadecimal format and <mask> is omitted,
            then <bits> is copied to <mask> and all non-zero bytes are replaced in <mask>
            with 0xFF.

            Example:

            Validate=FEFE0064FBFD  is equivalent to  Validate=FFFF00FFFFFF|FEFE0064FBFD
             */
            bits=bits.replaceAll("\\.", "");
            if (mask.isEmpty()) {
                mask=makeHexMask(mask, bits);
            }
            mask=mask.replaceAll("\\.", "");
        }

        maskbit[0]=mask;
        maskbit[1]=bits;
    }

    /**
     *
     * @param pmcmd
     * @param radioID
     */
     OmniRigCmdData(final String pmcmd, final String radioID) {
        this.pmcmd=pmcmd;
        this.radioID=radioID;
    }

     OmniRigCmdData copy() {
        List<String[]> mycopyOfValues=new LinkedList<String[]>();
        for (String[] blk : values) {
            String[] jj=new String[blk.length];
            System.arraycopy(blk, 0, jj, 0, blk.length);
            mycopyOfValues.add(blk);
        }
        String[] jj=new String[this.validate.length];
        System.arraycopy(this.validate, 0, jj, 0, this.validate.length);
        OmniRigCmdData result=new OmniRigCmdData(pmcmd, radioID, command, reply, jj, mycopyOfValues);
        return result;
    }

    /**
     *
     * @return
     */
     String getCommand() {
        return command;
    }

    /**
     *
     * @param command
     */
     void setCommand(final String command) {
        this.command=command;
    }

    /**
     *
     * @return
     */
     List<String[]> getFlags() {
        return new LinkedList<String[]>(flagList);
    }

    /**
     *
     * @param flags
     */
     void setFlags(final String[] flags) {
        final String[] flagtmp=new String[flags.length];
        System.arraycopy(flags, 0, flagtmp, 0, flags.length);
        flagList.add(flagtmp);
    }

    /**
     *
     * @return
     */
     String getReply() {
        return reply;
    }

    /**
     *
     * @param reply
     */
    @Deprecated
    public void setReply(final String reply) {
        this.reply=reply;
    }

    /**
     *
     * @return
     */
     String[] getValidate() {
        final String[] result=new String[this.validate.length];
        System.arraycopy(validate, 0, result, 0, this.validate.length);
        return result;
    }

    /**
     *
     * @param validate
     */
     void setValidate(final String[] validate) {
        this.validate=new String[validate.length];
        System.arraycopy(validate, 0, this.validate, 0, validate.length);
        normalizeMaskBits(validate);
    }

    /**
     *
     * @return
     */
     List<String[]> getValues() {
        return this.values;
    }

    /**
     *
     * @param values
     */
     void setValues(final String[] values) {
        final String[] myblk=new String[values.length];
        System.arraycopy(values, 0, myblk, 0, values.length);
        this.values.add(myblk);
    }

    /**
     *
     * @param radio
     * @param client2serverQ
     * @param args
     * @return
     */
    Properties doCmd(
            final OmniRigStruct radio,
            final BlockingQueue<PktInfo> client2serverQ,
            Object... args) throws IOException {
        final Properties result=new Properties();
        result.setProperty("STATUS", "ILLEGAL");
        return result;
    }
    static private boolean flg234=true;

    /**
     *
     * @param radio
     * @param client2serverQ
     * @param data
     * @return
     */
     PktInfo sendCmd(
            final OmniRigStruct radio,
            final BlockingQueue<PktInfo> client2serverQ,
            final byte[]... data) throws IOException {

        final BlkQRspHandler<OmniRigCmdData> handler=new BlkQRspHandler<OmniRigCmdData>(this);
        PktInfo pkt;

        if (data.length == 0) {
            pkt=new PktInfo(radio.commID, new BlkServerCATCmd(cmdData), handler);
        } else {
            pkt=new PktInfo(radio.commID, new BlkServerCATCmd(data[0]), handler);
        }
        if (!client2serverQ.offer(pkt)) {
            THE_LOGGER.warning("Client2ServerQ overflow");
            return null;//TODO return approprate error packet.
        }
//        if (isReplyNeeded()){
//            if (flg234){
//                THE_LOGGER.severe("need to complete");
//                flg234=false;
//            }
        return handler.waitForResponse();

//        }else{
//            return null;
//        }


    }

    /**
     *
     * @param str
     * @return
     */
     static boolean isTextFormat(final String str) {
        return '(' == str.charAt(0);
    }

    /**
     *
     * @return
     */
     boolean initialize() {
        int size=0;
        cmdData=new byte[size];
        if (isTextFormat(command)) {
            //text
            size=command.length() - 2;
            command=command.substring(1, command.length() - 1);
//            try {
//                cmdData=cmdd.getBytes("US-ASCII");
//            } catch (UnsupportedEncodingException ex) {
//               THE_LOGGER.log(Level.SEVERE, null, ex);
//            }

            cmdData=new byte[size];

        } else {
            //hex
            String cmd=command.trim().replaceAll("\\.", "");
            size=cmd.length();

            assert (size & 1) == 0;
            size>>=1;
            cmdData=new byte[size];

        }

        for (String[] flgs : flagList) {
            flags.add(new Flag(flgs));
        }
        if (values.isEmpty()) {
            binaryVal=new BinaryValue(BinaryValue.getLastSeenNV());
        } else {
            binaryVal=new BinaryValue(values.iterator().next());
        }
        cmdData=binaryVal.getCompletedCmd(this);
        return true;
    }
    private static boolean djjje=true;

    /**
     * 
     * @param payload
     * @param validate
     * @return
     */
    private boolean isValid(byte[] payload, String[] validate) {
        if (validate.length == 0) {
            return true;
        }
        boolean text=validate[1].startsWith("(");
        if (text) {

            normalizeMaskBits(validate);
            final String mask=validate[0];
            final String bit=validate[1];
            final byte[] cpy=new byte[payload.length];
            System.arraycopy(payload, 0, cpy, 0, payload.length);
            assert mask.length() >> 1 == payload.length;
            int pos=0;
            for (int i=0; i < cpy.length; i++) {
                if ("00".equals(mask.substring(pos, pos + 2))) {
                    cpy[i]=0;
                }
                pos+=2;
            }
            pos=0;
            for (int i=0; i < payload.length; i++) {
                if (cpy[i] != Integer.parseInt(bit.substring(pos, pos + 2), 16)) {
                    return false;
                }
                pos+=2;
            }
            return true;
        } else {
            if (djjje) {
                djjje=false;
                THE_LOGGER.severe("Complete this code*********************");//TODO finish this
            }
            return true;
        }

    }

    /**
     * If multiply listed same name params, then if any of the same name params are true, then the status will show true.
     * @param replypkt a PktInfo containing the status info.
     * @return a Properties containing the listed name and it string value (TRUE or FALSE).
     */
     Properties decodeStatus(final PktInfo replypkt) {
        Properties result=new Properties();

        byte[] payload=(byte[]) replypkt.payload;
        result.setProperty("STATUS_STATE", "valid");

        if (repLenSpecified) {
            if (payload.length != replLen || !isValid(payload, validate)) {
                result.setProperty("STATUS_STATE", "invalid");
                return result;
            }

            return decodeStatusNonString(payload, result);
        } else {
            String endofreply=reply.substring(1, reply.length() - 1);
            StringBuilder sb=new StringBuilder();
            for (byte b : payload) {
                sb.append((char) b);
            }
            if (sb.length() - sb.indexOf(endofreply) != 1 || !isValid(payload, validate)) {
                result.setProperty("STATUS_STATE", "invalid");
                return result;
            }
            return decodeStatusString(payload, result);
        }
    }

    private Properties decodeStatusString(final byte[] payload, final Properties result) {
        if (!flags.isEmpty()) {
            // long longval=makeLong(payload);
            for (Flag flag : flags) {
//                long temp=longval & flag.mask;
//                if (temp != flag.bits) {
                //  THE_LOGGER.severe("TESTCODE******************************");
                if (flag.isNotSatisfied(payload)) {

                    if (result.containsKey(flag.param.toString())) {
                        if (result.getProperty(flag.param.toString()).equals("TRUE")) {
                            continue;
                        }

                    } else {
                        result.setProperty(flag.param.toString(), "FALSE");
                    }
                    result.setProperty(flag.param.toString(), "FALSE");
                } else {
                    result.setProperty(flag.param.toString(), "TRUE");
                }
            }
        }
        for (String[] val : this.values) {
            if (val.length == 5 || val.length == 6) {  //result of Value or Value that could have been ValueN
                int startPos=Integer.parseInt(val[0]);
                int myLength=Integer.parseInt(val[1]);
                String formats=val[2];
                NumericValues format=NumericValues.valueOf(formats);
                Double multiplyConst=Double.parseDouble(val[3]);
                int addConst=Integer.parseInt(val[4]);
                String param="";
                if (val.length == 6) {
                    param=val[5];
                }


                byte[] newPayload=new byte[myLength];
                System.arraycopy(payload, startPos, newPayload, 0, myLength);

                long num=NumericValues.convertData(newPayload, format);
                Double value=multiplyConst * num;
                value+=addConst;
                long lvalue=value.longValue();

                result.setProperty(val[5], Long.toString(lvalue));

            }
            if (val.length == 7) {
                int startPos=Integer.parseInt(val[1]);
                int myLength=Integer.parseInt(val[2]);
                String formats=val[3];
                NumericValues format=NumericValues.valueOf(formats);
                Double multiplyConst=Double.parseDouble(val[4]);
                int addConst=Integer.parseInt(val[5]);
                String param="";
                if (val.length == 6) {
                    param=val[6];
                }
                byte[] newPayload=new byte[myLength];
                System.arraycopy(payload, startPos, newPayload, 0, myLength);

                long num=NumericValues.convertData(newPayload, format);
                Double value=multiplyConst * num;
                value+=addConst;
                long lvalue=value.longValue();

                result.setProperty(val[6], Long.toString(lvalue));

            }
        }

        return result;
    }

    private Properties decodeStatusNonString(final byte[] payload, final Properties result) {
        if (!flags.isEmpty()) {

            for (Flag flag : flags) {

                if (flag.isNotSatisfied(payload)) {

                    if (result.containsKey(flag.param.toString())) {
                        if (result.getProperty(flag.param.toString()).equals("TRUE")) {
                            continue;
                        }

                    } else {
                        result.setProperty(flag.param.toString(), "FALSE");
                    }
                    result.setProperty(flag.param.toString(), "FALSE");
                } else {
                    result.setProperty(flag.param.toString(), "TRUE");
                }
            }
        }
        for (String[] val : this.values) {
            // first check to see what type of value it is

            if (val.length == 5 || val.length == 6) {  //result of Value or Value that could have been ValueN
                int startPos=Integer.parseInt(val[0]);
                int myLength=Integer.parseInt(val[1]);
                String formats=val[2];
                NumericValues format=NumericValues.valueOf(formats);
                Double multiplyConst=Double.parseDouble(val[3]);
                int addConst=Integer.parseInt(val[4]);
                String param="";
                if (val.length == 6) {
                    param=val[5];
                }


                byte[] newPayload=new byte[myLength];
                System.arraycopy(payload, startPos, newPayload, 0, myLength);

                long num=NumericValues.convertData(newPayload, format);
                Double value=multiplyConst * num;
                value+=addConst;
                long lvalue=value.longValue();

                result.setProperty(val[5], Long.toString(lvalue));

            }
            if (val.length == 7) {
                THE_LOGGER.severe("need to complete code*****************************");
//TODO need to find out what results from a real ValueN vs Value
            }
        }
        return result;
    }

    /**
     * Returns a long value from the payload.  The payload must not be greater than 8 bytes long
     * @param payload a byte[] containing the data to be used to construct the long
     * @return a long value of the payload
     */
    @Deprecated
    private long makeLong(final byte[] payload) {
        long result=0;
        if (payload.length > 8) {
            throw new IllegalArgumentException();
        }
        if (payload.length == 1) {
            result=((long) payload[0] & 0xFF);
            return result;
        }
        int shift=0;
        for (int i=payload.length - 1; i >= 0; i--) {
            long temp=((long) payload[i] & 0xFF);
            temp<<=shift;
            shift+=8;
            result|=temp;
        }

        return result;
    }

    void makeTemplate() {
        if (values.isEmpty()) {
            NumericValues thisFmt=BinaryValue.getLastSeenNV();
            if (null == thisFmt) {
                thisFmt=this.defaultFmt;
            }
            binaryVal=new BinaryValue(thisFmt);
        } else {
            binaryVal=new BinaryValue(values.iterator().next());
        }
        try{
        templateData=binaryVal.getCompletedCmd(this);
          } catch (Throwable th) {
                int i=0;
                i++;
            }
    }
//        void createTemplateFromHex() {
//
//            String cleanhex = command.replaceAll("\\.", "").trim();
//            int size=cleanhex.length();
//            assert (size & 1) == 0;
//            size>>=1;
//            templateData= new byte[size];
//            templateData=new BinaryValue().getCompletedCmd(this);
//
//    }
//
//    void createTemplateFromText() {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    //  -------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OmniRigCmdData other=(OmniRigCmdData) obj;
        if ((this.pmcmd == null) ? (other.pmcmd != null) : !this.pmcmd.equals(other.pmcmd)) {
            return false;
        }
        if ((this.radioID == null) ? (other.radioID != null) : !this.radioID.equals(other.radioID)) {
            return false;
        }
        if ((this.command == null) ? (other.command != null) : !this.command.equals(other.command)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash=5;
        hash=79 * hash + (this.pmcmd != null ? this.pmcmd.hashCode() : 0);
        hash=79 * hash + (this.radioID != null ? this.radioID.hashCode() : 0);
        hash=79 * hash + (this.command != null ? this.command.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ORC: ").append(pmcmd).append(", ").append(command).toString();
    }

    //------------------------------------------------------
    /**
     *
     */
    static class Flag {

        final int statusId;
        final BigInteger mask;
        final BigInteger bits;
        final PMValues param;
//        final int numBytes;
//        final int numLong;
//        final int intSizeBytes;

        @Override
        public String toString() {
            StringBuilder result=new StringBuilder("flag: ");
            result.append(param.toString()).append(", s:").append(statusId);
            return result.toString().trim();
        }

        /**
         *
         * @param flgStr
         */
        Flag(final String[] flgStr) {
            super();

            this.statusId=Integer.parseInt(flgStr[0]);
            String[] maskbit=new String[]{flgStr[1], flgStr[2]};
            normalizeMaskBits(maskbit);
//            numBytes=maskbit[0].length() >> 1;
//            intSizeBytes=Integer.SIZE >> 3;
//            int numL=numBytes / intSizeBytes;
//            int rem=numBytes % intSizeBytes;
//            if (rem > 0) {
//                numL++;
//            }
           // numLong=numL;
           // if (numLong == 1) {
                mask=new BigInteger(maskbit[0],16);
                bits=new BigInteger(maskbit[1],16);
               // mask=new long[]{Long.parseLong(maskbit[0], 16)};
               // bits=new long[]{Long.parseLong(maskbit[1], 16)};
//            } else {
//                mask=new long[numLong];
//                bits=new long[numLong];
//                makeLongArray(maskbit[0], mask);
//                makeLongArray(maskbit[1], bits);
//            }
            param=PMValues.valueOf(flgStr[3]);
        }

//        private void makeLongArray(final String lngIn, final long[] out) {
//            if (lngIn.isEmpty()){
//               for (int idx=0; idx < out.length; idx++) {
//               out[idx]=0L;
//                }
//
//
//            }else{
//            int loc=0;
//            for (int idx=0; idx < out.length; idx++) {
//                int numcharperint = intSizeBytes<<1;
//                int endloc = loc+numcharperint;
//                if (endloc>lngIn.length()){
//                    endloc=lngIn.length();
//                }
//                 long templ;
//                String temps = lngIn.substring(loc, endloc);
//                if (temps.isEmpty()){
//                    int i=0;
//                    i++;
//                    templ=0L;
//                }else{
//                 templ = Long.parseLong(temps,16);
//                }
//               // final int endloc=(loc + (intSizeBytes << 1) < lngIn.length()) ? loc + (intSizeBytes << 1) : lngIn.length();
//               // out[idx]=Long.parseLong(lngIn.substring(loc, endloc), 16);
//                out[idx]=templ;
//                loc=endloc;
//            }
//            }
//        }

//        byte[] makeByteArray(final long[] lngIn) {
//            if (lngIn.length != numLong) {
//                throw new IllegalArgumentException();
//            }
//            byte[] result=new byte[numBytes];
//            int idx=0;
//            for (long lg : lngIn) {
//                for (int i=0; i < intSizeBytes; i++) {
//                }
//            }
//            return result;
//        }

//        private long[] makeLongArray(final byte[] payload) {
//            THE_LOGGER.severe("not yet implemented************************");
//            return new long[0];
//        }
//
//        private long makeLong(final byte[] payload) {
//            long result=0;
//            if (payload.length > 8) {
//                throw new IllegalArgumentException();
//            }
//            if (payload.length == 1) {
//                result=((long) payload[0] & 0xFF);
//                return result;
//            }
//            int shift=0;
//            for (int i=payload.length - 1; i >= 0; i--) {
//                long temp=((long) payload[i] & 0xFF);
//                temp<<=shift;
//                shift+=8;
//                result|=temp;
//            }
//
//            return result;
//        }
//
//        private byte[] makeByte(final long[] longval) {
//            byte[] result=new byte[0];
//            THE_LOGGER.severe("not yet implemented************************");
//            return result;
//        }
//
//        byte[] getMaskedByte(final byte[] inval) {
//            byte[] result=new byte[0];
//            THE_LOGGER.severe("not yet implemented************************");
//            return result;
//
//        }

        private boolean isNotSatisfied(final byte[] payload) {
            BigInteger temp =new BigInteger(payload);
            temp = mask.and(temp);
            return !temp.equals(bits);
//
//            if (this.numLong == 1) {
//                long longval=makeLong(payload);
//                long temp=longval & mask[0];
//                return temp != bits[0];
//
//            } else {
//                StringBuilder hexString=new StringBuilder();
//                for (byte b : payload) {
//                    hexString.append(Integer.toString(b, 16));
//                }
//                long[] mybits=new long[numLong];
//                makeLongArray(hexString.toString(), mybits);
//                for (int idx=0; idx < mybits.length; idx++) {
//                    mybits[idx]=mybits[idx] & mask[idx];
//                }
//                for (int idx=0; idx < mybits.length; idx++) {
//                    if (mybits[idx] != bits[idx]) {
//                        return true;
//                    }
//                }
//
//            }
//            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Flag other=(Flag) obj;
            if (this.statusId != other.statusId) {
                return false;
            }
            if (this.mask != other.mask && (this.mask == null || !this.mask.equals(other.mask))) {
                return false;
            }
            if (this.bits != other.bits && (this.bits == null || !this.bits.equals(other.bits))) {
                return false;
            }
            if (this.param != other.param) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash=5;
            hash=97 * hash + this.statusId;
            hash=97 * hash + (this.mask != null ? this.mask.hashCode() : 0);
            hash=97 * hash + (this.bits != null ? this.bits.hashCode() : 0);
            hash=97 * hash + (this.param != null ? this.param.hashCode() : 0);
            return hash;
        }
        //--------------------------------------------------------------------------------------


    }
}
