
package net.dbcrd.radiopackage.omnirigserial;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
class OmniRigSetTrnEnCmd extends OmniRigCmdData {

    /**
     *
     * @param pmcmd
     * @param radioID
     * @param command
     * @param reply
     * @param validate
     * @param values
     */
     OmniRigSetTrnEnCmd(final String pmcmd,
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
     OmniRigSetTrnEnCmd(final String pmcmd, final String radioID) {
        super(pmcmd, radioID);
    }

    @Override
    public Properties doCmd(
            final OmniRigStruct radio,
            final BlockingQueue<PktInfo> client2serverQ, Object... args) throws IOException {

        if (args.length != 0) {
            throw new IllegalArgumentException();
        }

      //  final Properties result=new Properties();
        super.sendCmd(radio, client2serverQ, binaryVal.getCompletedCmd(this));
        //result.putAll(radio.getStatus());
       // result.setProperty(RadioStatus.CMD_STATUS, "OK");
        return  radio.getStatus();
    }

//    @Override
//    public boolean initialize() {
//        boolean result=super.initialize();
//
//        if (values.isEmpty()) {
//            binaryVal=new BinaryValue();
//        } else {
//            binaryVal=new BinaryValue(values.iterator().next());
//        }
//        cmdData=binaryVal.getCompletedCmd(this);
//        return result;
//    }
    @Override
        public String toString(){
        StringBuilder sb = (new StringBuilder(super.toString())).replace(0, 2, "TNC: ");
        return sb.toString();
    }
}
