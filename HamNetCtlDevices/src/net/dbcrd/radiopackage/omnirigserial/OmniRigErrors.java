
package net.dbcrd.radiopackage.omnirigserial;

/**
 *
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 enum OmniRigErrors {
    ORS_0001("ORS_0001: one");
    String err;
    OmniRigErrors(final String arg){
        err=arg;
    }
    String getErr(){
        return err;
    }
}
