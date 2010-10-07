package net.dbcrd.radiopackage.omnirigserial;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tool to obtain the OmniRig supported radios.
 * @author dbcurtis
 */
public class OmniRigDefFileList {

    static private final String DIR_ID="net/dbcrd/omniRigInitFiles/";
    static private final ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
    static private final NavigableSet<File> iniFiles=new ConcurrentSkipListSet<File>();
    static private final NavigableMap<String, File> mapFn2File=new ConcurrentSkipListMap<String, File>();
    static private final Logger THE_LOGGER=Logger.getLogger(OmniRigDefFileList.class.getName());

    private OmniRigDefFileList() {
        final URL dirURL=classLoader.getResource(DIR_ID);
        final File dirFile;
        try {
            dirFile=new File(dirURL.toURI());
        } catch (URISyntaxException ex) {
            THE_LOGGER.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException();
        }
        if (dirFile.isDirectory()) {
            iniFiles.addAll(Arrays.asList(dirFile.listFiles(new INIFileFilter())));
            for (File file : iniFiles) {
                mapFn2File.put(file.getName(), file);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public URL getURL(final String filename) {
        
        if (mapFn2File.containsKey(filename)){
            try {
                return mapFn2File.get(filename).toURI().toURL();
            } catch (MalformedURLException ex) {
               throw new IllegalArgumentException("specified file error");
            }
        }
        throw new IllegalArgumentException("specified file is unknown");
    }

    /**
     *
     * @return
     */
     NavigableSet<File> getFiles() {
        final NavigableSet<File> result=new ConcurrentSkipListSet<File>();
        for (File file : iniFiles) {
            result.add(new File(file.toURI()));
        }
        return result;
    }

    /**
     *
     * @return
     */
     Iterator<File> getFileIterator() {
        return getFiles().iterator();
    }

    /**
     *
     * @return
     */
    public NavigableSet<String> getFileNames() {
        final NavigableSet<String> result=new ConcurrentSkipListSet<String>();
        for (String fn : mapFn2File.keySet()) {
            result.add(new StringBuilder(fn).toString());
        }
        return result;
    }

    /**
     *
     * @return
     */
     List<URL> getURLs() {
        final List<URL> result=new LinkedList<URL>();
        for (File file : iniFiles) {
            try {
                result.add(file.toURI().toURL());
            } catch (MalformedURLException ex) {
                THE_LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /**
     *
     * @return
     * @throws MalformedURLException
     */
     Iterator<URL> getURLIterator() throws MalformedURLException {
        return getURLs().iterator();
    }

    /**
     *
     * @return
     */
   public  static OmniRigDefFileList getInstance() {
        return OmniRigDefFileListHolder.INSTANCE;
    }

    /**
     * Holds the singleton
     */
     static class OmniRigDefFileListHolder {

        private static final OmniRigDefFileList INSTANCE=new OmniRigDefFileList();
    }

    /**
     *  retains .ini files
     */
    static class INIFileFilter implements FileFilter {

        @Override
        public boolean accept(final File f) {
            return !f.isDirectory() && f.getName().toLowerCase().trim().endsWith(".ini");
        }
    }
}
