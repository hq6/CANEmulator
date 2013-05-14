import java.util.*;


/**
  * Sample Host Processor which simply generates random data and pushes it through CAN.
  */
public class RandomHostProcessor implements IHostProcessor{
    int count;

    int ID;
    ICANController myController;
    Random random;

    public RandomHostProcessor(int ID){
       myController = new StandardCAN(this);
       this.ID  = ID;
       this.count = 3;
       this.random = new Random();
    }
    public int GetID(){
        return ID;
    }

	/* 
     * Grab the next message and discard it.
     */
    public void messageReady(){ 
        myController.GetNextMessage();
    } 

    public void run(){
        if (count > 0){
            byte[] message = new byte[] {1,2,3,4,5};
            random.nextBytes(message);
            myController.AddMessage(ID, message);
            count--;
            System.out.printf("Node %d adding message %s.\n", ID, Util.ByteArrayToHexString(message));
        }
	}
	public ICANController getController(){
		return myController;
	}
}
