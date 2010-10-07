
package net.dbcrd.radiopackage.omnirigserial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.Assert;
import static org.junit.Assert.*;

/**
 *
 * @author dbcurtis
 */
public class RadioIniFileAccess {
    final static String FILE_ID = "net/dbcrd/omniRigInitFiles/FT-897.ini";
   // final static String FILE_ID = "net/dbcrd/omniRigInitFiles/Kenwood.ini";
    static private final ClassLoader classLoader=Thread.currentThread().getContextClassLoader();

   /**
    *
    */ public RadioIniFileAccess() {
    }

    /**
     *
     * @return
     */
    static URL getOmniIniFileURL(){
        return classLoader.getResource(FILE_ID);

    }

    /**
     *
     * @param sbin
     * @return
     */
    static String getOmniIniFileData(final StringBuilder sbin) {
        File file=null;
        FileInputStream fstream=null;
        DataInputStream inStr=null;
        BufferedReader bReader=null;
        /*
         * Read in the ini file.
         */
        //  final StringBuilder sbin=new StringBuilder(4000);
        try {

            final URL url= getOmniIniFileURL();
            if (null == url) {
                fail("\nurl for test file not found.\n");
                return null;
            }
            try {
                file=new File(url.toURI());
            } catch (URISyntaxException ex) {
                fail("\nillegal url for test file.\n");
                return null;
            }

            fstream=new FileInputStream(file);
            inStr=new DataInputStream(fstream);
            bReader=new BufferedReader(new InputStreamReader(inStr));
            String strLine;

            while ((strLine=bReader.readLine()) != null) {
                sbin.append(strLine).append("\n");
            }
            return file.getName();
        } catch (FileNotFoundException ex) {
            Assert.fail("ini file not found");
            return null;
        } catch (IOException ex) {
            Assert.fail("bad wolf");
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
