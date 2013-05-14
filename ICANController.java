/**
 * This interface specifies the set of controls for the bus to poll for bits,
 * and interface for CAN Host Processors to push and receive messages.
 */
public interface ICANController{
    public Bit getBit(); /* Retrieve a bit from the node. */
    public void putBit(Bit bit); /* Push a bit to the node. */

    /* For Standard CAN, this cannot be more than 8. */
    public void AddMessage(int ID, byte[] bytes);

    /* Retrieve a received message. */
    public byte[] GetNextMessage();
}
