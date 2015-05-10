package MRLMCLAlgorithm;
import MRLMCLAlgorithm.MLRMCLApp;
import java.io.IOException;
import java.util.Scanner;

public class Main { 

      public static void main(String[] args) throws IOException {
        Scanner userInput = new Scanner(System.in); 
        String file_name;
        //User Interface for Input
	System.out.println("================MLR-MCL ALGORITHM FULL================");
	System.out.println("Enter the filename for data set: ");
	file_name=userInput.next();
		
        MLRMCLApp.readData(file_name);
        MLRMCLApp.rmlMcl(2,  5);

        MLRMCLApp.writeClusters(file_name);
    }
}
