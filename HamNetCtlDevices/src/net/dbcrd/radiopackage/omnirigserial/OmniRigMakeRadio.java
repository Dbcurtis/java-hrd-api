
package net.dbcrd.radiopackage.omnirigserial;

import net.dbcrd.radiopackage.CommSetupInput;
import net.dbcrd.radiopackage.CommSetup;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dbcrd.radiopackage.RadioControl;

/**
 * Makes a radio from a OnmniRig.ini URL
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
public class OmniRigMakeRadio {

    private static final Logger THE_LOGGER=Logger.getLogger(OmniRigMakeRadio.class.getName());
    private CommSetup comm;
    static BlkQServer server;
    static Map<CommSetup, RadioControl> radioMap=new ConcurrentHashMap<CommSetup, RadioControl>();
    static BlockingQueue<PktInfo> client2serverQ;

    static void setServer(BlkQServer server) {
        OmniRigMakeRadio.server=server;
    }

     static void setClient2serverQ(BlockingQueue<PktInfo> client2serverQ) {
        OmniRigMakeRadio.client2serverQ=client2serverQ;
    }

    public OmniRigMakeRadio() {
        comm=null;
    }

   /**
    *
    * @return
    */
    public static BlkQServer getServer() {
        return server;
    }

    /**
     * Stops a radio that was made by makeRadio.
     * @param radio a RadioControl of the radio to be stopped
     */
    public synchronized void stop(final RadioControl radio) {
       

        if (radioMap.containsKey(comm)&& radio.equals(radioMap.get(comm))) {
            radio.stop();
            final BlkServerCmd regCommPort=new BlkServerCmd(BlkServerCmd.CmdType.DEREGISTER_PORT, comm.getSerialPortStr());
            client2serverQ.offer(new PktInfo(regCommPort)); //de-register the comm port with the server

            radioMap.remove(comm);
            if (radioMap.isEmpty()) {
                int cntdwn=50;
  
                while (cntdwn-- > 0) {
                    if (0 == server.getNumRadios()) {
                        break;
                    }
                    try {
                        Thread.sleep(100); //TODO fix this
                    } catch (InterruptedException ex) {
                        THE_LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
                server.stop();
            }
        }
      
    }

   /**
    *
    * @param omniRigIniURL a URL of the OmniRig.ini file used for the radio
    * @param args either not exist or a 1 element array with the CommSetup to be used
    * @return a RadioContorl generated in accordance with the .ini file.
    */
    public synchronized RadioControl makeRadio(final URL omniRigIniURL, final CommSetup... args) {
        /*
         * Instantiate the server, get the blocking queue for commands to the server, and activate the server
         * this is not needed for the test, but is Best Operating Practice when in production.
         */

        setServer(BlkQServer.getInstance());
        if (!server.getServerThread().isAlive()) {
            setClient2serverQ(server.getClient2ServerQ()); 
            server.activate();
        } else {
             setClient2serverQ(server.getClient2ServerQ());
        }

        /*
         *  read the ini file
         */
        StringBuilder sbin=new StringBuilder(4000);
        final String filename=getData(sbin, omniRigIniURL);

        /*
         * instantiate the OmniRig radio.ini fileData reader
         */

        final OmniRigIniReader omniRigBuilder=new OmniRigIniReader(filename, sbin);
        /*
         * get the radio from the reader
         */
        final RadioControl radio=omniRigBuilder.getRadio(client2serverQ);

        /*
         * Specify the serial communication line to the radio (if not already)
         */
        if(args.length==1){
            comm=args[0];

        }
        if (null == comm) {
            comm=CommSetupInput.main(new String[]{"comm"});
        }

        /*
         * Generate a "register comm port" command and submit to the server.
         */
        final BlkServerCmd regCommPort=new BlkServerCmd(BlkServerCmd.CmdType.REGISTER_PORT, comm);
        client2serverQ.offer(new PktInfo(regCommPort)); // register the comm port with the server

        /*
         * Activate the radio on the comm port
         */

        radio.activate(comm);
        radioMap.put(comm, radio);
        return radio;
    }

    private String getData(final StringBuilder insb, final URL url) {
        File file=null;
        FileInputStream fstream=null;
        DataInputStream inStr=null;
        BufferedReader bReader=null;
        /*
         * Read in the ini file.
         */

        try {
            if (null == url) {
                THE_LOGGER.severe("\nurl for test file not found.\n");
                return null;
            }
            try {
                file=new File(url.toURI());
            } catch (URISyntaxException ex) {
                THE_LOGGER.severe("\nillegal url for test file.\n");
                return null;
            }

            fstream=new FileInputStream(file);
            inStr=new DataInputStream(fstream);
            bReader=new BufferedReader(new InputStreamReader(inStr));
            String strLine;
            while ((strLine=bReader.readLine()) != null) {
                insb.append(strLine).append("\n");
            }
            return file.getName();
        } catch (FileNotFoundException ex) {
            THE_LOGGER.severe("ini file not found");
            return null;
        } catch (IOException ex) {
            THE_LOGGER.severe("IO Error");
            return null;

        } finally {
            if (null != bReader) {
                try {
                    bReader.close();
                } catch (Exception e) {
                }
            }
            if (null != inStr) {
                try {
                    inStr.close();
                } catch (Exception e) {
                }
            }
            if (null != fstream) {
                try {
                    fstream.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
