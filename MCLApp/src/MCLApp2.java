import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class MCLApp2{
	HashMap<String, Integer> nameToIdMapping = new HashMap<String, Integer>();
	ArrayList<Edge> edgeList = new ArrayList<Edge>();

	String file_name =  "attweb_net";
                //"physics_collaboration_net"; // "attweb_net";//"yeast_undirected_metabolic";//
	// "physics_collaboration_net.txt";//"attweb_net.txt";//
	float inflationIndex = 1.3f;
	int expansionIndex = 2;

	int noOfVertices = 0;
	float initialMatrix[][];
	int clusters;

	class Edge {
		int pointX;
		int pointY;

		public int getPointX() {
			return pointX;
		}

		public void setPointX(int pointX) {
			this.pointX = pointX;
		}

		public int getPointY() {
			return pointY;
		}

		public void setPointY(int pointY) {
			this.pointY = pointY;
		}
	}

	public static void main(String args[]) throws IOException {

		MCLApp2 mcl = new MCLApp2();
		mcl.initializeData();
		mcl.applyAlgorithm();

	}

	public void applyAlgorithm() throws IOException {

		float[][] normalizedMatrix = normalizeMatrix();

		float[][] tempMatrix;
		int noOfEqualElements = 0;
		int totalElements = noOfVertices * noOfVertices;

		while (noOfEqualElements != totalElements) {

			tempMatrix = expansion(normalizedMatrix);
			normalizedMatrix = inflation(tempMatrix);

			noOfEqualElements = 0;
			for (int i = 0; i < noOfVertices; i++) {
				for (int j = 0; j < noOfVertices; j++) {
					if (Math.abs(tempMatrix[i][j] - normalizedMatrix[i][j]) <= 1e-200) {
						noOfEqualElements++;
					}
				}
			}

			// for (int i = 0; i < noOfVertices; i++) {
			// for (int j = 0; j < noOfVertices; j++) {
			// System.out.print(normalizedMatrix[i][j] + " ");
			// }
			// System.out.println();
			// }
			System.out.println(noOfEqualElements);
		}

		// for (int i = 0; i < noOfVertices; i++) {
		// for (int j = 0; j < noOfVertices; j++) {
		// // if (normalizedMatrix[i][j] > 0)
		// System.out.print(normalizedMatrix[i][j] + " ");
		// }
		// System.out.println();
		// }

		// Printing the clusters
		ArrayList<Integer> clusterElements = new ArrayList<Integer>();
		TreeSet<String> checkSet = new TreeSet<String>();
		HashMap<String, Integer> fileMap = new HashMap<String, Integer>();

		BufferedWriter writer = new BufferedWriter(new FileWriter("output.clu"));

		int clusterNumber = 0;
		for (int i = 0; i < noOfVertices; i++) {
			clusterElements.clear();

			for (int j = 0; j < noOfVertices; j++) {
				if (normalizedMatrix[i][j] != 0)
					clusterElements.add(j);
			}

			if (clusterElements.size() > 0) {
				clusterNumber++;
				clusters = 0;

				System.out.print(clusterElements.size() + " {");

				for (Integer e : clusterElements) {
					for (String key : nameToIdMapping.keySet()) {
						int value = nameToIdMapping.get(key);
						if (value == e) {
							if (!checkSet.contains(key)) {
								System.out.print(key + ",");
								fileMap.put(key, clusterNumber);
							}

							checkSet.add(key);
						}
					}

				}
				System.out.println("}");
			}
			clusters++;
		}

		writer.write("*Vertices " + noOfVertices);
		writer.newLine();

		BufferedReader inFile = new BufferedReader(new FileReader(file_name
				+ ".net"));
		inFile.readLine();
		for (int i = 0; i < noOfVertices; i++) {
			String s = inFile.readLine();
			int index1 = s.indexOf("\"");
			int index2 = s.indexOf("\"", index1 + 1);
			String key = s.substring(index1 + 1, index2);
			int value = fileMap.get(key);
			String toWrite = String.valueOf(value);
			writer.write(toWrite);
			writer.newLine();
		}

		writer.close();
		inFile.close();
	}

	public float[][] inflation(float[][] currentMatrix) {
		float sumOfSquare;

		float[][] inflationMatrix = new float[noOfVertices][noOfVertices];

		for (int i = 0; i < noOfVertices; i++) {
			sumOfSquare = 0;
			for (int j = 0; j < noOfVertices; j++) {
				sumOfSquare += Math.pow(currentMatrix[j][i], inflationIndex);
			}

			for (int j = 0; j < noOfVertices; j++) {
				inflationMatrix[j][i] = roundOf((float) Math.pow(
						currentMatrix[j][i], inflationIndex) / sumOfSquare);
			}
		}

		for (int i = 0; i < noOfVertices; i++) {
			for (int j = 0; j < noOfVertices; j++) {
				if (inflationMatrix[i][j] <= 1e-200) {
					inflationMatrix[i][j] = 0;
				}
			}
		}
		return inflationMatrix;
	}

	public float[][] expansion(float[][] currentMatrix) {

		float[][] expansionMatrix = new float[noOfVertices][noOfVertices];
		
		for (int expIndex = 1; expIndex < expansionIndex; expIndex++) {
			for (int i = 0; i < noOfVertices; i++) {
				for (int j = 0; j < noOfVertices; j++) {
					float sum = 0;
					for (int k = 0; k < noOfVertices; k++) {
						sum += currentMatrix[i][k] * currentMatrix[k][j];
					}
					expansionMatrix[i][j] = roundOf(sum);
				}
			}
			currentMatrix = expansionMatrix;
		}

		return expansionMatrix;
	}

	public float roundOf(float value) {
		BigDecimal bdc = new BigDecimal(value);
		BigDecimal rounded = bdc.setScale(2, BigDecimal.ROUND_HALF_UP);
		return rounded.floatValue();
	}

	public float[][] normalizeMatrix() {
		noOfVertices = nameToIdMapping.size();

		initialMatrix = new float[noOfVertices][noOfVertices];

		for (Edge edge : edgeList) {
			initialMatrix[edge.getPointX()][edge.getPointY()] = 1;
			initialMatrix[edge.getPointY()][edge.getPointX()] = 1;
		}

		for (int i = 0; i < noOfVertices; i++) {
			initialMatrix[i][i] = 1;
		}

		float[][] normalizedMatrix = new float[noOfVertices][noOfVertices];
		int colCount;

		for (int i = 0; i < noOfVertices; i++) {
			colCount = 0;

			for (int j = 0; j < noOfVertices; j++) {
				if (initialMatrix[j][i] == 1)
					colCount++;
			}

			for (int j = 0; j < noOfVertices; j++) {
				if (initialMatrix[j][i] == 1) {
					normalizedMatrix[j][i] = roundOf(1 / (float) colCount);
				}
			}
		}

		return normalizedMatrix;
	}

	private void initializeData() throws IOException {
		BufferedReader inFile = new BufferedReader(new FileReader(file_name
				+ ".txt"));
		String inputLine;
		String tokenX, tokenY;
		int id = 0;

		while ((inputLine = inFile.readLine()) != null) {
			StringTokenizer tokens;

			if (file_name.equals("attweb_net"))
				tokens = new StringTokenizer(inputLine, "\t");
			else
				tokens = new StringTokenizer(inputLine, " ");

			tokenX = tokens.nextToken();
			tokenY = tokens.nextToken();

			if (!nameToIdMapping.containsKey(tokenX)) {
				nameToIdMapping.put(tokenX, id);
				id++;
			}

			if (!nameToIdMapping.containsKey(tokenY)) {
				nameToIdMapping.put(tokenY, id);
				id++;
			}

			Edge edge = new Edge();
			edge.setPointX(nameToIdMapping.get(tokenX));
			edge.setPointY(nameToIdMapping.get(tokenY));
			edgeList.add(edge);
		}

		inFile.close();
	}
}