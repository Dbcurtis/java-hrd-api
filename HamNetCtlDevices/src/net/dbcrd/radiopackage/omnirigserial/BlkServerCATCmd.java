

package net.dbcrd.radiopackage.omnirigserial;

/**
 * a radio command object.  the byte[] has the data to be sent to the radio.
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
class BlkServerCATCmd {

    private final byte[] cmdObj;

    /**
     *
     * @param cmdObj a byte[] that is the serial  data to be sent to the radio.
     * The cmdObj is copied and is thus, not backed by cmdObj
     */
    BlkServerCATCmd(final byte[] cmdObj) {
        super();
        this.cmdObj=new byte[cmdObj.length];
        System.arraycopy(cmdObj, 0, this.cmdObj, 0, cmdObj.length);    
    }
/**
 *
 * @return a byte[] that is a copy of the cmdObj.
 * The returned byte[] is not backed by the object
 */
    byte[] getCmdObj() {
        final byte[] result = new byte[cmdObj.length];
        System.arraycopy(cmdObj, 0, result, 0, cmdObj.length);
        return result;
    }
}
