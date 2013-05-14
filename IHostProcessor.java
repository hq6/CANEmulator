/**
 * Interface that must be implemented by CAN host processors.
*/
public interface IHostProcessor{
    public void run();

	/* 
     * The method is called by the lower layer (CAN Controller) to inform the
     * upper layer that a message is ready to read. 
     */
    public void messageReady(); 

	public ICANController getController();
    public int GetID();
}
