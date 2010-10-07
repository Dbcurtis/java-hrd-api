
package net.dbcrd.radiopackage.omnirigserial;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import net.dbcrd.radiopackage.RadioStatus;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class OmniRigSetFreqCmd extends OmniRigCmdData {

    /**
     *
     * @param pmcmd
     * @param radioID
     * @param command
     * @param reply
     * @param validate
     * @param values
     */
     OmniRigSetFreqCmd(final String pmcmd,
                             final String radioID,
                             final String command,
                             final String reply,
                             final String[] validate,
                             final List<String[]> values) {

        super(pmcmd, radioID, command, reply, validate, values);
    }

    /**
     *
     * @param pmcmd
     * @param radioID
     */
     OmniRigSetFreqCmd(final String pmcmd, final String radioID) {
        super(pmcmd, radioID);
    }

    @Override
    public Properties doCmd(
            final OmniRigStruct radio,
            final BlockingQueue<PktInfo> client2serverQ,
            final Object... args) throws IOException {

        if (args.length==0){
            throw new IllegalArgumentException();
        }
        if (!(args[0] instanceof Long)){
             throw new IllegalArgumentException();
        }
        final long freq =(Long) args[0];
        byte[] cmddata = binaryVal.getCompletedCmd(this, freq);
      
        super.sendCmd(radio, client2serverQ,cmddata);
        radio.putStatus(RadioStatus.FREQ, Long.toString(radio.getFreq()));
        return  radio.getStatus();
    }

//    @Override
//    public boolean initialize() {
//        boolean result=super.initialize();
//
//        if(values.isEmpty()){
//            binaryVal=new BinaryValue();
//        } else{
//            binaryVal=new BinaryValue(values.iterator().next());
//        }
//        cmdData=binaryVal.getCompletedCmd(this);
//        return result;
//    }

   private String getsetresult(final PktInfo replypkt){
        return "reply ignored";
    }

    private byte[] addFreq(final byte[] cmdData,final long freq) {
         byte[] newcmd = new byte[cmdData.length];
        System.arraycopy(cmdData, 0, newcmd, 0, cmdData.length);
        throw new UnsupportedOperationException("Not yet implemented");
       // return newcmd;
    }

        @Override
        public String toString(){
        StringBuilder sb = (new StringBuilder(super.toString())).replace(0, 2, "FRQC: ");
        return sb.toString();
    }
}
