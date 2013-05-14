import java.util.*;

public class Util{
    public static String ByteArrayToHexString(byte[] in){
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < in.length; i++){
            String hex = Integer.toHexString(0xFF & in[i]);
            if (hex.length() == 1) 
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static String BitSetToString(BitSet b){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length(); i++)
            sb.append(b.get(i) ?  1: 0);
        return sb.toString();
    }
}
