import java.util.LinkedList;
import java.util.TreeSet;

public class Frontier {

	private static TreeSet<TreeNode> nodes;	//the nodes of the frontier
	private static TreeNode node;			//used to keep track of the node that is being expanded each time
	private static boolean solutionFound = false;

	//variables to handle memory
	private static long freemem = 0;
	private static long initialfreemem = 0;
	private static long limit = 0;
	private static long lastCheck = 0;
	private static Runtime runtime = null;

	//instance of the Frontier class singleton
	private static Frontier INSTANCE = null;

	//private constructor, this class is a singleton
	private Frontier(){
		nodes = new TreeSet<TreeNode>();
		runtime = Runtime.getRuntime();
		freemem = Math.round(((runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()))/1024)/1024);
		initialfreemem = freemem;
		limit = Math.round(initialfreemem*0.25); //use up to 75% of the JVM's max heap space
	}

	public static Frontier getInstance(){
		if(INSTANCE == null) {
			INSTANCE = new Frontier();
		}
		return INSTANCE;
	}

	//Begins the search and returns a solution as soon as it finds one.
	//The algorithm used is the one set when creating the root node
	//If no solution is found and the search space or the memory is exhausted it returns null
	public Solution beginSearch(TreeNode root) {

		Solution solution = null;

		//At first the search space consists of just the root node
		nodes.add(root);

		int expanded = 0;
		lastCheck = System.nanoTime();

		while (!solutionFound && !nodes.isEmpty() && (freemem > limit)) {

			//System.out.println("Frontier size: " +nodes.size() +" nodes.");

			node = nodes.pollFirst();	//get the next node and remove it from the frontier

			


			//if(loops%5000==0) System.out.println(node.toString());


			if (isSolution(node)) {
				solutionFound = true;
				solution = new Solution(node);
			}

			if(!solutionFound){				
				//extract the current node's children and add them to the frontier
				nodes.addAll(node.extractChildren());
				expanded++;
			}
			
			
			//Time since last check in seconds
			double seconds = (double) (System.nanoTime() - lastCheck) / 1000000000.0;
			
			if(seconds > 2) { //don't check too often
				updateFreeMem();
				lastCheck = System.nanoTime();			
			}				


		}// end of while loop

		System.out.println("Expanded: " +expanded +" nodes. Nodes in the frontier: " +nodes.size());
		if(nodes.isEmpty()) System.out.println("Exhausted search space.\n");
		if(limit > freemem) System.out.println("JVM heap size over " +(initialfreemem-limit) +"MB. Aborting search.");
		//System.out.println("Last node: \n" +node.toString());
		return solution;
	}

	private boolean isSolution(TreeNode node) {



		for (LinkedList<Card> cascade : node.getCascades()) {
			if (!cascade.isEmpty()) {				
				return false;
			}
		}

		for (LinkedList<Card> freecell : node.getFreecells()) {
			if (!freecell.isEmpty()) {				
				return false;
			}
		}

		return true;
	}

	private void downsizeTreeSet(TreeSet<TreeNode> treeset, int desiredSize) {
		while(treeset.size() > desiredSize) {
			treeset.pollLast();
		}
	}

	private static void updateFreeMem() {
		freemem = Math.round(((runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()))/1024)/1024);
	}




}
