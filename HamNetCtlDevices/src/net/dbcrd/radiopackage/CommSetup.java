package net.dbcrd.radiopackage;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dbcrd.radiopackage.omnirigserial.ClassPreamble;

/**
 * Communication setup for CAT control, both serial and TCP/IP (HRD)
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
public class CommSetup {

    private static final Logger THE_LOGGER=Logger.getLogger(CommSetup.class.getName());


    /**
     * specify 1, 1.5, or 2 stopbits in accordance with RXTX equivalents
     */
    public enum SPortStopBits {

        /**
         *
         */
        STOPBITS_1(SerialPort.STOPBITS_1),
        /**
         *
         */
        STOPBITS_1_5(SerialPort.STOPBITS_1_5),
        /**
         *
         */
        STOPBITS_2(SerialPort.STOPBITS_2);
        private final int spVal;

        SPortStopBits(final int spval) {
            spVal=spval;
        }

        /**
         *
         * @return  int containing the SerialPort stopbits value
         */
        public int getSerialPortVal() {
            return spVal;
        }
    }

    /**
     * specify 5, 6, 7, 8 data bits in accordance with RXTX
     */
    public enum SPortDataBits {

        /**
         *
         */
        DATABITS_5(SerialPort.DATABITS_5),
        /**
         *
         */
        DATABITS_6(SerialPort.DATABITS_6),
        /**
         *
         */
        DATABITS_7(SerialPort.DATABITS_7),
        /**
         *
         */
        DATABITS_8(SerialPort.DATABITS_8);
        private final int spVal;

        SPortDataBits(final int spval) {
            spVal=spval;
        }

        /**
         *
         * @return an int containing the SerialPort dataBits_? value
         */
        public int getSerialPortVal() {
            return spVal;
        }
    }


    /**
     * specify none, even or odd parity in accordance with  RXTX
     */
    public enum SPortParity {

        /**
         *
         */
        NONE(SerialPort.PARITY_NONE),
        /**
         *
         */
        EVEN(SerialPort.PARITY_EVEN),
        /**
         *
         */
        ODD(SerialPort.PARITY_ODD),
        /**
         *
         */
        MARK(SerialPort.PARITY_MARK),
        /**
         *
         */
        SPACE(SerialPort.PARITY_SPACE);
        /**
         *
         */
        private final int spVal;

        /**
         *
         * @param spval
         */
        SPortParity(final int spval) {
            spVal=spval;
        }

        /**
         *
         * @return
         */
        public int getSerialPortVal() {
            return spVal;
        }
    }

    private FlowCtlSel flowctl=FlowCtlSel.NONE;

   public  FlowCtlSel getFlowctl() {
        return flowctl;
    }

    private boolean valid=false;

    public SPortDataBits getDataBits() {
        return dataBits;
    }

    public SPortParity getParity() {
        return parity;
    }

    public SPortStopBits getStopBits() {
        return stopBits;
    }

    /**
     *
     * @return a boolean true if the object is valid
     */
     boolean isValid() {
        return valid;
    }
    /** name of the system hosting a virtual CAT */
    final String hostStr;
    /** a String that contains the TCP/IP Port number for a virtual CAT*/
    final String tcpipPortStr;
    /** a String that contains the communication port identification for a physical CAT  */
    final String serialPortStr;
    /** a String that contains the baud rate for a serial port*/
    final String baud;
    /**  specifies the parity for the serial port */
    final SPortParity parity;
    /** specifies the parity for the serial port */
    final SPortStopBits stopBits;

    public String getSerialPortStr() {
        return serialPortStr;
    }

    public String getBaud() {
        return baud;
    }

    public String getHostStr() {
        return hostStr;
    }

    public String getTcpipPortStr() {
        return tcpipPortStr;
    }

    public boolean isVirtual() {
        return virtual;
    }
    /** specifies the stopbits for the serial port */
    final SPortDataBits dataBits;
    /** specifies the databits for the serial port */
    final boolean comm;
    /** specifies whether the CAT is using a physical radio or virtual radio */
    final boolean virtual;

    @Override
    public String toString() {
        if (comm) {
            final StringBuilder sbTemp=new StringBuilder();
            sbTemp.append("comm: ").append(serialPortStr).append(", ").append(baud).append(", ").
                    append(parity.toString()).append(", ").append(stopBits.toString()).
                    append(", ").append(dataBits.toString());
            return sbTemp.toString();
        }
        if (virtual) {
            final StringBuilder sbTemp=new StringBuilder();
            sbTemp.append("virtual: ").append(hostStr).append(", ").append(tcpipPortStr);
            return sbTemp.toString();
        }
        return "illegal";
    }

    /**
     *  sets up a non-virtual, 9600baud, com6, odd parity, 1 stop bit, 8 databits serial CAT
     */
     CommSetup() {
        super();
        comm=true;
        virtual=false;
        hostStr="none";
        baud="9600";
        tcpipPortStr="";
        serialPortStr="COM6";
        flowctl=FlowCtlSel.NONE;
        parity=SPortParity.ODD;
        stopBits=SPortStopBits.STOPBITS_1;
        dataBits=SPortDataBits.DATABITS_8;
    }

    /**
     *
     * @param hostStr a String used to identify HRD host
     *
     * @param tcpIpPort a String specifying the TCP/IP Port number for a virtual CAT
     */
     CommSetup(final String hostStr, final String tcpIpPort) {
        super();
        comm=false;
        virtual=true;
        this.hostStr=hostStr;
        baud="";
        this.tcpipPortStr=tcpIpPort;
        serialPortStr="";
        flowctl=FlowCtlSel.NONE;
        parity=SPortParity.NONE;
        stopBits=SPortStopBits.STOPBITS_1;
        dataBits=SPortDataBits.DATABITS_8;
        valid=(!this.hostStr.isEmpty() && !this.tcpipPortStr.isEmpty());
    }

    /**
     *
     * @param serialPortStr a String that contains the communication port identification for a physical CAT
     * @param baud   a String specifying the baud rate for the physical CAT
     * @param parity a SPortParity specifying the parity.
     * @param stopbit a SPortStopBits specifying the number of stop bits
     * @param databits a SportDataBits specifying the number of databits.
     * @param flowctl an int to specify flow control mode
     */
     CommSetup(
            final String serialPortStr,
            final String baud,
            final SPortParity parity,
            final SPortStopBits stopbit,
            final SPortDataBits databits,
            final FlowCtlSel flowctl) {

        this.comm=true;
        this.virtual=false;
        this.hostStr="";
        this.tcpipPortStr="";
        this.serialPortStr=serialPortStr;
        this.baud=baud;
        this.parity=parity;
        this.stopBits=stopbit;
        this.flowctl=flowctl;
        this.dataBits=databits;
        valid=(!this.serialPortStr.isEmpty() && !this.baud.isEmpty());
    }

    /**
     *
     * @param comm a boolean, true if a physical CAT
     * @param virtual a boolean, true if a virtual CAT
     * @param serialPortStr a String, which if comm is true, then is the physical comm  id
     * @param baud a String for a baud rate for a physical CAT.
     * @param parity a SPortParity
     * @param stopbit a SPortStopBits
     * @param databits a SPortDataBits
     * @param flowctl
     * @param hostStr a String with the hostStr name for a virtual CAT
     * @param tcpIpPort a String with the tcpipPortStr number for a virtual CAT
     */
     CommSetup(
            final boolean comm,
            final boolean virtual,
            final String serialPortStr,
            final String baud,
            final SPortParity parity,
            final SPortStopBits stopbit,
            final SPortDataBits databits,
            final FlowCtlSel flowctl,
            final String hostStr,
            final String tcpIpPort) {

        super();
        this.comm=comm;
        this.virtual=virtual;
        this.serialPortStr=serialPortStr;
        this.baud=baud;
        this.parity=parity;
        this.stopBits=stopbit;
        this.dataBits=databits;
        this.hostStr=hostStr;
        this.flowctl=flowctl;
        this.tcpipPortStr=tcpIpPort;
        if (virtual) {
            valid=(!this.hostStr.isEmpty() && !this.tcpipPortStr.isEmpty());
        }
        if (comm) {
            valid=(!this.serialPortStr.isEmpty()
                    && !this.baud.isEmpty());
        }
    }

 
    /**
     * Get the port type identification.
     * @param portType
     * @return a String indicating the port type can be one of "I2C", "Parallel", "Raw", "RS485", "Serial" or "unknown type"
     */
     static String getPortTypeName(final int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    /**
     * Returns all serial ports.
     * @return a Set
     */
     static Set<CommPortIdentifier> getAllSerialPorts() {
        final HashSet<CommPortIdentifier> result=new HashSet<CommPortIdentifier>();

        @SuppressWarnings("unchecked")
        final Enumeration<CommPortIdentifier> thePorts=CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            final CommPortIdentifier com=thePorts.nextElement();
            if (CommPortIdentifier.PORT_SERIAL == com.getPortType()) {
                result.add(com);
            }
        }
        return result;
    }

    /**
     * Returns available serial ports.  Works by getting each port and trying to open it.
     * @return    A HashSet containing the CommPortIdentifier for all serial ports that are not currently being used.
     */
     static Set<CommPortIdentifier> getAvailableSerialPorts() {
        final Set<CommPortIdentifier> set=new HashSet<CommPortIdentifier>();
         @SuppressWarnings("unchecked")
        final Enumeration<CommPortIdentifier> thePorts=CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            final CommPortIdentifier com=thePorts.nextElement();
            if (CommPortIdentifier.PORT_SERIAL == com.getPortType()) {
                try {
                    final CommPort thePort=com.open("CommUtil", 50);
                    thePort.close();
                    set.add(com);
                } catch (PortInUseException e) {
                    THE_LOGGER.fine(new StringBuilder().append("Port, ").append(com.getName()).append(", is in use.").toString());
                } catch (Exception e) {
                    THE_LOGGER.log(Level.WARNING, new StringBuilder().append("Failed to open port ").append(com.getName()).toString(), e);
                }
            }
        }
        return set;
    }
}
