
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

public class MCLApp {
    /*File path name*/
	//public static String dataFilePath = "attweb_net.txt";  
    //public static String dataFilePath = "facebook1.txt"; 

    public static String dataFilePath2 = "attweb_net2.txt";
    public static String dataFilePath = "attweb_net.txt";
    public static int size = 0;
    public static int dimensions;
    public static int clusters;
    public static int numberKeys = 0;
    public static HashMap<String, Integer> stringToID = new HashMap<String, Integer>();
    public static TreeSet<Integer> tempBuffer = new TreeSet<Integer>();
    public static ArrayList<Integer> tempList = new ArrayList<Integer>();
    /*For the time complexity issue number of entries and nodes is bounded*/
    public static int numberOfEntries = 228;
    public static int numberOfNodes = 180;
    public static int adjMatrix[][] = new int[numberOfNodes][numberOfNodes];
    public static double transMatrix[][] = new double[numberOfNodes][numberOfNodes];

    /*The expansion operator is responsible for allowing
     flow to connect different regions of the graph. */
    public static int power = 2; //expansion power of 2 as default
        /*The inflation parameter affects cluster granularity*/
    public static double inflate = 2; //inflation of 2 (squaring)  

    /*HashMap init*/
    static readWriteHashMap rwHash = new readWriteHashMap();

    public static void convertToGraph(String input) {
        int node1;
        int node2;
        StringTokenizer tk;
        tk = new StringTokenizer(input);
        node1 = Integer.parseInt(tk.nextToken());
        node2 = Integer.parseInt(tk.nextToken());
		//System.out.println(node1);
        //System.out.println(node2);
        adjMatrix[node1][node2] = 1;
        adjMatrix[node2][node1] = 1;
        System.out.println("Edge between: " + node1 + " and: " + node2);

    }

    public static void readData() throws FileNotFoundException, IOException {
        String input;
        FileInputStream file_in;
        BufferedReader data_in;
        String current_line;
        int id = 0;
        BufferedReader inFile = new BufferedReader(new FileReader(dataFilePath));
        try {
            file_in = new FileInputStream("test/" + dataFilePath);
            data_in = new BufferedReader(new InputStreamReader(file_in));

            for (int i = 0; i < numberOfEntries; i++) {
                input = data_in.readLine();
                //System.out.println(input);
                convertToGraph(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while ((current_line = inFile.readLine()) != null) {
            StringTokenizer items;
            items = new StringTokenizer(current_line, "\t");

            items = new StringTokenizer(current_line, " ");

            while (items.hasMoreTokens()) {
                String token = items.nextToken();
                if (!stringToID.containsKey(token)) {
                    stringToID.put(token, id);
                    id++;
                }

                tempBuffer.add(stringToID.get(token));
                tempList.add(stringToID.get(token));
            }

        }
    }

    public static void printMatrix(double[][] matrix) {
        for (int i = 0; i < numberOfNodes; i++) {
            System.out.println();
            for (int j = 0; j < numberOfNodes; j++) {
                System.out.print(matrix[i][j] + " ");
            }
        }
        System.out.println();
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < numberOfNodes; i++) {
            System.out.println();
            for (int j = 0; j < numberOfNodes; j++) {
                System.out.print(matrix[i][j] + " ");
            }
        }
        System.out.println();
    }
    /*Small simple path loops can complicate things.
     – There is a strong effect that odd powers of expansion obtain
     their mass from simple paths of odd length, and likewise for
     even.
     – Adds a dependence to the transition probabilities on the
     parity of the simple path lengths.
      The addition of self looping edges on each node
     resolves this.
     – Adds a small path of length 1, so the mass does not only
     appear during odd powers of the matrix.*/

    public static void addSelfLoop() {
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                adjMatrix[i][i] = 1;
            }
        }
    }

    /*Construct the transition matrix for each node and edge*/
    public static void constructTransitionMatrix() {
        double columnSum[] = new double[numberOfNodes];
        /*column sums to one. Each column has a digit to identify the number of nodes connected to it (degree)*/
        for (int row = 0; row < numberOfNodes; row++) {
            for (int col = 0; col < numberOfNodes; col++) {
                columnSum[col] += adjMatrix[row][col];
            }
        }
        /*Check the number and convert it into probability*/
        for (int row = 0; row < numberOfNodes; row++) {
            for (int col = 0; col < numberOfNodes; col++) {
                transMatrix[row][col] = (double) adjMatrix[row][col] / columnSum[col];
            }
        }
    }
    /*Convergence to a state where probability is steady such that every column value has the same 
     number. Returns a true/false value*/

    public static boolean checkConvergence() {
        double prev = -1;
        /*Use the previous variable (prev) to check if after inflation/expansion if the value of columns are the same 
         if it is not the same, return false and continue to run while loop*/
        for (int j = 0; j < numberOfNodes; j++) {
            for (int i = 0; i < numberOfNodes; i++) {
                if (transMatrix[i][j] != 0) {
                    prev = transMatrix[i][j];
                    break;
                }
            }
            for (int i = 0; i < numberOfNodes; i++) {
                if (transMatrix[i][j] != 0) {
                    if (transMatrix[i][j] != prev) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* Expansion coincides with taking the power of a stochastic matrix using the normal matrix product (i.e. matrix squaring) 
     Expansion corresponds to computing random walks of higher length, which means random walks with many steps.  
     It associates new probabilities with all pairs of nodes, where one node is the point of departure and the other is the destination.  
     Since higher length paths are more common within clusters than between different clusters, the probabilities associated with node pairs  
     lying in the same cluster will, in general, be relatively large as there are many ways of going from one to the other. */
    public static double[][] expand() {
        double[][] matrix = new double[numberOfNodes][numberOfNodes];
        int p = power;
        while (p > 1) {
            for (int i = 0; i < numberOfNodes; i++) {
                for (int j = 0; j < numberOfNodes; j++) {
                    //int sum = 0;                                  
                    for (int k = 0; k < numberOfNodes; k++) {
                        matrix[i][j] += (transMatrix[i][k] * transMatrix[k][j]);
                    }
                    //transMatrix[i][j] = sum;
                }
            }
            p--;
        }
        return matrix;
    }

    /*  The inflation operator is responsible for both
     strengthening and weakening of current.
     (Strengthens strong currents, and weakens already weak currents).
     The inflation parameter, r (in this case; variable inflate), controls the extent of this
     strengthening / weakening. (In the end, this
     influences the granularity of clusters.)*/
    public static void inflate() {
        double[] sum = new double[numberOfNodes];
        /* multiplication of two matrices of the same size can be defined by multiplying the corresponding entries 
         and this is known as the Hadamard product.*/
        for (int j = 0; j < numberOfNodes; j++) {
            for (int i = 0; i < numberOfNodes; i++) {
                sum[j] += Math.pow(transMatrix[i][j], inflate);
            }
        }
        for (int j = 0; j < numberOfNodes; j++) {
            for (int i = 0; i < numberOfNodes; i++) {
                transMatrix[i][j] = Math.pow(transMatrix[i][j], inflate) / sum[j];
            }
        }
    }

    public static void prune() {
        double[][] matrix = new double[numberOfNodes][numberOfNodes];
        double avg, mx;
        int i, j, k;
        for (i = 0; i < numberOfNodes; i++) {
            for (j = 0; j < numberOfNodes; j++) {
                matrix[i][j] = transMatrix[i][j];
            }
        }
        for (j = 0; j < numberOfNodes; j++) {
            avg = 0;
            mx = -1.0;
            for (k = 0; k < numberOfNodes; k++) {
                avg = avg + matrix[k][j];
                mx = max(mx, matrix[k][j]);
            }
            avg = avg / (numberOfNodes - 1);
            double threshold = avg / 4;
            for (i = 0; i < numberOfNodes; i++) {
                if (transMatrix[i][j] < threshold) {
                    transMatrix[i][j] = 0;
                }
            }
        }
        normalise();
    }

    public static void normalise() {
        double csum;
        for (int j = 0; j < numberOfNodes; j++) {
            csum = 0;
            for (int k = 0; k < numberOfNodes; k++) {
                csum = csum + transMatrix[k][j];
            }
            for (int i = 0; i < numberOfNodes; i++) {
                transMatrix[i][j] = transMatrix[i][j] / csum;
            }
        }
    }
 /*Implementation of the Markov clustering algorithm*/
    /*1. Input is an un-directed graph, power parameter e,
     inflation parameter r.
     2. Create the associated matrix
     3. Add self loops to each node 
     4. Normalize the matrix
     5. Expand by taking the 2nd power of the matrix
     6. Inflate by taking inflation of
     the resulting matrix with parameter r which is 2
     7. Repeat steps 5 and 6 until a steady state is reached (convergence).
     8. Interpret resulting matrix to discover clusters.*/

    public static void mcl() throws IOException {
        //Markov Cluster Algorithm
        int iteration = 1;
        System.out.println("Iteration " + iteration);
        transMatrix = expand();
        inflate();
        iteration++;
        normalise();
        while (!checkConvergence()) {
            System.out.println("Iteration " + iteration);
            transMatrix = expand();
            inflate();
            iteration++;
            prune();
        }
		//System.out.println("Convergence Reached. The Matrix is: ");
        //printMatrix(transMatrix);
        System.out.println("The number of clusters are: " + findClusters());
        writeClusters(dataFilePath);
        writeClusters(dataFilePath2);

    }
    public static void writeClusters(String data) throws IOException {

        //Printing the clusters
        ArrayList<Integer> clusterElements = new ArrayList<Integer>();
        TreeSet<String> checkSet = new TreeSet<String>();
        HashMap<String, Integer> fileMap = new HashMap<String, Integer>();
        System.out.println("************* FINAL CLUSTERS ************");

        BufferedWriter writer = new BufferedWriter(new FileWriter(data + ".clu"));

        int clusterNumber = 0;
        for (int i = 0; i < numberOfNodes; i++) {

            for (int j = 0; j < numberOfNodes; j++) {
                double check = transMatrix[i][j];
                if (check != 0) {
                    clusterElements.add(j);
                }
            }

            if (clusterElements.size() > 0) {
                clusterNumber++;
                clusters = 0;
                System.out.print("Cluster Number " + clusterNumber + ":" + "{");
                for (Integer e : clusterElements) {

                    for (String key : stringToID.keySet()) {
                        int value = stringToID.get(key);
                        if (value == e) {
                            if (!checkSet.contains(key)) {
                                System.out.print(key + ",");
                                fileMap.put(key, clusterNumber);
                            }

                            checkSet.add(key);
                            numberKeys++;
                        }
                    }

                }
                System.out.println("}");

            }
            clusters++;
               
            clusterElements.clear();
        }
	
        //Writing to File
        writer.write("*Vertices " + numberOfNodes);
        writer.newLine();

        for (String key : stringToID.keySet()) {

            int value = fileMap.get(key);
            String toWrite = String.valueOf(value);
            writer.write(toWrite);
            writer.newLine();
        }
        //write to hashmap 
        rwHash.writeHashMap(fileMap);

        writer.close();
    }

    public static int findClusters() throws IOException {
        //File f = new File("yeastoutput.txt"); 
        File f = new File("attoutput2.txt");

        if (!f.exists()) {
            f.createNewFile();
        }

        FileOutputStream fs = new FileOutputStream(f);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fs));
        int count = 0;
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                if (transMatrix[i][j] != 0) {
                    count++;
                    break;
                }
            }
            for (int j = 0; j < numberOfNodes; j++) {
                if (transMatrix[i][j] != 0) {
                    out.write(Integer.toString(j) + "\t" + Integer.toString(count) + "\n");
                }
            }
        }
        out.close();
        return count;
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        readData();
        addSelfLoop();
		//printMatrix(adjMatrix);
        //System.out.println("Transition Matrix:");
        constructTransitionMatrix();
        //printMatrix(transMatrix);
        mcl();
        //printMatrix(transMatrix);
    }

}
