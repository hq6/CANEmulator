/**
  * The three states that the CAN bus can be, at any point in time.
  */
public enum Bit {
    ZERO(0), ONE(1), IDLE(2);

    private final int id;
    Bit(int id) { this.id = id; }
    public int getValue() { return id; }
}
