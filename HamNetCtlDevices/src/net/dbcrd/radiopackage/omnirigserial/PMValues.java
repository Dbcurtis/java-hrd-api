package net.dbcrd.radiopackage.omnirigserial;

/**
 * Mode values for OmniRig deffinitions.
 * Many of these are not implemented.
 * @author dbcurtis
 */

@ClassPreamble(date="4/6/2010")
enum PMValues {

    /**
     * operating frequency
     */
    pmFreq,//      operating frequency
    /**
     *VFO A frequency
     */
    pmFreqA,//     VFO A frequency
    /**
     *   VFO B frequency
     */
    pmFreqB,//      VFO B frequency
    /**
     *  CW pitch frequency
     */
    pmPitch,//      CW pitch frequency
    /**
     * RIT offset frequency
     */
    pmRitOffset,//  RIT offset frequency
    /**
     *  Clear RIT - this is a write-only parameter
     */
    pmRit0,//       Clear RIT - this is a write-only parameter
    /**
     * receive and transmit on VFO A
     */
    pmVfoAA,//      receive and transmit on VFO A
    /**
     *receive on VFO A, transmit on VFO B
     */
    pmVfoAB,//      receive on VFO A, transmit on VFO B
    /**
     * receive on VFO B, transmit on VFO A
     */
    pmVfoBA,//      receive on VFO B, transmit on VFO A
    /**
     *eceive and transmit on VFO B
     */
    pmVfoBB,//      receive and transmit on VFO B
    /**
     * receive on VFO A, transmit VFO unknown
     */
    pmVfoA,//       receive on VFO A, transmit VFO unknown
    /**
     * receive on VFO B, transmit VFO unknown
     */
    pmVfoB,//       receive on VFO B, transmit VFO unknown
    /**
     * copy the frequency of the receive VFO to the transmit VFO
     */
    pmVfoEqual,//   copy the frequency of the receive VFO to the transmit VFO
    /**
     *   swap frequencies of the receive and transmit VFO's
     */
    pmVfoSwap,//    swap frequencies of the receive and transmit VFO's
    /**
     *    enable split operation
     */
    pmSplitOn,//    enable split operation
    /**
     *    disable split operation
     */
    pmSplitOff,//   disable split operation
    /**
     *   enable RIT
     */
    pmRitOn,//      enable RIT
    /**
     *disable RIT
     */
    pmRitOff,//     disable RIT
    /**
     *  enable XIT
     */
    pmXitOn,//      enable XIT
    /**
     *disable XIT
     */
    pmXitOff,//     disable XIT
    /**
     * enable receive mode
     */
    pmRx,//         enable receive mode
    /**
     *enable transmit mode
     */
    pmTx,//         enable transmit mode
    /**
     *CW mode, upper sideband
     */
    pmCW_U,//       CW mode, upper sideband
    /**
     *   CW mode, lower sideband
     */
    pmCW_L,//       CW mode, lower sideband
    /**
     *USB mode
     */
    pmSSB_U,//      USB mode
    /**
     *   LSB mode
     */
    pmSSB_L,//      LSB mode
    /**
     *Digital mode (RTTY, FSK, etc.), upper sideband
     */
    pmDIG_U,//      Digital mode (RTTY, FSK, etc.), upper sideband
    /**
     * Digital mode, lower sideband
     */
    pmDIG_L,//      Digital mode, lower sideband
    /**
     *AM mode
     */
    pmAM,//         AM mode
    /**
     *   FM mode
     */
    pmFM, //        FM mode
    /**
     * unknown mode
     */
    Unknown
}
