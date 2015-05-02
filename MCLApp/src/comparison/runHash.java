package comparison;


import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author reker
 */
public class runHash { 
    static readWriteHashMap rwHash = new readWriteHashMap(); 
    public static String dataFilePath = "results.txt"; 
    public static String dataFilePath2 = "results2.txt";  
    public static String dataFilePath3 = "data.txt"; 
    public static void main(String[] args) throws IOException{ 
      rwHash.readHashMap();
      //System.out.println( countLines.countingLines(dataFilePath3)); 
    } 
    
}
