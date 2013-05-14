import java.util.*;

/*
 * A class designed for Standard CAN frame receivers, which parses the bits
 * into semantic fields for easy consumption and debugging.
 *
 * TODO: It should also verify the CRC and throw an exception in the
 * constructor if the CRC does not verify.
 *
 **/
public class SemanticCANFrame{
    int SOF; //1 bit
    int ARB1; // 11 bits 
    int RTR; //1 bit
    int IDE; //1 bit
    int r0 ; //1 bit
    int DLC; //4 bit
    byte[] data; //1 to 8 bytes
    int CRC; //15 bits 
    int CRC_Delim; //1 bit
    int ACK_Slot; //1 bit
    int ACK_Delim; //1 bit
    int EOF; //7 bit


    public static SemanticCANFrame GetInstance(BitString frame){
        try {
            SemanticCANFrame sframe = new SemanticCANFrame(frame);
            return sframe;
        }
        catch(RuntimeException e){
            return null;
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
         sb.append("SOF " + SOF + "\n"); //1 bit
         sb.append("ARB1 " + ARB1 + "\n"); // 11 bits 
         sb.append("RTR " + RTR + "\n"); //1 bit
         sb.append("IDE " + IDE + "\n"); //1 bit
         sb.append("r0  " + r0  + "\n"); //1 bit
         sb.append("DLC " + DLC + "\n"); //4 bit
         sb.append("Data " + Util.ByteArrayToHexString(data) + "\n"); //1 to 8 bytes
         sb.append("CRC " + CRC + "\n"); //15 bits 
         sb.append("CRC_Delim " + CRC_Delim + "\n"); //1 bit
         sb.append("ACK_Slot " + ACK_Slot + "\n"); //1 bit
         sb.append("ACK_Delim " + ACK_Delim + "\n"); //1 bit
         sb.append("EOF " + EOF + "\n"); //7 bit
         return sb.toString();
    }
    /*
     * Throw an exception if the CRC does not check or there are other frm
     * 
     * TODO: Verify CRC by factoring CRC code out.
     *
     */
    private SemanticCANFrame(BitString frame) throws RuntimeException{
        int index = 0;
        SOF = frame.getBit(index++);
        ARB1 = frame.toInt(index, index + 11); index+=11;
        RTR = frame.getBit(index++);
        IDE = frame.getBit(index++);
        r0 = frame.getBit(index++);
        DLC = frame.toInt(index, index + 4); index += 4;
        data = frame.toByteArray(index, index + DLC * 8); index += DLC * 8;
        CRC = frame.toInt(index, index + 15); index += 15;
        CRC_Delim = frame.getBit(index++);
		ACK_Slot = frame.getBit(index++);
        EOF = frame.toInt(index, index + 7);; index += 7; //7 bit
    }
}
