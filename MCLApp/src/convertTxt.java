
import java.io.BufferedWriter;
 import java.util.Scanner;
   import java.io.File;
   import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

   public class convertTxt {

     private static void readFile(String fileName) {
       try {
         Scanner scanner = new Scanner(new File(fileName));
         scanner.useDelimiter(System.getProperty("line.separator")); 
         while (scanner.hasNext()) {
           parseLine(scanner.next());
         }
         scanner.close();
       } catch (FileNotFoundException e) {
         e.printStackTrace();
       }
     }

     private static void parseLine(String line) {
       Scanner lineScanner = new Scanner(line);
      lineScanner.useDelimiter("\\s*,\\s*");
       String name = lineScanner.next();
       int age = lineScanner.nextInt();
       boolean isCertified = lineScanner.nextBoolean();
       System.out.println("It is " + isCertified +
         " that " + name + ", age "
         + age + ", is certified.");
     }

public static String readFileAsString(String fileName) throws java.io.IOException {
    java.io.InputStream is = new java.io.FileInputStream(fileName);
    try {
        final int bufsize = 4096;
        int available = is.available();
        byte data[] = new byte[available < bufsize ? bufsize : available];
        int used = 0;
        while (true) {
            if (data.length - used < bufsize) {
                byte newData[] = new byte[data.length << 1];
                System.arraycopy(data, 0, newData, 0, used);
                data = newData;
            }
            int got = is.read(data, used, data.length - used);
            if (got <= 0)
                break;
            used += got;
        }
        return new String(data, 0, used);
    } finally {
        is.close();
    }
}
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(new FileReader("test/twitter.txt"));
        String text = readFileAsString("test/twitter.txt");
        text = text.replace("\n", "").replace("\r", "");
            
} 
   }