import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeSet;
public class MarkovClustering {

	static HashMap<String,Integer> stringToID=new HashMap<String,Integer>();
	static TreeSet<Integer> tempBuffer=new TreeSet<Integer>();
	static int dimensions=0;
	static ArrayList<Integer> tempList=new ArrayList<Integer>();
	static double selfLoopMatrix[][];
	static double initialMatrix[][];
	static int clusters;

	public static void main(String args[]) throws IOException{
		Scanner userInput = new Scanner(System.in);
		String file_name;
		
		//User Interface for Input
		System.out.println("*********** IMPLEMENTATION OF MVC ALGORITHM **********");
		System.out.println("Enter the filename for data set: ");
		file_name=userInput.next();
		
		//Getting data matrix and calculating distances between each
		

		//GetData for String objects
		getDataString(file_name);
	}

	public static void getDataString(String fileName) throws IOException{

		BufferedReader inFile = new BufferedReader(new FileReader(fileName));		
		String current_line;
		int id=0;
		
		while((current_line=inFile.readLine()) != null){			
			StringTokenizer items;
			
			if(fileName.equals("yeast_undirected_metabolic.txt"))			
			items = new StringTokenizer(current_line, "\t");
			else
		    items = new StringTokenizer(current_line, " ");
			
			while(items.hasMoreTokens()){
				String token=items.nextToken();
				if(!stringToID.containsKey(token)){
					stringToID.put(token, id);
					id++;
				}
				
				tempBuffer.add(stringToID.get(token));
				tempList.add(stringToID.get(token));	
			}
			
		}
		
		dimensions=tempBuffer.size();
		
		initialMatrix=new double [dimensions][dimensions];
		Iterator<Integer> temp=tempList.iterator();
		//Initial associated matrix
		while(temp.hasNext())
		{
			
			initialMatrix[temp.next()][temp.next()]=1;
		}
		
		  
		//********************************Self loop matrix***********************************************
		selfLoopMatrix=new double [dimensions][dimensions];
		for(int i=0;i<dimensions;i++){	
			for(int j=0;j<dimensions;j++)	
				selfLoopMatrix[i][j]=initialMatrix[i][j];
		}	
			
		for(int i=0;i<dimensions;i++)
		{
			for(int j=0;j<dimensions;j++)
			{
				if(i==j)
					selfLoopMatrix[i][j]=1;
			}
			
		}
		
		//******************************Normalize the matrix************************************
		double[][] currentMatrix=new double [dimensions][dimensions];
		double check1;
		for(int a=0;a<dimensions;a++)
		{
			check1=0;
			for(int b=0;b<dimensions;b++)
			{
				if(selfLoopMatrix[b][a]==1)
					check1++;
			}
			
			for(int c=0;c<dimensions;c++)
			{
				if(selfLoopMatrix[c][a]==1)
				{
					currentMatrix[c][a]= (double)(1/check1);
					currentMatrix[c][a]=(double)currentMatrix[c][a]*100;
					currentMatrix[c][a]=(double)Math.round(currentMatrix[c][a]);
					currentMatrix[c][a]=(double)currentMatrix[c][a]/100;
				}
			}
			
		}		
		
		//Call function for expansion	
		double[][] lastMatrix=new double[dimensions][dimensions];
		int equalElements=0;
		while(equalElements!=(dimensions*dimensions)){
			
			lastMatrix=currentMatrix;
			currentMatrix=expansion(currentMatrix);			
			currentMatrix=inflation(currentMatrix);
			equalElements=0;
			for(int i=0;i<dimensions;i++){
				
				for(int j=0;j<dimensions;j++){
					
					if(lastMatrix[i][j]==currentMatrix[i][j]){
						equalElements++;
					}
				}
			}			
							
		}
		
		
		
		int numberKeys=0;
		//Printing the clusters
		ArrayList<Integer> clusterElements=new ArrayList<Integer>();
		TreeSet<String> checkSet=new TreeSet<String>();
		HashMap<String,Integer> fileMap=new HashMap<String,Integer>();
		
		System.out.println("************* FINAL CLUSTERS ************");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName+".clu"));
		
		int clusterNumber=0;
		for(int i=0;i<dimensions;i++){
			
			for(int j=0;j<dimensions;j++){
				double check=currentMatrix[i][j];
				if(check!=0)						
					clusterElements.add(j);		
			}
			
			if(clusterElements.size()>0)
			{
				clusterNumber++;
				clusters=0;
				System.out.print("{");
				for(Integer e:clusterElements)
				{	
										
					for(String key:stringToID.keySet()){
						int value=stringToID.get(key);
						if(value==e)							
						{	
							if(!checkSet.contains(key))
							{
								System.out.print(key+",");
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
		//System.out.println("Total number of clusters : "+clusters);
		//Writing to File
		writer.write("*Vertices "+dimensions);
		writer.newLine();
		
		
		for(String key: stringToID.keySet()){
			
			int value=fileMap.get(key);
			String toWrite=String.valueOf(value);
			writer.write(toWrite);
			writer.newLine();
		}
		
		writer.close();
	}	
	
	
	public static double[][] expansion(double[][] currentMatrix)
	{
		
		double tempVal=0.0;
		double[][] expansionMatrix=new double[dimensions][dimensions];
		for(int a=0;a<dimensions;a++)
		{
			for(int b=0;b<dimensions;b++)
			{
				double sum=0;
				for(int c=0;c<dimensions;c++)
				{
					sum+= currentMatrix[a][c]*currentMatrix[c][b];
				}
				tempVal=(double)sum*100;
				tempVal=(double)Math.round(tempVal);
				expansionMatrix[a][b]=(double)tempVal;
			}
		}
		
		return 	expansionMatrix;
	}
	public static double[][] inflation(double[][] currentMatrix)
	{
		double squaresum;
		double squareval;
		double[][] inflationMatrix=new double[dimensions][dimensions];
		for(int a=0;a<dimensions;a++)
		{
		    squaresum=0;
			squareval=0;
			for(int c=0;c<dimensions;c++)
			{
				
					squareval=(currentMatrix[c][a]*currentMatrix[c][a]);
					squaresum+=squareval;
								
			}
			for(int b=0;b<dimensions;b++)
			{
				inflationMatrix[b][a]=(double)(Math.pow(currentMatrix[b][a],2.0)/squaresum);
				inflationMatrix[b][a]=(double)inflationMatrix[b][a]*100;
				inflationMatrix[b][a]=(double)Math.round(inflationMatrix[b][a]);
				inflationMatrix[b][a]=(double)inflationMatrix[b][a]/100;
			}
		}
		
				
		return inflationMatrix;
			
	}
}