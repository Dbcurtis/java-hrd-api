
package net.dbcrd.radiopackage.omnirigserial;

import java.util.logging.Logger;

/**
 * Class used to translate values to what the radio is expecing and from what the radio sends to what the program expects
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
final  class BinaryValue {
        private static final Logger THE_LOGGER=Logger.getLogger(BinaryValue.class.
            getName());
/** an int that contains the value# for those that are sequenced, else 0*/
    final int valseq;
    /** start position in the byte[] data to insert a value */
    int startPos;
    /** number of bytes to insert in the byte[] data */
    int len;
    /** multiply the supplied value by this, and add the add */
    Double multiply;
    /** the add to be added to the *** */
    int add;
    /**  */
    String param;
    /** format for the storage of the value */
    NumericValues fmt;




    /**
     * bcd big endian unsigned multiplier 1.0, no add
     */
    BinaryValue() {
        startPos=0;
        len=0;
        valseq=0;
        multiply=1.0D;
        add=0;
        fmt=null;
        param="";
    }

    BinaryValue(final NumericValues fmt) {
        startPos=0;
        len=0;
        valseq=0;
        multiply=1.0D;
        add=0;
        this.fmt=fmt;
        lastSeenNV=fmt;
        param="";
    }

    static NumericValues getLastSeenNV() {
        return lastSeenNV;
    }

    private static NumericValues lastSeenNV;
//    private static final Pattern VALUE_NUM_PAT = Pattern.compile("value(\\d+)\\s*=", Pattern.CASE_INSENSITIVE);

   /**
    *
    * @param val a String[5] , [6] or [8]  String[6] where
    * [0] is the starting position
    * [1] is the len
    * [2] is the format string
    * [3] is the multiplier
    * [4] is the add
    * [5] is the param
    * [7]
    * [8]
    *  I think [8] is not yet implemented
    * the valseq is set to 0
    */
    BinaryValue(final String[] val) {
        if(val.length==5){
            startPos=Integer.parseInt(val[0]);
            len=Integer.parseInt(val[1]);
            fmt=NumericValues.valueOf(val[2]);
            lastSeenNV=fmt;
            multiply=Double.parseDouble(val[3]);
            add=Integer.parseInt(val[4]);

            param="";
            valseq=0;
            return;
        }
       if(val.length==6){
            startPos=Integer.parseInt(val[0]);
            len=Integer.parseInt(val[1]);
            fmt=NumericValues.valueOf(val[2]);
             lastSeenNV=fmt;
            multiply=Double.parseDouble(val[3]);
            add=Integer.parseInt(val[4]);
            param=val[5];
            valseq=0;
            return;
        }
        if (val.length == 7) {
         //   final Matcher mx=VALUE_NUM_PAT.matcher(val[0]);
         //   if (mx.find()) {
                valseq=Integer.parseInt(val[0]);
                startPos=Integer.parseInt(val[1]);
                len=Integer.parseInt(val[2]);
                multiply=Double.parseDouble(val[4]);
                add=Integer.parseInt(val[5]);
                fmt=NumericValues.valueOf(val[3]);
                 lastSeenNV=fmt;
                param=val[6];
                return;
           // }
        }
        throw new IllegalArgumentException();
    }

   /**
    * Gets the data for the radio corrosponding to the command
    * @param cmdTmpl a OmniRigCmdData
    * @return a byte[] the is the template for the cmdTmpl   (that is not modified by an argument)
    */
    byte[] getCompletedCmd(final OmniRigCmdData cmdTmpl) {
         return NumericValues.convertData(cmdTmpl.command, fmt);

    }

    /**
     *Gets the data for the radio corrosponding to the command as modified by the value
     * @param cmdTmpl a OmniRigCmdData
     * @param value a long that is to be applied to the cmdTmpl
     * @return a byte[] that is the cmdTmpl data modified by the value
     */
    byte[] getCompletedCmd(final OmniRigCmdData cmdTmpl, final long value) {
        final byte[] result=new byte[cmdTmpl.cmdData.length];
        System.arraycopy(cmdTmpl.cmdData, 0, result, 0, cmdTmpl.cmdData.length);
        final Double dbl=((value*1.0D)*multiply)+add;
        final byte[] valbyte=convertDbl2Bytes(dbl);   
        System.arraycopy(valbyte, 0, result, startPos, len);
        return result;
    }

//    /**
//     *
//     * @param cmdTmpl a OmniRigCmdData
//     * @return a byte[] that is the template for the cmdTmpl   (that is not modified by an argument)
//     */
//    //TODO Why this and the getcompletedcmd with the single argument?
//    private byte[] getTemplateCmd(final OmniRigCmdData cmdTmpl) {
//        return NumericValues.convertData(cmdTmpl.command, fmt);
//    }

    /**
     * converts a double to bytes
     * @param dbl a double
     * @return a byte[]
     */
    private byte[] convertDbl2Bytes(final Double dbl) {
        if(dbl<0){
            throw new IllegalArgumentException();
        }
        final long lng=dbl.longValue();
        if(lng>=Integer.MAX_VALUE){
            throw new IllegalArgumentException();
        }
        return NumericValues.convertData(Long.valueOf(lng).intValue(), fmt, len);
    }
}
