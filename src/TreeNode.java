import java.util.LinkedList;
import java.util.TreeSet;

public class TreeNode implements Comparable<TreeNode> {

	public static final int BREADTH = 0;
	public static final int DEPTH = 1;
	public static final int BEST = 2;
	public static final int ASTAR = 3;

	private LinkedList<LinkedList<Card>> freecells;
	private LinkedList<LinkedList<Card>> foundations;
	private LinkedList<LinkedList<Card>> cascades;
	private int h, g, f;
	private TreeNode parent;
	private FreecellMove lastmove;
	private int algorithm; // 0 = breadth-first, 1 = depth-first, 2 = best-first, 3 = Astar
	private int n;			//the size of the freecell game, (n * 4 = number of cards)
	int totalValueOfCards;			// 4 * (n^2+n)/2

	private TreeNodeFactory factory; //will be used as a reference to the TreeNodeFactory singleton

	public TreeNode(){
		super();
	}


	//copy constructor
	//creates a deep copy of this object since not all variables are immutable
	public TreeNode(TreeNode somenode) {

		this.foundations = new LinkedList<LinkedList<Card>>();
		for(LinkedList<Card> foundation : somenode.getFoundations()) {
			LinkedList<Card> copy = new LinkedList<Card>();
			for(Card c : foundation) {
				copy.add(new Card(c));
			}
			this.foundations.add(copy);
		}

		this.freecells = new LinkedList<LinkedList<Card>>();
		for(LinkedList<Card> freecell : somenode.getFreecells()) {
			LinkedList<Card> copy = new LinkedList<Card>();
			for(Card c : freecell) {
				copy.add(new Card(c));
			}
			this.freecells.add(copy);
		}

		this.cascades = new LinkedList<LinkedList<Card>>();
		for(LinkedList<Card> cascade : somenode.getCascades()) {
			LinkedList<Card> copy = new LinkedList<Card>();
			for(Card c : cascade) {
				copy.add(new Card(c));
			}
			this.cascades.add(copy);
		}		

		this.algorithm = somenode.algorithm;
		this.g = somenode.g;
		this.f = somenode.f;
		this.h = somenode.h;
		this.parent = somenode.parent;
		this.lastmove = null;
		this.n = somenode.n;
		this.totalValueOfCards = somenode.totalValueOfCards;

		this.factory = somenode.factory;
	}

	@Override
	public boolean equals(Object obj) {
		TreeNode other = (TreeNode) obj;

		if(!(this.cascades.size() == other.cascades.size())) return false; //if they don't have the same number of cascades
		if(!(this.freecells.size() == other.freecells.size())) return false; //if they don't have the same number of freecells
		if(!(this.foundations.size() == other.foundations.size())) return false; //if they don't have the same number of foundations

		if(this.cascades.size() == other.cascades.size()) {
			for (int i = 0; i < this.cascades.size(); i++) {
				if (!(this.cascades.get(i).size() == other.cascades.get(i).size())) return false; //if the sizes of these cascades are different
				for(int j=0; j<this.cascades.get(i).size(); j++){
					if(!this.cascades.get(i).get(j).equals(other.cascades.get(i).get(j))) return false; //if a card is different 
				}
			}
		}

		if(this.foundations.size() == other.foundations.size()) {
			for (int i = 0; i < this.foundations.size(); i++) {
				if (!(this.foundations.get(i).size() == other.foundations.get(i).size())) return false; //if the sizes of these foundations are different
				for(int j=0; j<this.foundations.get(i).size(); j++){
					if(!this.foundations.get(i).get(j).equals(other.foundations.get(i).get(j))) return false; //if a card is different 
				}
			}
		}

		if(this.freecells.size() == other.freecells.size()) {
			for (int i = 0; i < this.freecells.size(); i++) {
				if (!(this.freecells.get(i).size() == other.freecells.get(i).size())) return false; //if the sizes of these freecells are different
				for(int j=0; j<this.freecells.get(i).size(); j++){
					if(!this.freecells.get(i).get(j).equals(other.freecells.get(i).get(j))) return false; //if a card is different 
				}
			}
		}

		//if no difference has been found so far they're identical
		return true;
	}



	@Override
	public int compareTo(TreeNode other) {
		// when the algorithm is breadth-first we want to exhaust all tree-nodes
		// of depth g before moving to depth g+1
		if (algorithm == BREADTH) {
			if(this.equals(other)) return 0; //if they're identical
			if (this.g > other.g)
				return 1;
			else if (this.g < other.g)
				return -1;
			else {
				//this could be either 1 or -1, it doesn't matter if they're at the same depth
				return 1;
			}
		}
		// when the algorithm is depth-first we want to prioritize nodes that
		// are at a higher depth
		else if (algorithm == DEPTH) {
			if(this.equals(other)) return 0;
			if (this.g > other.g) return -1;
			else if (this.g < other.g) return 1;
			else {
				//this could be either 1 or -1, it doesn't matter if they're at the same depth
				return 1;
			}
		} else if (algorithm == BEST) {
			if(this.equals(other)) return 0;
			if (this.h > other.h) return 1;
			else if (this.h < other.h) return -1;
			else {
				if(this.g < other.g) return -1;
				return 1;
			}
		}

		// if algorithm == ASTAR
		if(this.equals(other)) return 0;
		if (this.f > other.f)
			return 1;
		else if (this.f < other.f)
			return -1;
		else {
			return 1;
		}
	}

	
	
	public void calculateHeuristicValue() {

		int h = 0;		
		
		//penalty for every card not being in a cascade
		//higher penalty for lower cards
		
		int cardsNotInFoundationsPenalty = 0;
		
		for(LinkedList<Card> freecell : freecells) {
			if(!freecell.isEmpty()) {
				cardsNotInFoundationsPenalty += reverseWorthOfCards(freecell);
			}
		}

		for(LinkedList<Card> cascade : cascades) {
			if(!cascade.isEmpty()) {
				cardsNotInFoundationsPenalty += reverseWorthOfCards(cascade);
			}
		}
		
		//Penalty for cards not being in order
		
		int cardOrderPenalty = 0;
		
		for(LinkedList<Card> cascade : cascades) {
			cardOrderPenalty += orderingPenalty(cascade);
		}
		
		
		h = (int) Math.round(0.75 * cardsNotInFoundationsPenalty + 0.25 * cardOrderPenalty);
				
		setH(h);

	}
	
	//returns a number that has to do with how ordered a pile of cards is
	//if the cards are in perfect order this number is 0 (no penalty)
	private int orderingPenalty(LinkedList<Card> cards) {
		int penalty = 0;
		int difference;
		if(cards.size() > 1) {
			for(int i=0; i<cards.size()-1; i++) {
				difference = (cards.get(i).getValue() - cards.get(i+1).getValue());
				if(!(cards.get(i).suitIsCompatible(cards.get(i+1)) && difference == 1)) {
					penalty++;
				}
			}
		}
		return penalty;
	}
	
	private int sumValueOfCards(LinkedList<Card> cards) {
		int sum = 0;
		for(Card card : cards) {
			sum += card.getValue()+1;
		}
		return sum;
	}
	
	private int reverseWorthOfCards(LinkedList<Card> cards) {
		int sum = 0;
		for(Card card : cards) {
			sum += Math.abs((n) - card.getValue());
		}
		return sum;
	}
	

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public LinkedList<LinkedList<Card>> getCascades() {
		return cascades;
	}

	public LinkedList<LinkedList<Card>> getFreecells() {
		return freecells;
	}





	//Check if it is legal for a card to move to a non-empty foundation
	public static boolean moveToFoundationIsLegal(Card card1, Card foundationcard) {

		if (card1.getValue() == (foundationcard.getValue() + 1)) {
			if (card1.getSuit().equalsIgnoreCase(foundationcard.getSuit())) return true;
		}

		return false;
	}

	//calls the findChildren() method to polulate this tree-node's set of children, and returns them
	public TreeSet<TreeNode> extractChildren() {		
		return factory.extractChildren(this);
	}




	@Override
	public String toString() {
		String str = "=================================================\n";
		str += "Freecells: ";
		for(LinkedList<Card> freecell : freecells){
			if(freecell.isEmpty()) str += " ";
			else str += freecell.getLast().toString() +" ";
		}
		str += "\t\tFoundations: ";
		for(LinkedList<Card> foundation : foundations){
			if(foundation.isEmpty()) str += " ";
			else str += foundation.getLast().toString() + "(" +foundation.size() +") ";
		}
		str += "\n\n";
		for(LinkedList<Card> cascade : cascades) {
			if(cascade.isEmpty()) str += "\n";
			else {
				for(Card c : cascade) {
					str += c.toString() +" ";
				}
				str += "\n";
			}
		}
		str += "\nF: " +f +"\tG: " +g +"\tH: " +h +"\n";
		str += "=================================================\n";
		return str;
	}

	public LinkedList<LinkedList<Card>> getFoundations() {
		return foundations;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public FreecellMove getLastmove() {
		return lastmove;
	}

	public void setLastmove(FreecellMove lastmove) {
		this.lastmove = lastmove;
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setFreecells(LinkedList<LinkedList<Card>> freecells) {
		this.freecells = freecells;
	}

	public void setFoundations(LinkedList<LinkedList<Card>> foundations) {
		this.foundations = foundations;
	}

	public void setCascades(LinkedList<LinkedList<Card>> cascades) {
		this.cascades = cascades;
	}


	public void setFactory(TreeNodeFactory factory) {
		this.factory = factory;
	}


	public int getN() {
		return n;
	}


	public void setN(int n) {
		this.n = n;
	}


	public int getTotalValueOfCards() {
		return totalValueOfCards;
	}


	public void setTotalValueOfCards(int totalValueOfCards) {
		this.totalValueOfCards = totalValueOfCards;
	}
	
	
	









}
