import java.util.*;
import java.io.*;

/**
  * Driver class for the CAN Bus. 
  * It loads instances of the classes specified in the file CAN_NODES, and then
  * constantly alternates between pushing and receiving bits among all the CAN
  * Controllers. Note that the bits are essentially simultaneously processed.
  *
  */
public class Main implements Runnable{
    List<IHostProcessor> processors;
    Map<Integer, ICANController> controllers;
    final String CAN_NODES = "CurrentNodes.txt";

    static boolean isRunning;
    
    public void LoadNodes() throws Exception{

        // Construct Host Procsesors by Reflection.
        processors = new ArrayList<IHostProcessor>();
        controllers = new HashMap<Integer, ICANController>();

        Scanner in = new Scanner(new File(CAN_NODES));
        while (in.hasNextLine()){
            String line = in.nextLine();
            if (line.startsWith("#") || line.trim().equals("")) continue;

            String[] args = line.split("\\s+");
            Integer ID = Integer.parseInt(args[0]);
            IHostProcessor p = (IHostProcessor) Class.forName(args[1]).getConstructor(Integer.TYPE).newInstance(ID);
            ICANController c = p.getController();

            processors.add(p);
            controllers.put(ID, c);
        }
    }

    class BitCompare implements Comparator<Bit>{
        public int compare(Bit a, Bit b){
            Map<Bit, Integer> map = new HashMap<Bit,Integer>();
            map.put(Bit.ZERO, 0);
            map.put(Bit.ONE, 1);
            map.put(Bit.IDLE, 2);

            return map.get(a) - map.get(b);
        }
    }

    public void run(){
        int bitCycle = 0;
        BitCompare cp = new BitCompare();
        try {
            LoadNodes();            
        } catch(Exception e){
            System.err.println("Loading nodes failed, exiting!");
            e.printStackTrace();
            System.exit(-1);
        }
        while (isRunning){
            for (IHostProcessor proc : processors) proc.run();

            Bit bit = Bit.IDLE;
            for (ICANController controller : controllers.values()){
                Bit obit = controller.getBit();
                if (cp.compare(obit, bit) < 0) bit = obit;
            }
            for (ICANController controller : controllers.values()) controller.putBit(bit);

            bitCycle++;
        }
    }

    /**
      * Start the polling thread, and terminate when the user presses [Enter].
      */
    public static void main(String[] args) throws Exception{
        isRunning = true;
        (new Thread(new Main())).start();
        System.in.read(); 
        System.out.println("Exiting...");
        isRunning = false;
    }
}
