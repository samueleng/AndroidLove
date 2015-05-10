package MRLMCLAlgorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class MLRMCLApp {

    /*File Name*/
    public static String FileName;
    /*Matrix variables*/
    public static int size = 0;
    public static int dimensions;

    /*Building Matrix*/
    public static int clusters;
    public static int numberKeys = 0;
    public static HashMap<String, Integer> stringToID = new HashMap<String, Integer>();
    public static TreeSet<Integer> tempBuffer = new TreeSet<Integer>();
    public static ArrayList<Integer> tempList = new ArrayList<Integer>();
    public static int numberOfEdges;

    /*Build adjacent matrix*/
    public static Matrix<IntContainer> adjMatrix;
    /*Build transitive matrix*/
    public static Matrix<DoubleContainer> transMatrix;
    public static Matrix<DoubleContainer> canonicalTransMatrix;
    public static Matrix<DoubleContainer> conv;

    /*The expansion operator is responsible for allowing
     flow to connect different regions of the graph. */
    public static int power = 2; //expansion power of 2 as default
    /*The inflation parameter affects cluster granularity*/
    public static double inflate = 2; //inflation of 2 (squaring)  

    /*HashMap init*/
    static readWriteHashMap rwHash = new readWriteHashMap();

    /*Functions*/
    /*convert Hashmap to adjacent matrix*/
    public static void convertToGraph(String input, HashMap<String, Integer> idMap) {
        int node1;
        int node2;
        StringTokenizer tk; 
        
        /*This tk is for those files with comma delimiter*/
        tk = new StringTokenizer(input, ",");
        /*This tk is for those files with tab delimiter*/
        //tk = new StringTokenizer(input);

        node1 = idMap.get(tk.nextToken());
        node2 = idMap.get(tk.nextToken());

        /*Assign corresponding role/column*/
        adjMatrix.get(node1).c[node2] = 1;
        adjMatrix.get(node2).c[node1] = 1;
        System.out.println("Edge between: " + node1 + " and: " + node2);
    }

    public static void readData(String fileName) throws FileNotFoundException, IOException {
        String input;
        String current_line;
        int id = 0;
        FileName = fileName;
        numberOfEdges = 0;
        boolean empty = true;
        /*Read datasets from the folder (nodes)*/
        try (BufferedReader inFile = new BufferedReader(new FileReader("./nodes/" + FileName))) {
            while ((current_line = inFile.readLine()) != null) {
                empty = false;
                numberOfEdges++;
                StringTokenizer items;

                /*This tk is for those files with tab delimiter*/
                //items = new StringTokenizer(current_line, " "); 
                /*This tk is for those files with comma delimiter*/
                items = new StringTokenizer(current_line, ",");

                /*assign value/key in hashmap*/
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
        /*empty file*/
        if (empty) {
            numberOfEdges = 1;
        }
        /*calculate number of nodes*/
        dimensions = tempBuffer.size();

        System.out.println("Number of edges: " + numberOfEdges);
        System.out.println("Number of entries: " + dimensions);
        /*Container (memory issues)*/
        adjMatrix = new Matrix<>(IntContainer.class, dimensions, dimensions, i -> new IntContainer(i));
        try (BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream("./nodes/" + FileName)))) {
            for (int i = 0; i < numberOfEdges; i++) {
                input = data_in.readLine();
                /*get to matrix work*/
                convertToGraph(input, stringToID);
            }
        }
        try {
            FileOutputStream fout = new FileOutputStream("g");
            PrintStream out = new PrintStream(fout);
            out.println(dimensions + " " + numberOfEdges);
            for (int i = 0; i < dimensions; i++) {
                for (int j = 0; j < dimensions; j++) {
                    out.print(adjMatrix.get(i).c[j] + " ");
                }
                out.println("");
            }
        } catch (Exception e) {

        }
    } 
    
    /*For validating the matrix*/
    public static void printMatrix(Matrix<DoubleContainer> matrix) {
        PrintStream out = null;
        try {
            FileOutputStream fout = new FileOutputStream("matrix");
            out = new PrintStream(fout);
        } catch (Exception e) {

        }

        for (int i = 0; i < dimensions; i++) {
            out.println();
            for (int j = 0; j < dimensions; j++) {
                out.print(matrix.get(i).c[j] + " ");
            }
        }
        out.println();
    }

    public static void printIntMatrix(Matrix<IntContainer> matrix) {
        for (int i = 0; i < dimensions; i++) {
            System.out.println();
            for (int j = 0; j < dimensions; j++) {
                System.out.print(matrix.get(i).c[j] + " ");
            }
        }
        System.out.println();
    }

    /*Small simple path loops can complicate things.
     There is a strong effect that odd powers of expansion obtain
     their mass from simple paths of odd length, and likewise for
     even.
     Adds a dependence to the transition probabilities on the
     parity of the simple path lengths.
     The addition of self looping edges on each node
     resolves this.
     Adds a small path of length 1, so the mass does not only
     appear during odd powers of the matrix. 
     */
    public static void addSelfLoop() {
        for (int i = 0; i < dimensions; i++) {
            for (int j = 0; j < dimensions; j++) {
                adjMatrix.get(i).c[i] = 1;
            }
        }
    }

    /*Construct the transition matrix for each node and edge*/
    public static void constructTransitionMatrix() {
        double columnSum[] = new double[dimensions];

        transMatrix = new Matrix<>(DoubleContainer.class, dimensions, dimensions, d -> new DoubleContainer(d));

        /*column sums to one. Each column has a digit to identify the number of nodes connected to it (degree)*/
        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {
                columnSum[col] += adjMatrix.get(row).c[col];
            }
        }
        /*Check the number and convert it into probability*/
        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {
                transMatrix.get(row).c[col] = (double) adjMatrix.get(row).c[col] / columnSum[col];
            }
        }
    }
    /*Convergence to a state where probability is steady such that every column value has the same 
     number. Returns a true/false value*/
    public static boolean isConvergence() {
        if (conv == null) {
            return false;
        }
        int i = 0, j = 0;
        for (j = 0; j < dimensions; j++) {
            for (i = 0; i < dimensions; i++) {
                if (Math.abs(transMatrix.get(i).c[j] - conv.get(i).c[j]) > 1e-6) {
                    break;
                }
            }
            if (i < dimensions) {
                break;
            }
        }
        if (j == dimensions) {
            return true;
        }
        System.out.println(" **** " + i + " " + j + " ****" + transMatrix.get(i).c[j] + " " + conv.get(i).c[j]);
        for (j = 0; j < dimensions; j++) {
            for (i = 0; i < dimensions; i++) {
                conv.get(i).c[j] = transMatrix.get(i).c[j];
            }
        }
        return false;
    }

   

    /* Expansion coincides with taking the power of a stochastic matrix using the normal matrix product (i.e. matrix squaring) 
     Expansion corresponds to computing random walks of higher length, which means random walks with many steps.  
     It associates new probabilities with all pairs of nodes, where one node is the point of departure and the other is the destination.  
     Since higher length paths are more common within clusters than between different clusters, the probabilities associated with node pairs  
     lying in the same cluster will, in general, be relatively large as there are many ways of going from one to the other. */
    public static Matrix<DoubleContainer> expand() {
        Matrix<DoubleContainer> matrix = new Matrix<>(DoubleContainer.class, dimensions, dimensions, d -> new DoubleContainer(d));
        int p = power;
        while (p > 1) {
            for (int i = 0; i < dimensions; i++) {
                for (int j = 0; j < dimensions; j++) {
                    for (int k = 0; k < dimensions; k++) {
                        matrix.get(i).c[j] += transMatrix.get(i).c[k] * transMatrix.get(k).c[j];
                    }
                }
            }
            p--;
        }
        return matrix;
    }

    /*The inflation operator is responsible for both strengthening and weakening of current.
     (Strengthens strong currents, and weakens already weak currents).
     The inflation parameter, r (in this case; variable inflate), controls the extent of this
     strengthening / weakening. (In the end, this influences the granularity of clusters.)*/
    public static void inflate() {
        double[] sum = new double[dimensions];
        /* multiplication of two matrices of the same size can be defined by multiplying the corresponding entries 
         and this is known as the Hadamard product.*/
        for (int j = 0; j < dimensions; j++) {
            for (int i = 0; i < dimensions; i++) {
                sum[j] += Math.pow(transMatrix.get(i).c[j], inflate);
            }
        }
        for (int j = 0; j < dimensions; j++) {
            for (int i = 0; i < dimensions; i++) {
                transMatrix.get(i).c[j] = Math.pow(transMatrix.get(i).c[j], inflate) / sum[j];
            }
        }
    }

    /*Reduces iteration, improves speed: The threshold is one fourth of the average of all the entries in a column. 
     Any value less than that (0.25) will be set to 0 (Assuming they will reach there anyway*/
    public static void prune() {
        for (int j = 0; j < dimensions; j++) {
            double avg;
            //double mx;
            avg = 0;
            //mx = -1.0;
            for (int k = 0; k < dimensions; k++) {
                avg = avg + transMatrix.get(k).c[j];
                //mx = max(mx, transMatrix.get(k).c[j]);
            }
            avg = avg / dimensions;
            double threshold = avg / 4;
            for (int i = 0; i < dimensions; i++) {
                if (transMatrix.get(i).c[j] < threshold) {
                    transMatrix.get(i).c[j] = 0;
                }
            }
        }
        /*after pruning, renormalise the matrix*/
        normalise();
    }

    /*Normalise the transitive matrix*/
    public static void normalise() {
        double csum;
        for (int j = 0; j < dimensions; j++) {
            csum = 0;
            for (int k = 0; k < dimensions; k++) {
                csum = csum + transMatrix.get(k).c[j];
            }
            for (int i = 0; i < dimensions; i++) {
                transMatrix.get(i).c[j] = transMatrix.get(i).c[j] / csum;
            }
        }
    }

    public static Matrix<DoubleContainer> regularize() {
        Matrix<DoubleContainer> matrix = new Matrix<>(DoubleContainer.class, dimensions, dimensions, d -> new DoubleContainer(d));
        for (int i = 0; i < dimensions; i++) {
            for (int j = 0; j < dimensions; j++) {
                matrix.get(i).c[j] = 0;
                for (int k = 0; k < dimensions; k++) {
                    matrix.get(i).c[j] += transMatrix.get(i).c[k] * canonicalTransMatrix.get(k).c[j];
                }
            }
        }
        return matrix;
    }

    public static void cRmlMcl(int cLevel) throws IOException {
        if (cLevel > 0) {
            int oldDim = dimensions;
            Matrix<IntContainer> oldAdj = adjMatrix;
            List<Integer> match = maximalMatching();
            List<Integer> cmap = new ArrayList<Integer>();
            for (int i = 0; i < oldDim; i++) {
                cmap.add(null);
            }
            dimensions = 0;
            for (int i = 0; i < oldDim; i++) {
                if (cmap.get(i) != null) {
                    continue;
                }
                if (match.get(i) == null) {
                    cmap.add(i, dimensions);
                } else {
                    cmap.add(i, dimensions);
                    cmap.add(match.get(i), dimensions);
                }
                dimensions++;
            }
            Integer[] nodemap1 = new Integer[dimensions];
            Integer[] nodemap2 = new Integer[dimensions];
            for (int i = 0; i < oldDim; i++) {
                int j = cmap.get(i);
                if (nodemap1[j] == null) {
                    nodemap1[j] = nodemap2[j] = i;
                } else {
                    nodemap2[j] = i;
                }
            }

            adjMatrix = new Matrix<>(IntContainer.class, dimensions, dimensions, i -> new IntContainer(i));
            for (int i = 0; i < dimensions; i++) {
                for (int j = i + 1; j < dimensions; j++) {
                    int a1 = nodemap1[i];
                    int a2 = nodemap2[i];
                    int b1 = nodemap1[j];
                    int b2 = nodemap2[j];
                    adjMatrix.get(i).c[j] = 0;
                    if (oldAdj.get(a1).c[b1] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a1).c[b1];
                        //oldAdj.get(a1).c[b1] = 0;
                    }
                    if (b1 != b2 && oldAdj.get(a1).c[b2] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a1).c[b2];
                        //oldAdj.get(a1).c[b2] = 0;
                    }
                    if (a1 != a2 && oldAdj.get(a2).c[b1] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a2).c[b1];
                        //oldAdj.get(a2).c[b1] = 0;
                    }
                    if (a1 != a2 && b1 != b2 && oldAdj.get(a2).c[b2] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a2).c[b2];
                        //oldAdj.get(a2).c[b2] = 0;
                    }
                    adjMatrix.get(j).c[i] = adjMatrix.get(i).c[j];
                }
            }
            cRmlMcl(cLevel - 1);
            Matrix<DoubleContainer> matrix = new Matrix<>(DoubleContainer.class, oldDim, oldDim, d -> new DoubleContainer(d));
            for (int i = 0; i < dimensions; i++) {
                for (int j = 0; j < dimensions; j++) {
                    if (transMatrix.get(i).c[j] != 0) {
                        matrix.get(nodemap1[i]).c[nodemap1[j]] = transMatrix.get(i).c[j];
                        matrix.get(nodemap1[i]).c[nodemap2[j]] = transMatrix.get(i).c[j];
                        if (nodemap1[i] == nodemap2[i]) {
                            continue;
                        }
                        matrix.get(nodemap2[i]).c[nodemap1[j]] = 0;
                        matrix.get(nodemap2[i]).c[nodemap2[j]] = 0;
                    }
                }
            }
            dimensions = oldDim;
            adjMatrix = oldAdj;
            transMatrix = matrix;
            normalise();
        }
        cRMcl();
        return;
    }

    public static void rmlMcl(int r, int cLevel) throws IOException {
        inflate = r;
        if (cLevel > 0) {
            int oldDim = dimensions;
            Matrix<IntContainer> oldAdj = adjMatrix;
            List<Integer> match = maximalMatching();
            List<Integer> cmap = new ArrayList<Integer>();
            for (int i = 0; i < oldDim; i++) {
                cmap.add(null);
            }
            dimensions = 0;
            for (int i = 0; i < oldDim; i++) {
                if (cmap.get(i) != null) {
                    continue;
                }
                if (match.get(i) == null) {
                    cmap.add(i, dimensions);
                } else {
                    cmap.add(i, dimensions);
                    cmap.add(match.get(i), dimensions);
                }
                dimensions++;
            }
            Integer[] nodemap1 = new Integer[dimensions];
            Integer[] nodemap2 = new Integer[dimensions];
            for (int i = 0; i < oldDim; i++) {
                int j = cmap.get(i);
                if (nodemap1[j] == null) {
                    nodemap1[j] = nodemap2[j] = i;
                } else {
                    nodemap2[j] = i;
                }
            }

            adjMatrix = new Matrix<>(IntContainer.class, dimensions, dimensions, i -> new IntContainer(i));
            for (int i = 0; i < dimensions; i++) {
                for (int j = i + 1; j < dimensions; j++) {
                    int a1 = nodemap1[i];
                    int a2 = nodemap2[i];
                    int b1 = nodemap1[j];
                    int b2 = nodemap2[j];
                    adjMatrix.get(i).c[j] = 0;
                    if (oldAdj.get(a1).c[b1] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a1).c[b1];
                        //oldAdj.get(a1).c[b1] = 0;
                    }
                    if (b1 != b2 && oldAdj.get(a1).c[b2] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a1).c[b2];
                        //oldAdj.get(a1).c[b2] = 0;
                    }
                    if (a1 != a2 && oldAdj.get(a2).c[b1] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a2).c[b1];
                        //oldAdj.get(a2).c[b1] = 0;
                    }
                    if (a1 != a2 && b1 != b2 && oldAdj.get(a2).c[b2] > 0) {
                        adjMatrix.get(i).c[j] += oldAdj.get(a2).c[b2];
                        //oldAdj.get(a2).c[b2] = 0;
                    }
                    adjMatrix.get(j).c[i] = adjMatrix.get(i).c[j];
                }
            }
            cRmlMcl(cLevel - 1);
            Matrix<DoubleContainer> matrix = new Matrix<>(DoubleContainer.class, oldDim, oldDim, d -> new DoubleContainer(d));
            for (int i = 0; i < dimensions; i++) {
                for (int j = 0; j < dimensions; j++) {
                    if (transMatrix.get(i).c[j] != 0) {
                        matrix.get(nodemap1[i]).c[nodemap1[j]] = transMatrix.get(i).c[j];
                        matrix.get(nodemap1[i]).c[nodemap2[j]] = transMatrix.get(i).c[j];
                        if (nodemap1[i] == nodemap2[i]) {
                            continue;
                        }
                        matrix.get(nodemap2[i]).c[nodemap1[j]] = 0;
                        matrix.get(nodemap2[i]).c[nodemap2[j]] = 0;
                    }
                }
            }
            dimensions = oldDim;
            adjMatrix = oldAdj;
            transMatrix = matrix;
            normalise();
        }
        rMcl();
        return;
    }

    private static List<Integer> maximalMatching() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < dimensions; i++) {
            result.add(null);
        }
        for (int i = 0; i < dimensions; i++) {
            if (result.get(i) != null) {
                continue;
            }
            int j;
            for (j = i + 1; j < dimensions; j++) {
                if (result.get(j) != null) {
                    continue;
                }
                if (adjMatrix.get(i).c[j] == 1) {
                    break;
                }
            }
            if (j < dimensions) {
                result.add(i, j);
                result.add(j, i);
            }
        }
        return result;
    }

    public static void cRMcl() throws IOException {
        addSelfLoop();
        constructTransitionMatrix();
        canonicalTransMatrix = transMatrix;

        int iteration = 1;
        long t1 = System.currentTimeMillis();
        System.out.println("Iteration " + iteration);
        transMatrix = regularize();
        inflate();
        iteration++;
        prune();
        //printMatrix(conv);printMatrix(transMatrix);printMatrix(canonicalTransMatrix);
        for (int i = 0; i < 0; i++) {
            System.out.println("Iteration " + iteration);
            transMatrix = regularize();
            inflate();
            iteration++;
            prune();
            if (iteration % 100 == 0) {
                printMatrix(transMatrix);
            }
        };
        long t2 = System.currentTimeMillis() - t1;
        System.out.print("Finish convergence in: ");
        System.out.println((t2) + "ms");
        System.out.println("The number of clusters are: " + findClusters());
        //writeClusters(FileName);
    }

    public static void rMcl() throws IOException {
        conv = transMatrix;
        addSelfLoop();
        constructTransitionMatrix();
        canonicalTransMatrix = transMatrix;
        if (conv != null) {
            canonicalTransMatrix = conv;
        } else {
            conv = transMatrix;
        }
        //printMatrix(canonicalTransMatrix);
        //printMatrix(transMatrix);
        //printIntMatrix(adjMatrix);
        //System.out.println(canonicalTransMatrix + " " + transMatrix + " " + conv + " " + adjMatrix);
        //System.out.println(dimensions + " " + inflate + " " + power);
        //System.exit(0);
        int iteration = 1;
        long t1 = System.currentTimeMillis();
        System.out.println("Iteration " + iteration);
        transMatrix = regularize();
        printMatrix(transMatrix);
        inflate();
        iteration++;
        prune(); 
        
        while (!isConvergence()) {
            System.out.println("Iteration " + iteration);
            transMatrix = regularize();
            inflate();
            iteration++;
            prune();
            if (iteration % 100 == 0) {
                printMatrix(transMatrix);
            }
        };
        long t2 = System.currentTimeMillis() - t1;
        System.out.print("Finish convergence in: ");
        System.out.println((t2) + "ms");
        System.out.println("The number of clusters are: " + findClusters());
        //writeClusters(FileName);
    }


    /*This function prints cluster results and write a hashmap to a file*/
    public static void writeClusters(String data) throws IOException {
        /*Printing the clusters*/
        ArrayList<Integer> clusterElements = new ArrayList<Integer>();
        TreeSet<String> checkSet = new TreeSet<String>();
        HashMap<String, Integer> fileMap = new HashMap<String, Integer>();
        System.out.println("************* Cluster Results ************");

        BufferedWriter writer = new BufferedWriter(new FileWriter(data + ".clu"));

        int clusterNumber = 0;
        for (int i = 0; i < dimensions; i++) {

            for (int j = 0; j < dimensions; j++) {
                double check = transMatrix.get(i).c[j];
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

        /*Writing to File*/
        writer.write("*Vertices " + dimensions);
        writer.newLine();

        for (String key : stringToID.keySet()) {

            int value = fileMap.get(key);
            String toWrite = String.valueOf(value);
            writer.write(toWrite);
            writer.newLine();
        }
        /*write to hashmap*/
        rwHash.writeHashMap(fileMap);
        writer.close();
    }

    /*find the clusters*/
    public static int findClusters() throws IOException {
        /*Intepret the file from /nodes/ClusterResults/*/
        File f = new File("./nodes/ClusterResults/" + "clusterResults.txt");
        if (!f.exists()) {
            f.createNewFile();
        }

        FileOutputStream fs = new FileOutputStream(f);
        int count;
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fs))) {
            count = 0;
            for (int i = 0; i < dimensions; i++) {
                for (int j = 0; j < dimensions; j++) {
                    if (transMatrix.get(i).c[j] != 0) {
                        count++;
                        break;
                    }
                }
                for (int j = 0; j < dimensions; j++) {
                    if (transMatrix.get(i).c[j] != 0) {
                        out.write(Integer.toString(j) + "\t" + Integer.toString(count) + "\n");
                    }
                }
            }
        }
        return count;
    }
}
