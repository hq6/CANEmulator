import java.util.*;

/**
  * Sample Host Processor which simply records (outputs) all the data that it sees.
  */

public class RecordingHostProcessor implements IHostProcessor{
    ICANController myController;
    int ID;
    public RecordingHostProcessor(int ID){
        myController = new StandardCAN(this);
        this.ID = ID;
    }
    public int GetID(){
        return ID;
    }

    /*
     * Print out raw bytes in Hex format.
     */
    public void messageReady(){
        byte[] data = myController.GetNextMessage();
        String hex = Util.ByteArrayToHexString(data);
        System.out.printf("Node %d received a CAN Frame containing %s.\n", ID, hex);
    }

    /*
     * This host processor does not try to write, so there is no run() method.
     */
    public void run(){ }

    public ICANController getController(){
        return myController;
    }
}
