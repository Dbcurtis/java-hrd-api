
package net.dbcrd.radiopackage.omnirigserial;

/**
 * A class for simulating a FT-897
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
 class SimFT897 extends AbstractBlkSimWorker {

    private boolean lock=false;
    private boolean ptt=false;
    private boolean inCW=false;
    private byte[] freqBCDMode=new byte[]{ //1452300 lsb
        (byte) 0x14,
        (byte) 0x52,
        (byte) 0x30,
        (byte) 0x00,
        (byte) 00};

    @Override
     void doWork(PktInfo pkt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//                case 0:
//                    currentMode=Mode.LSB;
//                    break;
//                case 1:
//                    currentMode=Mode.USB;
//                    break;
//                case 2:
//                    currentMode=Mode.CW;
//                    break;
//                case 3:
//                    currentMode=Mode.CWR;
//                    break;
//                case 4:
//                    currentMode=Mode.AM;
//                    break;
//                case 6:
//                    currentMode=Mode.WFM;
//                    break;
//                case 8:
//                    currentMode=Mode.FM;
//                    break;
//                case 0x0A:
//                    currentMode=Mode.DIG;
//                    break;
//     private static final byte[] PTT_ON_CMD={0, 0, 0, 0, (byte) 0x08};
//    private static final byte[] PTT_OFF_CMD={0, 0, 0, 0, (byte) 0x88};
//    private static final byte[] LOCK_ON_CMD={0, 0, 0, 0, (byte) 0x00};
//    private static final byte[] LOCK_OFF_CMD={0, 0, 0, 0, (byte) 0x80};
//    private static final byte[] SET_MODE_FM_CMD={8, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_AM_CMD={4, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_CW_CMD={2, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_CWR_CMD={3, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_DIG_CMD={(byte) 0x0A, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_USB_CMD={1, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_MODE_LSB_CMD={0, 0, 0, 0, (byte) 0x07};
//    private static final byte[] SET_FREQ_CMD={0, 0, 0, 0, (byte) 0x01};
//    private static final byte[] READ_FREQ_CMD={0, 0, 0, 0, (byte) 0x03};
//    //   private static final byte[] READ_RX_STAT_CMD={0, 0, 0, 0, (byte) 0xE7};
//    //   private static final byte[] READ_TX_STAT_CMD={0, 0, 0, 0, (byte) 0xF7};
//    //  private static final byte[] setRepOffsetCmd={0, 0, 0, 0, (byte) 0x09};
//    private static final byte[] SET_REP_SIMPLEX_CMD={(byte) 0x89, 0, 0, 0, (byte) 0x09};
//    private static final byte[] SET_REP_POS_CMD={(byte) 0x49, 0, 0, 0, (byte) 0x09};
//    private static final byte[] SET_REP_NEG_CMD={(byte) 0x09, 0, 0, 0, (byte) 0x09};
//    private static final byte[] SET_REP_OFF_FREQ_CMD={0, 0, 0, 0, (byte) 0xF9};
//    private static final byte[] SET_TONE_MODE_ON={(byte) 0x4A, 0, 0, 0, (byte) 0x0A};
//    private static final byte[] SET_TONE_MODE_OFF={(byte) 0x8A, 0, 0, 0, (byte) 0x0A};
//    private static final byte[] SET_TONE_FREQ={0, 0, 0, 0, (byte) 0x0B};
    enum FT897cmd {

        PTTON(0X08),
        PTTOFF(0X88),
        LOCKON(0),
        LOCKOFF(0X80),
        SETFREQ(0X01),
        READFREQ(0X03),
        SETMODE(0X07),
        READTXST(0xF7),
        READRXST(0xE7),
        UNKNOWN(0xFFFFF);
        int cmd;

        FT897cmd(final int xx) {
            cmd=xx;
        }
    }

    @Override
    Object doSimRadio(Object payload) {
        byte[] result=new byte[(byte) 0xF0];
        if(payload instanceof byte[]){
            byte[] pl=(byte[]) payload;
            if(pl.length!=5){
                return result;
            }
            FT897cmd cmd=FT897cmd.UNKNOWN;
            for(FT897cmd c:FT897cmd.values()){
                if(c.cmd==pl[4]){
                    cmd=c;
                    break;
                }
            }
            switch(cmd){
                case PTTON:
                    ptt=true;
                    result=new byte[(byte) 0x00];
                    break;
                case PTTOFF:
                    ptt=false;
                    result=new byte[(byte) 0x00];
                    break;
                case LOCKON:
                    lock=true;
                    result=new byte[(byte) 0x00];
                    break;
                case LOCKOFF:
                    lock=false;
                    result=new byte[(byte) 0x00];
                    break;
                case SETFREQ:
                    result=new byte[(byte) 0x00];
                    break;
                case READFREQ:
                    byte[] ss=new byte[5];
                    System.arraycopy(freqBCDMode, 0, ss, 0, 5);
                    result=ss;
                    break;
                case READRXST:
                    break;
                case READTXST:

                    break;
                case SETMODE:
//                        if (pl[0]==2||pl[0]==3){
//                            if(pl[0]==2){
//                                if(inCW){}else{
//                                inCW=true;
//
//                                }
//                            }else{
//                               if(inCW){}else{}
//                            }
//                        }else{
//                             if(inCW){}else{}
//                        }
                    freqBCDMode[4]=pl[0];
                    result=new byte[(byte) 0x00];

                    break;
                default:
                    result=new byte[(byte) 0x00];
                    break;
            }
        }
        return result;
    }
}
