import java.util.LinkedList;
import java.util.TreeSet;

public class TreeNodeFactory {

	public static final int BREADTH = 0;
	public static final int DEPTH = 1;
	public static final int BEST = 2;
	public static final int ASTAR = 3;

	private static int algorithm;

	//make this class a singleton
	private static TreeNodeFactory INSTANCE = null;

	private TreeNodeFactory() {
		//private contructor
	}


	public static TreeNodeFactory getInstance(int type) {
		if(INSTANCE == null) {
			INSTANCE = new TreeNodeFactory();
		}
		algorithm = type;
		return INSTANCE;
	}


	//given a deck of cards creates and returns a root node for the frontier
	//the type of algorithm has to be set beforehand (when creating the TreeNodeFactory)
	public TreeNode getRootNode(LinkedList<LinkedList<Card>> deck){
		TreeNode root = new TreeNode();
		root.setAlgorithm(algorithm);
		root.setCascades(deck);
		root.setFoundations(new LinkedList<LinkedList<Card>>());
		root.setFreecells(new LinkedList<LinkedList<Card>>());
		root.setG(0);
		if (algorithm == BEST || algorithm == ASTAR) {
			root.calculateHeuristicValue();
			if(algorithm == ASTAR) root.setF(root.getH() + root.getG());
		}

		root.setParent(null);
		root.setLastmove(null);

		// create 4 foundations and 4 freecells
		for (int i = 0; i < 4; i++) {
			root.getFoundations().add(new LinkedList<Card>());
			root.getFreecells().add(new LinkedList<Card>());
		}

		root.setFactory(getInstance(algorithm));

		//count the cards
		int numCards = 0;
		for(LinkedList<Card> cascade : deck) {
			numCards += cascade.size();
		}
		int n = numCards/4;
		root.setN(numCards/4);
		root.setTotalValueOfCards(4 * ((n*n+n)/2));
		return root;
	}

	// used to create and return the child that is the result of executing the
	// given move on the parent
	public static synchronized TreeNode createChildByMove(TreeNode parent, FreecellMove move) {

		//at first the child is created as an exact copy of the parent
		//and then the given move is executed on it
		TreeNode child = new TreeNode(parent);		
		child.setG(parent.getG()+1);		
		child.setParent(parent);
		executeMoveOnNode(child, move);
		if (child.getAlgorithm() == BEST || child.getAlgorithm() == ASTAR) {
			child.calculateHeuristicValue(); // calculate h of child
			child.setF(child.getH() + child.getG()); // f=h+j, in case of astar algorithm
		}

		return child;
	}

	private static void executeMoveOnNode(TreeNode node, FreecellMove move) {

		boolean cardadded = false;
		boolean cardremoved = false;

		// FOUNDATION type moves
		if (move.getType() == FreecellMove.FOUNDATION) {
			Card cardthatmoved = move.getCard1();
			// locate the card and move it accordingly
			for (LinkedList<Card> freecell : node.getFreecells()) {
				if (freecell.contains(cardthatmoved)) {
					freecell.remove(cardthatmoved);
					cardremoved = true;
				}
				if(cardremoved) break;
			}
			if (!cardremoved) { // if it has not already been removed
				for (LinkedList<Card> cascade : node.getCascades()) {
					if (cascade.contains(cardthatmoved)) {
						cascade.remove(cardthatmoved);
						cardremoved = true;						
					}
					if(cardremoved) break;
				}
			}

			// Move it to the correct position
			if (cardthatmoved.getValue() == 0) { 	// if it's an ACE add it to the first empty foundation
				for (LinkedList<Card> foundation : node.getFoundations()) {					
					if(!cardadded) { // if it has not been added to a foundation already
						if (foundation.isEmpty()) {
							foundation.add(cardthatmoved);
							cardadded = true;
						}
					} else break;					
				}
			} else { // if it's not an ACE we have to add it to the correct foundation
				for (LinkedList<Card> foundation : node.getFoundations()) {					
					if(!cardadded){ // if it has not been added to a foundation already
						if (!foundation.isEmpty()) {
							Card foundationcard = foundation.getLast();
							if (TreeNode.moveToFoundationIsLegal(cardthatmoved, foundationcard)) {
								foundation.add(cardthatmoved);
								cardadded = true;
							}
						}
					} else break; //if it has been added already, exit the loop			
				}
			}
		}// end of FOUNDATION type moves

		// FREECELL type moves
		else if (move.getType() == FreecellMove.FREECELL) {
			Card cardthatmoved = move.getCard1();
			// locate the card and remove it from the cascades then add it to
			// the first available freecell
			for (LinkedList<Card> cascade : node.getCascades()) {
				if(!cascade.isEmpty()) {
					if(cascade.getLast().equals(cardthatmoved)) cascade.pollLast();
					cardremoved = true;
				}

				if(cardremoved) break;
			}

			for (LinkedList<Card> freecell : node.getFreecells()) {
				if(!cardadded) {
					if (freecell.isEmpty()) {
						freecell.add(cardthatmoved);
						cardadded = true;
					}
				} else break;				
			}
		}// end of FREECELL type moves

		// NEWSTACK type moves
		else if (move.getType() == FreecellMove.NEWSTACK) {
			Card cardthatmoved = move.getCard1();
			// locate the card that moved and remove it from the freecells or
			// from a cascade and move it to the first empty cascade
			for (LinkedList<Card> freecell : node.getFreecells()) {
				if(!cardremoved){
					if (freecell.contains(cardthatmoved)) {
						freecell.remove(cardthatmoved);
						cardremoved = true;
					}
				} else break;				
			}
			if (!cardremoved) { // if it has not already been removed
				for (LinkedList<Card> cascade : node.getCascades()) {
					if(!cardremoved) {
						if (cascade.contains(cardthatmoved)) {
							cascade.remove(cardthatmoved);
							cardremoved = true;
						}
					} else break;					
				}
			}

			//add it to the first empty cascade
			for (LinkedList<Card> cascade : node.getCascades()) {
				if(!cardadded){
					if (cascade.isEmpty()) {
						cascade.add(cardthatmoved);
						cardadded = true;
					}
				} else break;				
			}

		}// end of NEWSTACK type moves

		// STACK type moves
		else if (move.getType() == FreecellMove.STACK) {
			Card card1 = move.getCard1();
			Card card2 = move.getCard2();
			// remove card1 from it's former position and add it on top of card2
			// (stack it)
			for (LinkedList<Card> freecell : node.getFreecells()) {
				if(!cardremoved){
					if (freecell.contains(card1)) {
						freecell.remove(card1);
						cardremoved = true;
					}
				} else break;

			}
			if (!cardremoved) { // if it has not already been removed
				for (LinkedList<Card> cascade : node.getCascades()) {
					if(!cardremoved){
						if (cascade.contains(card1)) {
							cascade.remove(card1);
							cardremoved = true;
						}
					} else break;					
				}
			}

			// go through the cascades and find card2, stack card1 on top of it
			for (LinkedList<Card> cascade : node.getCascades()) {
				if(!cardadded) {
					if(!cascade.isEmpty()){
						Card cascadecard = cascade.getLast();
						if (cascadecard.equals(card2)) {
							cascade.add(card1);
							cardadded = true;
						}
					}	
				} else break;

			}
		}// end of STACK type moves

		else {
			throw new RuntimeException("Invalid move type.\n");
		}

		node.setLastmove(move);
	}

	//Given a tree-node, it returns all the children that can be created by executing one freecell move on it
	//(It returns all immediate children of the node it receives)
	public TreeSet<TreeNode> extractChildren(TreeNode node){
		TreeSet<TreeNode> extracted = new TreeSet<TreeNode>();
		TreeNode child = null;

		//first check the freecell cards
		for(LinkedList<Card> freecell : node.getFreecells()){

			if(!freecell.isEmpty()) {

				Card freecellcard = freecell.getLast();

				child = createChild_FOUNDATION(freecellcard, node);				
				if(child != null) extracted.add(child);
				child = null;

				child = createChild_STACK(freecellcard, node);				
				if(child != null) extracted.add(child);
				child = null;

				child = createChild_NEWSTACK(freecellcard, node);				
				if(child != null) extracted.add(child);
				child = null;	

			}			

		}


		//now check the cascade cards
		for(LinkedList<Card> cascade : node.getCascades()) {

			if(!cascade.isEmpty()) {

				Card cascadecard = cascade.getLast();

				child = createChild_FOUNDATION(cascadecard, node);				
				if(child != null) extracted.add(child);
				child = null;

				child = createChild_STACK(cascadecard, node);				
				if(child != null) extracted.add(child);
				child = null;

				child = createChild_FREECELL(cascadecard, node);				
				if(child != null) extracted.add(child);
				child = null;			

				child = createChild_NEWSTACK(cascadecard, node);				
				if(child != null) extracted.add(child);
				child = null;				

			}			

		}

		return extracted;
	}

	//creates and returns a child that is the result of executing a FOUNDATION type move on the given node
	//if no such child can be created it returns null
	private static TreeNode createChild_FOUNDATION(Card somecard, TreeNode node) {

		TreeNode child = null;

		for(LinkedList<Card> foundation : node.getFoundations()){ //go through the foundations
			if(foundation.isEmpty()) {
				if(somecard.getValue() == 0) { //if it is an ACE it can move to an empty foundation
					child = createChildByMove(node, new FreecellMove(FreecellMove.FOUNDATION, somecard));
					return child;
				}
			}
			else { //if it's not an empty foundation
				Card foundationcard = foundation.getLast();
				if(moveToFoundationIsLegal(somecard, foundationcard)) {
					child = createChildByMove(node, new FreecellMove(FreecellMove.FOUNDATION, somecard));
					return child;
				}
			}
		}

		return child;
	}


	//creates and returns a child that is the result of executing a FREECELL type move on the given node
	//if no such child can be created it returns null
	private static TreeNode createChild_FREECELL(Card somecard, TreeNode node) {

		TreeNode child = null;

		//just check for empty freecells since any card can be moved to an empty freecell		
		for(LinkedList<Card> freecell : node.getFreecells()) {
			if(freecell.isEmpty()) {
				child = createChildByMove(node, new FreecellMove(FreecellMove.FREECELL, somecard));
				return child;
			}
		}		
		return child;

	}


	//creates and returns a child that is the result of executing a STACK type move on the given node
	//if no such child can be created it returns null
	private static TreeNode createChild_STACK(Card somecard, TreeNode node) {

		TreeNode child = null;

		for(LinkedList<Card> cascade : node.getCascades()) { //go through the cascades
			if(!cascade.isEmpty()) {
				Card cascadecard = cascade.getLast();
				if(somecard.getValue() == cascadecard.getValue()-1) { //if it's smaller by one and the suits are compatible
					if(somecard.suitIsCompatible(cascadecard)) {
						child = createChildByMove(node, new FreecellMove(FreecellMove.STACK, somecard, cascadecard));
						return child;
					}
				}
			}
		}

		return child;
	}

	//creates and returns a child that is the result of executing a NEWSTACK type move on the given node
	//if no such child can be created it returns null
	private static TreeNode createChild_NEWSTACK(Card somecard, TreeNode node) {

		TreeNode child = null;
		//it basically just checks if there are any empty cascades
		//since any card can be moved to an empty cascade
		for(LinkedList<Card> cascade : node.getCascades()) {
			if(cascade.isEmpty()) {
				child = createChildByMove(node, new FreecellMove(FreecellMove.NEWSTACK, somecard));
				return child;
			}
		}

		return child;
	}

	//Check if it is legal for a card to move to a non-empty foundation
	private static boolean moveToFoundationIsLegal(Card card1, Card foundationcard) {

		if (card1.getValue() == (foundationcard.getValue() + 1)) {
			if (card1.getSuit().equalsIgnoreCase(foundationcard.getSuit())) return true;
		}

		return false;
	}



}
