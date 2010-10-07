/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.dbcrd.radiopackage.omnirigserial;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
class BlkServerCmd {

    enum CmdType {
/**
 *
 */
        REGISTER_PORT,
        /**
         *
         */
        DEREGISTER_PORT,
        /**
         *
         */
        REGISTER_ECHO,
        /**
         * 
         */
        REGISTER_SIM,
    }
    private final CmdType cmd;
    private final Object cmdObj;

    BlkServerCmd(final CmdType cmd, final Object cmdObj) {
        super();
        this.cmd=cmd;
        this.cmdObj=cmdObj;
    }

     CmdType getCmd() {
        return cmd;
    }

     Object getCmdObj() {
        return cmdObj;
    }
    
}
