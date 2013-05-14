import java.util.*;

/*
 * This class implements the Standard CAN Protocol as a service to host processors.
 * It interprets the bits on the wire as CAN frames.
 *
 * The current implementation assumes no bit errors, and supports standard CAN
 * ONLY, with no support for extended CAN.
 * It tracks the number of bits expected, which gets updated after the DLC is received.
 * Parse the packet after all bits have been received.
 *
 */
public class StandardCAN implements ICANController{
    private IHostProcessor myProcessor;

    private SenderState senderState;
    private ReceiverState receiverState;


    /* 
     * Hold current messages on the wire, for resend if not acknowledged 
     * Might convert this to Frame. 
     * Need a data structure that tracks its own send status.
     * Functions like a scanner, so we can get the next bit.
     */ 
    boolean isSending;
    boolean busIdle;


    int bitCycle = 0;
    public StandardCAN(IHostProcessor processor){ 
        myProcessor = processor; 
        senderState = new SenderState(); 
        receiverState = new ReceiverState(); 
        /*
         * If we assume the bus is busy at startup, we will simply wait one iteration to learn that the bus is idle.
         */
        isSending = false;
        busIdle = true; 
    }

    public Bit getBit(){
        if (!busIdle && !isSending) return Bit.IDLE;
        if (!busIdle && isSending){
            if (myProcessor.GetID() == 4 && bitCycle == 16){
                System.out.printf("GetNextBit is being invoked because !busIdle and isSending\n");
            }
            return senderState.getNextBit();
        }
        if (busIdle && senderState.currentMessage == null) return Bit.IDLE;
        if (busIdle && senderState.currentMessage != null) {
            if (myProcessor.GetID() == 4 && bitCycle == 16){
                System.out.printf("GetNextBit is being invoked because busIdle and currentMessage != NULL\n");
            }
            return senderState.getNextBit();
        }
        return Bit.IDLE;
    }
    public void putBit(Bit bit){
        bitCycle++;
        if (isSending) // We are currently sending, need to check whether we lost arbitration or not.
            senderHandleBit(bit);
        receiverHandleBit(bit); 
    }

    /** 
     * If I am a sender I care about the following things:
     *  a) Have I lost  arbitration? 
     *       If so, I need to shut up and reset, and toggle into receiver mode.
     *       I also need to pass the bits I have collected thus far and push them into the receiver state.
     *  b) Is the bus idle for me to send?  
     *
     *  Assert that this method should only be called when isSending is true.
     */
    private void senderHandleBit (Bit bit){
        assert senderState.sentBit != null;

        if (senderState.sentBit != bit){ //lost arbitration, if we sent a 1 and got a 0 back.
            // Lost arbitration try to send again the next time the bus is idle
            senderState.reset();
            isSending = false; 
            // What happens next time getBit is called? busIdle should be false,
            // and isSending should be false, so we just return IDLE.
        }
    }

    /**
     * If I received Interframe space following a packet, which means three recessive bits and at least one idle frame, then I can signal a bus_idle to the sender.
     * Once I have finished receiving enough bits to see the DLC, I can compute the remaining expected bits.

     * We need to process messages sent by ourselves in case we lose
     * arbitration, so until we finish receiving the message we are not really
     * sure whether it is ourselves sending or another node which happens to be
     * sending the same prefix of bits.
     */
    private void receiverHandleBit (Bit bit){
        if (bit == Bit.IDLE){
            busIdle = true;
            assert !receiverState.IsMidFrame();
            return;
        }
        busIdle = false;
        boolean completed = receiverState.processBit(bit); //is this the end of a frame?
        if (completed){
            if(!isSending){
                receiverState.accept();
                myProcessor.messageReady();
            }
            else {
                /* 
                 * We have reached the end of a message sent by ourselves, and
                 * should reject it rather than processing it.
                 */
                receiverState.reject(); 
            }
        }
    }

    /**
     * Let the host processor send a message.
     * For Standard CAN, this cannot be more than 8 bytes long. 
     */
    public void AddMessage(int ID, byte[] bytes){
        senderState.offer(ID, bytes);
    }

    /* 
     * Let the host processor retrieve a message after notification. 
     * Such a message will be at maximum 8 bytes. 
     */
    public byte[] GetNextMessage(){
        if (!receiverState.receivedMessages.isEmpty())
            return receiverState.receivedMessages.poll();
        return null;
    }   
    class ReceiverState{
        final int BITS_TO_DLC = 19;

        /* 
         * This does not include the three recessive bits at the end of the
         * frame, and our sender will not send them for now.
         */
        final int BITS_POST_DATA = 25; 

        private BitString bits;
        private boolean seenDLC;
        int expectedBits;
        int bitPosition; 

        SemanticCANFrame completedFrame = null;
        Queue<byte[]> receivedMessages;

        public void accept(){
            receivedMessages.offer(completedFrame.data);
            reset();
        }
        public boolean IsMidFrame(){
            return seenDLC || bitPosition != 0;
        }
        public void reject(){
            completedFrame = null;
            reset();
        }
        public void reset(){
            bits = null;
            expectedBits = BITS_TO_DLC;
            bitPosition = 0;
            seenDLC = false;
            completedFrame = null;
        }
        public ReceiverState(){
            reset();
            receivedMessages = new LinkedList<byte[]>();
        }

        /*
         * Return value: True if this is the last bit in a message.
         */
        public boolean processBit(Bit bit){
            assert bit != Bit.IDLE;
            if (bits == null) bits = new BitString();
            bits.add(bit);
            bitPosition++;
            expectedBits--;


            if (expectedBits == 0){
                if (!seenDLC){
                    int DLC = bits.get(bitPosition - 4, bitPosition).toInt();
                    expectedBits = DLC*8 + BITS_POST_DATA;
                    seenDLC = true;
                }
                else {
                    // TODO: check CRC - if not verify, then drop it. 

                    // Signal upwards to the host controller that we have completed a CAN Frame.
                    completedFrame = SemanticCANFrame.GetInstance(bits); 
                    return true;
                }
            }
            return false;
        }


    }

    class SenderState{
        Bit sentBit;
        private Queue<CANFrame> queuedMessages;
        private CANFrame currentMessage;

        public SenderState(){
            sentBit = null;
            currentMessage = null;
            queuedMessages = new LinkedList<CANFrame>();
        }
        public void offer(int ID, byte[] bytes){
            queuedMessages.offer(new CANFrame(bytes, ID));
            if (senderState.currentMessage == null)
                senderState.currentMessage = queuedMessages.poll(); 
        }
        public void reset(){
            currentMessage.reset();
            sentBit = null;
        }
        public Bit getNextBit(){
            assert currentMessage != null;
            int bit = currentMessage.getNextBit();
            if (bit == -1){ //completed sending current message
                currentMessage  = queuedMessages.poll();
                if (currentMessage == null){
                    isSending = false;
                    sentBit = null;
                }
                return Bit.IDLE;
            }
            else{ 
                sentBit = bit == 1 ? Bit.ONE : Bit.ZERO;
                isSending = true;
                return sentBit;
            }
        }
    }
}
