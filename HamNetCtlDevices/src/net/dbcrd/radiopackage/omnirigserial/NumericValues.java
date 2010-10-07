package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.AbstractRadio;
import java.math.BigInteger;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
enum NumericValues {

    /*
     * These examples show the values of 123 and -123 converted to
     * a sequence of 4 bytes according to different formats:
     *
     *
     *  Value:       123          -123
     *  ----------------------------------
     *  vfText   30.31.32.33   2D.31.32.33
     *  vfBinL   7B.00.00.00   85.FF.FF.FF
     *  vfBinB   00.00.00.7B   FF.FF.FF.85
     *  vfBcdLU  23.01.00.00   n/a
     *  vfBcdLS  23.01.00.00   23.01.00.FF
     *  vfBcdBU  00.00.01.23   n/a
     *  vfBcdBS  00.00.01.23   FF.00.01.23
     *  vfYaesu  00.00.00.7B   80.00.00.7B
     */
    vfText, //asc codes of digits
    vfBinL, //integer, little endian
    vfBinB, //integer, big endian
    vfBcdLU, //BCD, little endian, unsigned
    vfBcdLS, //BCD, little endian, signed; the sign is in the MSB byte (0x00 or 0xFF)
    vfBcdBU, //big endian, unsigned
    vfBcdBS, //big endian, signed
    vfYaesu; //special format used by Yaesu
    static final Integer ZEROINT=Integer.valueOf(0);
    /**
     *
     */
    private static final char[] HEX_CHAR_TBL=new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
 private static final Logger THE_LOGGER = Logger.getLogger(NumericValues.class.getName());
    /**
     * convert a payload from the radio into a long value in accordance with the format
     * @param payload a byte[] that contains the payload received from the radio.
     * @param format a NumericValues that specifies how the conversion is to be done.
     * @retun a long representing the value returned from the radio
     */
    static long convertData(final byte[] payload, final NumericValues format) {
        return convertDatta(payload, format);

    }

    /**
     * Generates a byte[] that contains the inVal value as formatted as specified by format.
     * @param inVal an int that contains the value to be converted.
     * @param format a NumericValues the specifies the format for the conversion.
     * @param size an optional int to specify the the number of bytes to be returned.  If blank, 4 bytes
     * @return a byte[] containing in the byte order to be sent of the inVal being converted.
     */
    static byte[] convertData(final int inVal, final NumericValues format, final int... size) {
        return convertDataa(Integer.valueOf(inVal), format, (size.length == 0) ? 4 : size[0]);
    }

    /**
     * Generates a byte[] that contains the inVal value as formatted as specified by format.
     * @param inStr a String that contains the data to be converted.
     * @param format a NumericValues the specifies the format for the conversion.
     * @param size an optional int to specify the the number of bytes to be returned.  If blank the size of the return is the number of
     * characters in the string
     * @return a byte[] containing in the byte order to be sent of the inVal being converted.
     */
    static byte[] convertData(final String instr, final NumericValues format, final int... size) {
        if (null==instr||instr.length() == 0) {
            throw new IllegalArgumentException();
        }
        return convertDataa(instr, format, (size.length == 0) ? instr.length() : size[0]);
    }

    /**
     * Generates a byte[] that contains the longVal value as formatted as specified by format.
     * @param longVal an int that contains the value to be converted.
     * @param format a NumericValues the specifies the format for the conversion.
     * @param size an optional int to specify the the number of bytes to be returned.  If blank, 8 bytes
     * @return a byte[] containing in the byte order to be sent of the longVal being converted.
     */
    static byte[] convertData(final long longVal, final NumericValues format, final int... size) {
        return convertDataa(Long.valueOf(longVal), format, (size.length == 0) ? 8 : size[0]);
    }

//    private static void processVfText(final String arg, final byte[] result, final int size) {
//        byte[] temp = new byte[0];
//        try {
//            temp =arg.getBytes("US-ASCII");
//        } catch (UnsupportedEncodingException ex) {
//            THE_LOGGER.log(Level.SEVERE, null, ex);
//        }
//        System.arraycopy(temp , 0, result, 0, temp.length);
//    }

    /**
     *
     * @param arg an Object for the value to be converted
     * @param format a NumericValues to specifies the format for the conversion.
     * @param size an int that specifies the number of bytes to be returned.
     * @return a byte[] containing the arg value as formatted in accordance with format
     */
    private static byte[] convertDataa(final Object arg, final NumericValues formatIn, final int size) {

         NumericValues format;
         if (formatIn==null){
             format=NumericValues.vfText;
         }else{
             format=formatIn;
         }
        int mysize=size;
        byte[] result=new byte[0];
         try{
        switch (format) {

            case vfText:
                if (arg instanceof String) {
                    String strarg=(String) arg;
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfText(arg, size, result, mysize);
                }
                break;
            case vfBinL:
                if (arg instanceof String) {
                    String strarg=(String) arg;
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfBinL(arg, size, result);
                }

                break;
            case vfBinB:
                if (arg instanceof String) {
                    String strarg=(String) arg;
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfBinB(arg, size, result);
                }
                break;
            case vfBcdLU:
                if (arg instanceof String) {
                    String strarg=(String) arg;
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfBcdLU(arg, result);
                }
                break;
            case vfBcdLS:
                if (arg instanceof String) {
                    String strarg=(String) arg;
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfBcdLS(arg, result);
                }
                break;
            case vfBcdBU:
                
                if (arg instanceof String) {
                    try {
                        int asize=size;
                        String arga=(String) arg;
                        String argb="";

                        if (arga.trim().startsWith("(")) {
                            throw new IllegalArgumentException();
                        } else {
                            argb=arga.replaceAll("\\.", "").trim();
                            asize=argb.length();

                            if ((asize & 1) != 0) {
                                throw new IllegalArgumentException();
                            }
                            asize>>=1;
                            BigInteger bint = new BigInteger(argb.toString(), 16);
                            argb=bint.toString();
                            result=new byte[asize];
                            processVfBcdBU(argb, asize, result);
                            break;
                        }

                    } catch (Throwable th) {
                        int i=0;
                        i++;
                    }
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    final int intarg=(Integer) arg;
                    final long longarg=0xffffffffL & intarg;
                    final byte[] temp=AbstractRadio.int2BCD((int) longarg, 8);
                    System.arraycopy(temp, 0, result, 0, 4);
                }

                break;
            case vfBcdBS:
                if (arg instanceof String) {
                    String strarg=(String) arg;

                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                    processVfBcdBS(arg, result);
                }
                break;
            case vfYaesu:
                if (arg instanceof String) {
                    
                    final String arga=(String) arg;
                    if (arga.trim().startsWith("(")) {
                        throw new IllegalArgumentException();
                    } else {
                        String argb=arga.replaceAll("\\.", "").trim();
                        int asize=argb.length();
                        if ((asize & 1)!= 0){
                              throw new IllegalArgumentException();
                        }
                        asize>>=1;
                        argb=Long.toString(Long.valueOf(argb.toString(), 16));
                        result=new byte[asize];
                        processVfYaesu(argb, asize, result);
                        break;
                    }
                }
                if (arg instanceof Long) {
                    Long longarg=(Long) arg;
                    break;
                }
                if (arg instanceof Integer) {
                    result=new byte[size];
                        processVfYaesu((Integer) arg, size, result);
                    }
                    break;
                default:
                    break;
            
        }
         } catch (Throwable th) {
            int i=0;
            i++;
        }
        return result;

    }

    /**
     *
     * @param arg
     * @param size
     * @param result
     */
    private static void processVfYaesu(final Integer arg, final int size, final byte[] result) {
        final int intarg=arg;
        final long absval=(long) ((intarg < 0) ? -intarg : intarg);
        long longarg=0xffffffffL & absval;
        final Deque<Integer> stack=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            stack.push(Integer.valueOf((int) (0xFF & longarg)));
            longarg=longarg >> 8;
        }
        int idx=0;
        while (!stack.isEmpty()) {
            result[idx++]=stack.pop().byteValue();
        }
        if (intarg < 0) {
            result[0]=(byte) 0x80;
        }
    }


    /**
     *
     * @param arg a String that is the decimal value of the result to be returned
     * @param size
     * @param result
     */
    private static void processVfYaesu(final String arg, final int size, final byte[] result) {

        final long inArgLong=Long.parseLong(arg);
        final long absVal=(inArgLong < 0) ? -inArgLong : inArgLong;
        long longArg=0x9fffffffffffffffL & absVal;
        final Deque<Integer> stack=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            stack.push(Integer.valueOf((int) (0xFF & longArg)));
            longArg=longArg >> 8;
        }
        int idx=0;
        while (!stack.isEmpty()) {
            result[idx++]=stack.pop().byteValue();
        }
        if (inArgLong < 0) {
            result[0]=(byte) (0x80 & result[0]);
        }
    }

    /**
     *
     * @param arg
     * @param size
     * @param result
     */
    private static void processVfBcdBU(final String arg, final int size, final byte[] result) {
        final long inArgLong=Long.parseLong(arg);
     
        long longArg=inArgLong;
        final Deque<Integer> stack=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            stack.push(Integer.valueOf((int) (0xFF & longArg)));
            longArg=longArg >> 8;
        }
        int idx=0;
        while (!stack.isEmpty()) {
            result[idx++]=stack.pop().byteValue();
        }
    }

    /**
     *
     * @param arg
     * @param size
     * @param result
     */
    private static void processVfBinB(final Object arg, final int size, final byte[] result) {
        final int intArg=(Integer) arg;
        long longArg=0xffffffffL & intArg;
        final Deque<Integer> stack=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            stack.push(Integer.valueOf((int) (0xFF & longArg)));
            longArg=longArg >> 8;
        }
        int idx=0;
        while (!stack.isEmpty()) {
            result[idx++]=stack.pop().byteValue();
        }
    }

    /**
     *
     * @param arg
     * @param size
     * @param result
     */
    private static void processVfBinL(final Object arg, final int size, final byte[] result) {
        final int intArg=(Integer) arg;
        long longArg=0xffffffffL & intArg;
        final Deque<Integer> fifo=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            fifo.add(Integer.valueOf((int) (0xFF & longArg)));
            longArg=longArg >> 8;
        }
        int idx=0;
        while (!fifo.isEmpty()) {
            result[idx++]=fifo.poll().byteValue();
        }
    }

    /**
     *
     * @param arg
     * @param result
     */
    private static void processVfBcdBS(final Object arg, final byte[] result) {
        final int intArg=(Integer) arg;
        final long absVal=(long) ((intArg < 0) ? -intArg : intArg);
        final long longArg=0xffffffffL & absVal;
        final byte[] temp=AbstractRadio.int2BCD((int) longArg, 8);
        System.arraycopy(temp, 0, result, 0, 4);
        if (intArg < 0) {
            result[0]=(byte) 0xff;
        }
    }

    /**
     *
     * @param arg
     * @param size
     * @param result
     * @param sizeIn
     */
    private static void processVfText(final Object arg, final int size, final byte[] result, final int sizeIn) {
        int mySize=sizeIn;
        final int intArg=(Integer) arg;
        final long absVal=(long) ((intArg < 0) ? -intArg : intArg);
        long longArg=0xffffffffL & absVal;
        final Deque<Integer> stack=new LinkedList<Integer>();
        for (int i=0; i < size; i++) {
            final long longArgDiv10=longArg / 10;
            stack.push(Integer.valueOf((int) (longArg - longArgDiv10 * 10L)));
            longArg=longArgDiv10;
            if (longArg == 0) {
                break;
            }
        }
        final int stackSize=stack.size();
        int idx=0;
        if (intArg < 0) {
            result[idx++]=(byte) 0x2d;
            mySize--;
        }
        for (int i=stackSize; i < mySize; i++) {
            stack.push(ZEROINT);
        }
        while (!stack.isEmpty()) {
            final byte idd=stack.pop().byteValue();
            result[idx++]=(byte) HEX_CHAR_TBL[idd];
        }
    }

    /**
     *
     * @param inbyte
     * @param str
     */
    static void byte2BcdChar(final byte inbyte, final StringBuilder str) {
//        final int top=(inbyte >> 4) & 0x0f;
//        final int bot=inbyte & 0x0f;
        str.append("*************** wrong stuff ***********");//TODO fix this
    }

    /**
     *
     * @param arg
     * @param result
     */
    private static void processVfBcdLS(final Object arg, final byte[] result) {
        final int intArg=(Integer) arg;
        final long absVal=(long) ((intArg < 0) ? -intArg : intArg);
        final long longArg=0xffffffffL & absVal;
        final byte[] temp=AbstractRadio.int2BCD((int) longArg, 8);
        int rIdix=0;
        int tempIdx=3;
        result[rIdix++]=temp[tempIdx--];
        result[rIdix++]=temp[tempIdx--];
        result[rIdix++]=temp[tempIdx--];
        result[rIdix++]=(intArg < 0) ? (byte) 0xFF : temp[tempIdx--];
    }

    /**
     *
     * @param arg
     * @param result
     */
    private static void processVfBcdLU(final Object arg, final byte[] result) {
        final int intArg=(Integer) arg;
        final long longArg=0xffffffffL & intArg;
        final byte[] temp=AbstractRadio.int2BCD((int) longArg, 8);
        int rIdx=0;
        int tempIdx=3;
        result[rIdx++]=temp[tempIdx--];
        result[rIdx++]=temp[tempIdx--];
        result[rIdx++]=temp[tempIdx--];
        result[rIdx++]=temp[tempIdx--];
    }

    /*
     * These examples show the values of 123 and -123 converted to
     * a sequence of 4 bytes according to different formats:
     *
     *
     *  Value:       123          -123
     *  ----------------------------------
     *  vfText   30.31.32.33   2D.31.32.33
     *  vfBinL   7B.00.00.00   85.FF.FF.FF
     *  vfBinB   00.00.00.7B   FF.FF.FF.85
     *  vfBcdLU  23.01.00.00   n/a
     *  vfBcdLS  23.01.00.00   23.01.00.FF
     *  vfBcdBU  00.00.01.23   n/a
     *  vfBcdBS  00.00.01.23   FF.00.01.23
     *  vfYaesu  00.00.00.7B   80.00.00.7B
     */
    /**
     * 
     * @param payload
     * @param format
     * @return
     */
    private static long convertDatta(final byte[] payLoad, final NumericValues format) {
 
        long result=0;
        switch (format) {
            case vfText: //asc codes of digits  -  30.31.32.33   2D.31.32.33
                StringBuilder sb=new StringBuilder();
                for (byte b : payLoad) {
                    char ch=(char) b;
                    sb.append(ch);
                }
                result=Long.parseLong(sb.toString().trim());
                break;

            case vfBinL: //integer, little endian - 7B.00.00.00   85.FF.FF.FF
            {
                int shift=24;
                for (int i=3; i >= 0; i--) {
                    long temp=((long) payLoad[i]) & 0xFF;
                    temp<<=shift;
                    shift-=8;
                    result|=temp;
                }
                if ((payLoad[3] & 0x80) != 0) {
                    result|=0xFFFFFFFF00000000L;
                }

            }
            break;

            case vfBinB: //integer, big endian -  00.00.00.7B   FF.FF.FF.85
            {
                int shift=24;
                for (int i=0; i < 4; i++) {
                    long temp=((long) payLoad[i]) & 0xFF;
                    temp<<=shift;
                    shift-=8;
                    result|=temp;
                }
                if ((payLoad[0] & 0x80) != 0) {
                    result|=0xFFFFFFFF00000000L;
                }
            }
            break;

            case vfBcdLU: //BCD, little endian, unsigned  -  23.01.00.00   n/a
            {
                sb=new StringBuilder();
                for (int i=3; i >= 0; i--) {
                    byte val=payLoad[i];
                    sb.append(HEX_CHAR_TBL[(val & 0xF0) >> 4]).append(HEX_CHAR_TBL[(val & 0xF)]);
                }
                result=Long.parseLong(sb.toString());
            }
            break;

            case vfBcdLS: //BCD, little endian, signed; the sign is in the MSB byte (0x00 or 0xFF) - 23.01.00.00   23.01.00.FF
            {
                sb=new StringBuilder();
                for (int i=2; i >= 0; i--) {
                    byte val=payLoad[i];
                    sb.append(HEX_CHAR_TBL[(val & 0xF0) >> 4]).append(HEX_CHAR_TBL[(val & 0xF)]);
                }
                result=Long.parseLong(sb.toString());
                result=payLoad[3] < 0 ? -1 * result : result;
            }
            break;

            case vfBcdBU: //big endian, unsigned -- 00.00.01.23   n/a
            {
                sb=new StringBuilder();
                for (int i=0; i < 4; i++) {
                    byte val=payLoad[i];
                    sb.append(HEX_CHAR_TBL[(val & 0xF0) >> 4]).append(HEX_CHAR_TBL[(val & 0xF)]);
                }
                result=Long.parseLong(sb.toString());
            }
            break;

            case vfBcdBS: //big endian, signed --    00.00.01.23   FF.00.01.23
            {
                sb=new StringBuilder();
                for (int i=1; i < 4; i++) {
                    byte val=payLoad[i];
                    sb.append(HEX_CHAR_TBL[(val & 0xF0) >> 4]).append(HEX_CHAR_TBL[(val & 0xF)]);
                }
                result=Long.parseLong(sb.toString());
                result=payLoad[0] < 0 ? -1 * result : result;
            }
            break;

            case vfYaesu: //special format used by Yaesu -- 00.00.00.7B   80.00.00.7B
            {
                int shift=16;
                for (int i=1; i < 4; i++) {
                    long temp=((long) payLoad[i]) & 0xFF;
                    temp<<=shift;
                    shift-=8;
                    result|=temp;
                }
                boolean neg=(payLoad[0] & 0x80) != 0;
                long upper=payLoad[0] & (0x7F);
                upper<<=24;
                result|=upper;
                result=neg ? -1 * result : result;
            }
            break;

            default:
                result=-1;
        }
        return result;
    }
}
