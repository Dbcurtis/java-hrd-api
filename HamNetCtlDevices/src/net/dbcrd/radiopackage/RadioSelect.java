package net.dbcrd.radiopackage;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import net.dbcrd.radiopackage.hrdtcpipclient.RadioHRD;
import net.dbcrd.radiopackage.omnirigserial.OmniRigDefFileList;
import net.dbcrd.radiopackage.omnirigserial.OmniRigMakeRadio;

/**
 *
 * @author dbcurtis
 */
public final class RadioSelect {

    private static final Preferences RADIO_SEL_PREFS=Preferences.userRoot().node(RadioSelect.class.getName());
    final static String DEFAULT_RADIO="unknown_radio";
    /**
     *
     */
    public final static String RADIO="radio";
    private  static ExecutorService myWorkerPool=null;
    static final String SELECTED_RADIO="selected radio";
    static final String RESET_VAL="Unknown";
    private static final Logger THE_LOGGER=
            Logger.getLogger(RadioSelect.class.getName());

    public static void setMyWorkerPool(final ExecutorService myWorkerPool) {
        if (null == RadioSelect.myWorkerPool) {
            RadioSelect.myWorkerPool=myWorkerPool;
        }
    }

//    public static ExecutorService getMyWorkerPool() {
//        return myWorkerPool;
//    }

    private static RadioControl getRadioFromName(final String radio) {

        if(DEFAULT_RADIO.equals(radio)){
            return new RadioUnknown(false);
        } else{
            String myR = radio;
            if (myR.startsWith("OR-")){
                myR="OMNI_RIGP";
            }
            switch(RadioIdentification.valueOf(myR.toUpperCase())){
                case HRD:
                    return new RadioHRD();
                case OMNI_RIGS:
                    return new RadioUnknown(false);
//                case YAESU_FT8X7:
//                    return new RadioFT8x7(false);
                case OMNI_RIGP:
                     OmniRigDefFileList orf=OmniRigDefFileList.getInstance();
                     URL radiodef=orf.getURL(new StringBuilder(radio.trim()).append(".ini").toString());
                     return new OmniRigMakeRadio().makeRadio(radiodef);
                     
                default:
                    return new RadioUnknown(false);
            }
        }
    }

    private RadioSelect() {
        super();

    }

    /**
     *
     * @param wp
     * @return
     */
    public static RadioControl getRadioPref() {
        RadioControl result;
        final String selectedRadioST=RADIO_SEL_PREFS.get(SELECTED_RADIO, RESET_VAL);
        if(RESET_VAL.equals(selectedRadioST)){
            result=selectRadio();
        } else{
            result=getRadio(selectedRadioST);
        }
        return result;
    }

    /**
     *
     * @param wp
     * @return
     */
    public static RadioControl selectRadio() {
        return getUserSpecifiedRadio();
    }

    /**
     *
     */
    public static void resetPreference() {
        RADIO_SEL_PREFS.put(SELECTED_RADIO, RESET_VAL);
    }

    /**
     *
     * @param selectedRadioST
     * @return
     */
    public static RadioControl getRadio(final String selectedRadioST) {    
        return getRadioFromName(selectedRadioST);
    }
   
    /**
     *
     * @return
     */
    private static RadioControl getUserSpecifiedRadio() {
        return RadioSelectorGUI.main(new String[]{""}, RADIO_SEL_PREFS, myWorkerPool);
    }
}
