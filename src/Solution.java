import java.util.LinkedList;


public class Solution {

	private LinkedList<FreecellMove> reversemoves;
	private LinkedList<FreecellMove> moves;

	public Solution(){
		reversemoves = new LinkedList<FreecellMove>();
		moves = new LinkedList<FreecellMove>();
	}

	//Create a solution given a TreeNode that represents a solved Freecell game
	public Solution(TreeNode solved) {
		reversemoves = new LinkedList<FreecellMove>();
		moves = new LinkedList<FreecellMove>();
		extractSolution(solved);
		while(!reversemoves.isEmpty()){
			moves.add(reversemoves.pollLast());
		}
	}

	private void extractSolution(TreeNode solved) {
		boolean rootReached = false;
		TreeNode current = solved;
		while(!rootReached) {
			if(current.getParent() == null) {
				rootReached = true;
				break;
			}else {
				reversemoves.add(current.getLastmove());
				current = current.getParent();
			}			
		}		
	}

	@Override
	public String toString() {
		String solution = "";
		solution += "" +moves.size() +"\n";
		for(FreecellMove m : moves) {
			solution += m.toString() +"\n";
		}

		return solution;
	}

	public LinkedList<String> getSolutionStrings() {
		LinkedList<String> lines = new LinkedList<String>();
		lines.add(Integer.toString(moves.size()));
		for(FreecellMove m : moves) {
			lines.add(m.toString());
		}
		return lines;
	}



}
