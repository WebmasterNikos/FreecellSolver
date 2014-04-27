import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import javax.swing.JOptionPane;


public class Freecell {

	private static final int BREADTH = 0;
	private static final int DEPTH = 1;
	private static final int BEST = 2;
	private static final int ASTAR = 3;

	public static void main(String[] args) {

		if(args.length != 3) {
			JOptionPane.showMessageDialog(null, "Invalid number of arguments.\n", "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Invalid number of arguments.\n");			
		}
		else {
			String algorithm = args[0];
			String input = args[1];
			input.trim();
			String output = args[2];
			output.trim();
			
			//Create the structure that represents the deck of cards and print it for validation
			LinkedList<LinkedList<Card>> deck = readCardsFromInput(input);
			System.out.println("Read from input: \n");
			printDeck(deck);

			System.out.println("\nBeggining search.\n");

			TreeNodeFactory factory = TreeNodeFactory.getInstance(getAlgorithm(algorithm));
			TreeNode root = factory.getRootNode(deck);
			Frontier frontier = Frontier.getInstance();
			long t0 = System.nanoTime();
			Solution solution = frontier.beginSearch(root);
			long elapsedTime = System.nanoTime() - t0;
			double seconds = (double)elapsedTime / 1000000000.0;
			if(solution != null) {
				writeSolutionToFile(solution, output);
				//System.out.println(solution.toString());
				System.out.println("Solution found.");
			}

			System.out.printf("Elapsed time: %.2f seconds\n", seconds);	

		}




	}

	private static int getAlgorithm(String algo){
		int type = BEST;
		if(algo.trim().equalsIgnoreCase("breadth")){
			type = BREADTH;
		} else if(algo.trim().equalsIgnoreCase("depth")){
			type = DEPTH;
		} else if(algo.trim().equalsIgnoreCase("best")){
			type = BEST;
		} else if(algo.trim().equalsIgnoreCase("astar")) {
			type = ASTAR;
		} else {
			type = BEST;
			throw new RuntimeException("Invalid algorithm name used, using best-first instead.\n");
		}
		return type;
	}


	//Read the text file at the given location and return a deck of cards (the cascades of the freecell game)
	private static LinkedList<LinkedList<Card>> readCardsFromInput(String filename){

		LinkedList<LinkedList<Card>> deck = new LinkedList<LinkedList<Card>>();

		try {

			FileInputStream fstream = new FileInputStream(filename);			
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			//each line represents one cascade
			while ((strLine = br.readLine()) != null) {
				LinkedList<Card> cascade = new LinkedList<Card>();
				String[] tokens = strLine.split(" ");
				for(String s : tokens){
					cascade.add(Card.stringToCard(s));
				}
				deck.add(cascade);
			}
			in.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}	

		return deck;
	}


	private static void writeSolutionToFile(Solution solution, String filename){



		BufferedWriter oFile;
		LinkedList<String> lines = solution.getSolutionStrings();
		try {
			oFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-16"));
			for(String line : lines){
				oFile.write(line);
				oFile.newLine();
			}

			oFile.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	private static void printDeck(LinkedList<LinkedList<Card>> deck){
		for(LinkedList<Card> cascade : deck) {
			for(Card c : cascade) {
				System.out.printf(c +" ");
			}
			System.out.println();
		}
	}

}
