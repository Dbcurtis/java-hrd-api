
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetup;
import net.dbcrd.radiopackage.CommSetupInput;

/**
 *
 * @author dbcurtis
 */
public  class CommSetup4Test {
   static private CommSetup comm;
  

   private  CommSetup4Test() {

    }

 static void setComm(){
       if (null==comm){
         comm=CommSetupInput.main(new String[]{"comm"});
       }
   }

  static CommSetup getComm(){
      return comm;
  }

  static void clearComm(){
      comm=null;
  }

}
