package cannibals;

// CLEARLY INDICATE THE AUTHOR OF THE FILE HERE (YOU),
//  AND ATTRIBUTE ANY SOURCES USED (INCLUDING THIS STUB, BY
//  DEVIN BALKCOM).


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class UUSearchProblem {

	// used to store performance information about search runs.
	//  these should be updated during the process of searches

	// see methods later in this class to update these values
	protected int nodesExplored;
	protected int maxMemory;

	protected UUSearchNode startNode;

	//===============UUSearchNode Interface================
	protected interface UUSearchNode {
		public ArrayList<UUSearchNode> getSuccessors();
		public boolean goalTest();
		public int getDepth();

    }
	//=====================================================

	// breadthFirstSearch:  return a list of connecting Nodes, or null
	// no parameters, since start and goal descriptions are problem-dependent.
	//  therefore, constructor of specific problems should set up start
	//  and goal conditions, etc.

	public List<UUSearchNode> breadthFirstSearch(){
        resetStats();
        // A hashset to contain the explored nodes
        HashSet<UUSearchNode> explored = new HashSet<UUSearchNode>();
        // A hashmap to contain the relation between the children and their parents
        HashMap<UUSearchNode, UUSearchNode> relations = new HashMap<UUSearchNode, UUSearchNode>();
        // A queue to hold the nodes to be explored next
        Queue<UUSearchNode> frontier = new LinkedList<UUSearchNode>();

        //check if the startnode is the goal node, if yes return the path
        if(startNode.goalTest()){
            relations.put(startNode, null);
            return backchain(startNode,relations);
        }else{ //start searching
            frontier.add(startNode);
            relations.put(startNode, null);

            while(!frontier.isEmpty()){
                UUSearchNode n = frontier.poll();//pop
                explored.add(n);

                List<UUSearchNode> children = n.getSuccessors(); //get all its children
                for(UUSearchNode child: children){
                    if(!explored.contains(child) && !frontier.contains(child)){
                        if(child.goalTest()){
                            relations.put(child,n);
                            return backchain(child, relations);
                        }
                        relations.put(child, n);
                        frontier.add(child);
                    }
                }
            }
            return null;
        }
	}

	// backchain should only be used by bfs, not the recursive dfs
	private List<UUSearchNode> backchain(UUSearchNode node,
			HashMap<UUSearchNode, UUSearchNode> visited) { //DEBUG THIS IT KEEPS ADDING <010> to the path until it runs out, Why <010> has parent <010>?
		// you will write this method
        List<UUSearchNode> path = new ArrayList<UUSearchNode>();
        UUSearchNode tempNode = node;
        while(tempNode != null){

            path.add(tempNode);
            tempNode = visited.get(tempNode);
        }
        return path; //this path is from goal to the start reverse it if you want it to be from start to goal (u can use a stack)
	}

	public List<UUSearchNode> depthFirstMemoizingSearch(int maxDepth) {
		resetStats();
		// You will write this method
        HashSet<UUSearchNode> visited = new HashSet<UUSearchNode>();
        return dfsrm(startNode, visited, 0, maxDepth);
	}

//	 recursive memoizing dfs. Private, because it has the extra
//	 parameters needed for recursion.
	private List<UUSearchNode> dfsrm(UUSearchNode currentNode, HashSet<UUSearchNode> visited,
			int depth, int maxDepth) { //NOTE: used a HashSet instead of HashMap

		// keep track of stats; these calls charge for the current node
		updateMemory(visited.size());
		incrementNodeCount();

		// you write this method.  Comments *must* clearly show the
		//  "base case" and "recursive case" that any recursive function has.
        List<UUSearchNode> path = null;
        visited.add(currentNode); //mark the current node as visited
        if(currentNode.goalTest()){ //if the current node is the goal (basecase 1)
            path = new ArrayList<UUSearchNode>();
            path.add(currentNode);
            return path;
        }else if(depth > maxDepth){ //if we have reached the cutoff depth (basecase 2)
            System.out.println("Exceeded Maximum depth of " + maxDepth + " with NO RESULT");
            return null;
        }else{ // (recursive case)
            //getting current nodes children
            ArrayList<UUSearchNode> children = currentNode.getSuccessors();
            for (UUSearchNode child : children){
                // if not yet visited then visit it!
                if(!visited.contains(child)) {
                    path = dfsrm(child, visited, depth + 1, maxDepth);
                    if(path != null) {// not failure or cutoff
                        //we have found the goal, add currentNode to the path and pass it on!
                        path.add(currentNode);
                        return path;
                    }
                }
            }
            return null; // failure
        }
	}


	// set up the iterative deepening search, and make use of dfspc
//	public List<UUSearchNode> IDSearch(int maxDepth) {
//		resetStats();
//		// you write this method
//	}

	// set up the depth-first-search (path-checking version),
	//  but call dfspc to do the real work
	public List<UUSearchNode> depthFirstPathCheckingSearch(int maxDepth) {
		resetStats();

		// I wrote this method for you.  Nothing to do.

		HashSet<UUSearchNode> currentPath = new HashSet<UUSearchNode>();

		return dfsrpc(startNode, currentPath, 0, maxDepth);

	}

	// recursive path-checking dfs. Private, because it has the extra
	// parameters needed for recursion.
	private List<UUSearchNode> dfsrpc(UUSearchNode currentNode, HashSet<UUSearchNode> currentPath,
			int depth, int maxDepth) {

        // keep track of stats; these calls charge for the current node
        updateMemory(currentPath.size());
        incrementNodeCount();


        List<UUSearchNode> result;
        currentPath.add(currentNode); //add currentNode to the current path
        if(currentNode.goalTest()){ //if currentNode is the goal node (Base case 1)
            result = new ArrayList<UUSearchNode>();
            result.add(currentNode);
            return result;
        }else if(depth > maxDepth){ // if the cutoff depth has been reached (Base case 2)
            System.out.println("Exceeded Maximum depth of " + maxDepth + " with NO RESULT :(");
            return null;
        }else{ // (Recursion)
            ArrayList<UUSearchNode> children = currentNode.getSuccessors();
            for (UUSearchNode child : children){
                if (!currentPath.contains(child)){ //if the child is not part of the current path then visit it!
                    result = dfsrpc(child, currentPath, depth+1, maxDepth);
                    if (result == null){ //failure or cutoff
                        currentPath.remove(child);
                    }else{
                        result.add(currentNode);
                        return result;
                    }
                }
            }
            return null;
        }
    }

	protected void resetStats() {
		nodesExplored = 0;
		maxMemory = 0;
	}

	protected void printStats() {
		System.out.println("Nodes explored during last search:  " + nodesExplored);
		System.out.println("Maximum memory usage during last search " + maxMemory);
	}

	protected void updateMemory(int currentMemory) {
		maxMemory = Math.max(currentMemory, maxMemory);
	}

	protected void incrementNodeCount() {
		nodesExplored++;
	}

}
