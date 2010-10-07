package net.dbcrd.radiopackage;

/**
 * Class for dummy radio
 *
 * @author Daniel B. Curtis N6WN
 *
 */
public class RadioUnknown extends AbstractRadio {

    /**
     * Instanitate but do not activate a RadioHRD object.
     * @param activate a boolean  that should be false
     */
    public RadioUnknown(final boolean activate) {
        super(RadioIdentification.UNKNOWN);
        isRadio=false;
        setAbortClientThread(true);
    }

    /**
     *
     * @return a boolean that is true if activated, false if not activated
     * if not activated the port and host are set to default.
     */
    public final boolean activate(Object... arg) {
        isRadio=false;
        setAbortClientThread(true);
        return false;
    }


    public boolean setDefaultComm(Object... vargs) {
        isRadio=false;
        setAbortClientThread(true);
        return true;
    }

    /**
     * Instanitate and activate a RadioHRD object.
     * @param computer a String used to identify a computer ("localhost" etc.)
     * @param port an int that is the port number to be used.
     */
    public RadioUnknown(final String computer, final int port) {
        super(RadioIdentification.UNKNOWN);
        activate();

    }

    /**
     * Instanitate and activate a RadioHRD object.
     * Uses the preferences to get the HRDserver name, and port and trys to connect.
     * May wait up to 9 sec for the conection to take place.  Then will try the
     * default localhost 7809 and may wait up to 9 more seconds for the connection.
     * Should attempt an operation (like set mode) to see if a connection was made.
     */
    public RadioUnknown() {
        super(RadioIdentification.UNKNOWN);
        activate();
    }

    @Override
    public boolean pttPush() {
        return true;
    }

    @Override
    public boolean pttRelease() {
        return true;
    }

    /**
     *
     */
    @Override
    public boolean setFreq(final long freq) {
        return false;
    }

    /**
     * Set the radio's mode.
     * @param mode a Mode
     * @return boolean false if radio not recognized or mode not able to be set after 10 attempts, otherwise true
     */
    @Override
    public boolean setMode(final Mode mode) {
        return false;
    }

    /**
     * Get the radio's frequency.
     * @return a long representing the current frequency of the radio, or 0 if the radio is not recognized
     */
    @Override
    public long getFreq() {
        return 0L;
    }

    /**
     * Gets the radio's mode.
     * @return a Mode (or Mode.UNSPECIFIED if radio is not recognized).
     */
    @Override
    public Mode getMode() {
        return Mode.UNSPECIFIED;
    }

    /**
     * Identifies the radio and CAT mode.  The CAT mode is (HRD).  The radio is the id from HRD.
     * @return a String containing the radioID or "HRD Radio not connected" if radio is not recognized.
     */
    @Override
    public String getRadioID() {
        return "Radio not connected";
    }

    /**
     * If the radio recognised, returns whether the radio is locked.  If the radio is not recognised, returns false.
     * @return a boolean true if the radio recognised and is locked, false if the radio is not recognised, or recognised and not locked
     */
    @Override
    public boolean isLock() {
        return false;
    }

    /**
     * Returns whether radio is currently under CAT control.
     * @return a boolean true if under CAT control, false otherwise.
     */
    @Override
    public boolean isThisRadioControlled() {
        return false;
    }

    /**
     * Not implemented. Logs a severe error if anything other than "0" specified.
     * @param pltone a String.
     * @return true if pltone is "0", false otherwise.
     */
    @Override
    public boolean setPlTone(final String pltone) {
        return false;
    }

    /**
     * Sets the radio's "lock" control. Will try 5 times to set and confirm.
     * @param lock a boolean: true to lock, false to unlock.
     * @return  a boolean indicating whether the operation succeeded.
     */
    @Override
    public boolean setLock(final boolean lock) {
        return true;
    }

    /**
     * Sets a repeater offset and repeater offset direction. Does not see the repeater offset, but will set
     * the negative/positive/simplex operation responsive to repeaterOffset
     * @param repeaterOffset a long: if 0 radio is set to simplex, if negative to negative repeater offset, and
     * if positive to positive repeater offset.
     * @return a boolean true - always.
     */
    @Override
    public boolean setRepeaterOffset(final long repeaterOffset) { //TODO add confirm and retry
        return true;
    }


    /**
     * empty method.
     */
    @Override
    public void editSelf() {
        //empty method
    }

    /**
     * empty method.
     */
    @Override
    public void stop() {
        //empty method
    }

    @Override
    public void saveCommPrefs(CommSetup comdata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
