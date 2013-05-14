import java.util.*;

/**
  * Represents a string of Bit enums, and contains convenience methods for
  * converting to various types, as well as extracting "substrings"
  */
public class BitString{
    private List<Bit> myBits;
    public BitString(){
        myBits = new ArrayList<Bit>();
    }
    public BitString(BitSet bits, int length){
        this();
        for (int i = 0; i < length; i++)
            if (bits.get(i)) myBits.add(Bit.ONE);
            else myBits.add(Bit.ZERO);
    }
    private BitString(List<Bit> bits){
        myBits = new ArrayList<Bit>(bits);
    }
    public Bit get(int i){
        return myBits.get(i);
    }
    public int getBit(int i){
        return get(i) == Bit.ZERO ? 0 : 1;
    }
    public void add(Bit bit){
        myBits.add(bit);
    }
    public int length(){
        return myBits.size();
    }
    public int toInt(int fromIndex, int toIndex){
       return get(fromIndex, toIndex).toInt(); 
    }
    public byte[] toByteArray(int fromIndex, int toIndex){
       return get(fromIndex, toIndex).toByteArray(); 
    }
    public BitString get(int fromIndex, int toIndex){
        return new BitString(myBits.subList(fromIndex, toIndex));
    }
    public int toInt(){
        assert myBits.size() <= 32;
        
        int total = 0;
        for (int i = 0; i < myBits.size(); i++)
            if (myBits.get(i) == Bit.ONE) total += Math.pow(2, myBits.size() - 1 - i);
        return total;
    }
    public byte[] toByteArray(){
        byte[] retVal = new byte[(myBits.size() + 7) / 8];
        for (int i = 0; i < retVal.length; i++) retVal[i] = 0;

        int index = -1;
        for (int i = 0; i < myBits.size(); i++){
            if (i % 8 == 0) index++;
            if (myBits.get(i) == Bit.ONE) retVal[index] += Math.pow(2, 7 - i % 8);
        }

        return retVal;
    }
}
