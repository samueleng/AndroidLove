package comparison;

 
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
public class readWriteHashMap { 
    private final static Logger LOGGER = Logger.getLogger(readWriteHashMap.class.getName()); 
    /*Writing and saving hashmap*/
    
    public void writeHashMap(HashMap hashmap) throws IOException {
        File file = new File("./results/clusterData.txt");//your file
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (Object p : hashmap.keySet()) {
                bw.write(p + "," + hashmap.get(p));
                bw.newLine();
            } 
            bw.flush();
            bw.close();
        }

    }//end of writeHashMap method 
    
    public void readHashMap() throws IOException {
        System.out.println("*******READING HASHMAP********");
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map2 = new HashMap<String, String>();
        BufferedReader in = new BufferedReader(new FileReader("results.txt"));
        BufferedReader in2 = new BufferedReader(new FileReader("results2.txt"));
        String line = "";
        while ((line = in.readLine()) != null) {
            String parts[] = line.split(",");

            map.put(parts[0], parts[1]); 
           
        }
        in.close(); 

        String line2 = "";
        while ((line2 = in2.readLine()) != null) {
            String parts2[] = line2.split(",");

            map2.put(parts2[0], parts2[1]); 
        }
        in2.close();
        compareHashSet(map, map2); 
        compareEntries(map, map2); 
        entries_in_common_or_intersection(map,map2);
        System.out.println("*******END OF HASHMAP********");
    }//end of readHashMap method 
     
    /*Prototype*/
    public void compareHashSet(Map<String, String> beforeMap, Map<String, String> afterMap){
        Set<String> removedKeys = new HashSet<String>(beforeMap.keySet());
        removedKeys.removeAll(afterMap.keySet());

        Set<String> addedKeys = new HashSet<String>(afterMap.keySet());
        addedKeys.removeAll(beforeMap.keySet());

        Set<Entry<String, String>> changedEntries = new HashSet<Entry<String, String>>(
                afterMap.entrySet());
        changedEntries.removeAll(beforeMap.entrySet());
        
        System.out.println("added " + addedKeys);
        System.out.println("removed " + removedKeys);
        System.out.println("changed " + changedEntries);
    }   
     
    /*Print out entries that changed*/
    public void compareEntries(Map<String, String> map1, Map<String, String> map2) throws IOException{ 
        BufferedWriter writer = new BufferedWriter(new FileWriter("./comparisonClusters/" + "compare.txt"));
        
        System.out.println(Maps.difference(map1,map2));  
        writer.write("results: "+Maps.difference(map1,map2)); 
        writer.close();
    }  
     
    /*Print out entries that are common*/
    public void entries_in_common_or_intersection(Map<String, String> map1, Map<String, String> map2) {

    MapDifference<String, String> mapDifference = Maps.difference(
            map1, map2);

    Map<String, String> commonElements = mapDifference.entriesInCommon();

    System.out.println("All common elements: " + commonElements);

    }
}//end of class

