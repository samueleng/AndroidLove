package MCLAlgorithm; 

import java.io.IOException;
import java.util.Scanner;

public class Main { 
      public static void main(String[] args) throws IOException {
        Scanner userInput = new Scanner(System.in); 
        String file_name;
        //User Interface for Input
	System.out.println("***** MCL ALGORITHM FULL *****");
	System.out.println("Enter the filename for data set: ");
	file_name=userInput.next();
		
        MCLApp.readData(file_name);
        MCLApp.addSelfLoop();
        MCLApp.constructTransitionMatrix();
        MCLApp.mcl();
    }
}
