import java.util.*;

/* 
 * Class for constructing standard CAN Frames to send on the wire.
 * This class also manages bit-by-bit sending.
 */
class CANFrame{

    private int length;
    private BitSet bits; 
    private byte[] data; 
    private int bitPosition;

    /*
     * Reads the rightmost bits of the input from left to right.
     */
    private int AddBits(int bits, int bitlength){
         for (int i = 0; i < bitlength; i++){
            int index = length / 8; // # of bits in a byte
            int bindex = length % 8;
            int curBit = (bits >> (bitlength - i - 1)) & 1; //this should assume it is on the right
            if (curBit == 1) this.bits.set(length);
            else this.bits.clear(length);

            length++;
         }
         return length;
    }

    /** 
      * Overload for byte array. 
      * TODO: Remove the assumption that the number of bits is length * 8 
     */
    private int AddBits(byte[] bits, int bitlength){
        assert bitlength % 8 == 0;
        for (byte b : bits) AddBits(b, 8); 
        return length;
    }

    /* 
     * The length field must be equal to exactly the previous part when computing this CRC.
     * 
     * Returns a 15-bit CRC based on the standard. 
     * Page 48 of Bosch Standard.   
     */
    private int ComputeCRC(){
        int CRC_RG = 0;
        for (int i = 0; i < length; i++){
            int CRCNXT = getBit(i) ^ (CRC_RG >> 13); //Bit 0 is on the right hand side.
            CRC_RG <<= 1;
            if (CRCNXT != 0) CRC_RG ^= 0x4599;
        }
        return CRC_RG;
    }
    /* 
     * Assume Standard format for now.
     *
     * Remember that Java is Big Endian.
     *
     * This class merely represents a CANFrame and maintains state about where we
     *  are reading in it.
     */
    public CANFrame(byte[] data, int ID){
        this.data = data;
        this.bits = new BitSet();
        this.length = 0;
        this.bitPosition = 0;

        // construct the actual frame one part at a time
        int SOF = 0; //1 bit
        int ARB1 = ID; // 11 bits - for now
        int RTR = 0; //1 bit
        int IDE = 0; //1 bit
        int r0 = 0; //1 bit
        int DLC = data.length; //4 bit
        int CRC_Delim = 1; //1 bit
        int ACK_Slot = 1; //1 bit
        int ACK_Delim = 1; //1 bit
        int EOF = 255; //7 bit
        
        AddBits(SOF, 1);
        AddBits(ARB1, 11);
        AddBits(RTR, 1);
        AddBits(IDE, 1);
        AddBits(r0, 1);
        AddBits(DLC, 4);
        AddBits(data, data.length * 8);

        int CRC = ComputeCRC(); //15 bits 
        AddBits(CRC, 15);
        AddBits(CRC_Delim, 1);
        AddBits(ACK_Slot, 1);
        AddBits(ACK_Delim, 1);
        AddBits(EOF, 7);
    }

    private int getBit(int bitPosition){
        return bits.get(bitPosition) ? 1 : 0;
    }

    public int getNextBit(){
        if (bitPosition >= length) return -1;
        int retVal = getBit(bitPosition);
        bitPosition++;
        return retVal;
    }
    public void reset(){
        bitPosition = 0;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        int bit = -1;
        while ((bit = this.getNextBit()) != -1) sb.append(bit);
        this.reset();
        return sb.toString();
    }
}
