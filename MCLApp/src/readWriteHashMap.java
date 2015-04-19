
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author reker
 */
public class readWriteHashMap {
    /*Writing and saving hashmap*/
    
    public void writeHashMap(HashMap hashmap) throws IOException {
        //HashMap<String,Boolean> hashmap = new HashMap<String, Boolean>();
        File file = new File("results2.txt");//your file

        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.newLine();
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
        //System.out.println(map.toString());

        String line2 = "";
        while ((line2 = in2.readLine()) != null) {
            String parts2[] = line2.split(",");

            map2.put(parts2[0], parts2[1]);
        }
        in2.close();
        // System.out.println(map.toString());
        compareHashSet(map, map2);
        System.out.println("*******END OF HASHMAP********");
    }//end of readHashMap method

    public void compareHashSet(Map<String, String> map, Map<String, String> map2) {
        HashMap<String, String> hMapNotInMap2 = new HashMap<String, String>(); 
        HashMap<String, String> hMapNotInMap = new HashMap<String, String>();  
        HashMap<String, String> hMapChange = new HashMap<String, String>(); 
  
        /*Checks what map2 has but map does not*/ 
        for (Entry<String, String> entry : map.entrySet()) {
            // Check if the current value is a key in the 2nd map
            if (!map2.containsKey(entry.getKey())) {
                // map2 doesn't have the key for this value. Add key-value in new map.
                hMapNotInMap2.put(entry.getKey(), entry.getValue());
            }  
        }  
         /*Checks what map has but map2 does not*/ 
        for (Entry<String, String> entry : map2.entrySet()) {
            // Check if the current value is a key in the 1st map
            if (!map.containsKey(entry.getKey())) {
                // map doesn't have the key for this value. Add key-value in new map.
                hMapNotInMap.put(entry.getKey(), entry.getValue());
            }   
        }  
   
        
        System.out.println("*****New values in Map ******");
        System.out.println("Ordered by Values,ClusterNumber"); 
        System.out.println(hMapNotInMap2.toString());  
        System.out.println("**************************************");
        System.out.println("*****New values in Map2******");
        System.out.println("Ordered by Values,ClusterNumber");
        System.out.println(hMapNotInMap.toString());    
        System.out.println("**************************************");
        System.out.println("*****New values in Map******");
        System.out.println("Ordered by Values,ClusterNumber");
        System.out.println(hMapChange.toString());   

    }//end of compareHashSet method 
 
}//end of class

