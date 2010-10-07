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


 class OmniRigGetStatusCmd extends OmniRigCmdData {

    /**
     *
     * @param pmcmd
     * @param radioID
     * @param command
     * @param reply
     * @param validate
     * @param values
     */
    public OmniRigGetStatusCmd(final String pmcmd,
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
    public OmniRigGetStatusCmd(final String pmcmd, final String radioID) {
        super(pmcmd, radioID);
    }

    /**
     *
     * @param radio
     * @param client2serverQ
     * @return
     */
    public Properties getStatus(final OmniRigStruct radio,
            final BlockingQueue<PktInfo> client2serverQ) throws IOException {

        final PktInfo replypkt = super.sendCmd(radio, client2serverQ);
        return decodeStatus(replypkt);
    }

//    @Override
//    public boolean initialize() {
//
//        final boolean result = super.initialize();
//        if (values.isEmpty()) {
//            binaryVal = new BinaryValue();
//        } else {
//            binaryVal = new BinaryValue(values.iterator().next());
//        }
//        cmdData = binaryVal.getCompletedCmd(this);
//        return result;
//    }
        @Override
        public String toString(){
        StringBuilder sb = (new StringBuilder(super.toString())).replace(0, 2, "GSC: ");
        return sb.toString();
    }
}
