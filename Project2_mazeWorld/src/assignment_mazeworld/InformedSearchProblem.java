package assignment_mazeworld;

import java.util.*;

public class InformedSearchProblem extends SearchProblem {
	
	public List<SearchNode> astarSearch() {
        resetStats();

        Queue<SearchNode> frontier = new PriorityQueue<SearchNode>();
        HashMap<SearchNode, SearchNode> visited = new HashMap<SearchNode, SearchNode>();
        HashMap<SearchNode, Double> costTable = new HashMap<SearchNode, Double>();

        frontier.add(startNode);
        visited.put(startNode, null);
        costTable.put(startNode, startNode.getCost()); //handy way of getting the cost for each node

        while(!frontier.isEmpty()){
            //====for stats purposes===
            incrementNodeCount();
            updateMemory(frontier.size() + visited.size());
            //=========================

            SearchNode currentNode = frontier.poll();
            if(costTable.get(currentNode) < currentNode.getCost()){//discard the node if it has a higher cost than it is written in the costTable
                continue;
            }
            if(currentNode.goalTest()){
                return backchain(currentNode, visited);
            }
            List<SearchNode> children = currentNode.getSuccessors();
            for(SearchNode child : children){
                if(!visited.containsKey(child)) {//if not in the visted hashMap
                    visited.put(child, currentNode);
                    costTable.put(child, child.getCost());
                    frontier.add(child);
                }else if(costTable.get(child) > child.getCost()){ // if it is already in the visited, then at what cost?
                    visited.put(child, currentNode); //update the entry for this child
                    costTable.put(child, child.getCost()); //update the cost for this child
                    frontier.add(child); //add the lower cost child to the frontier
                }
            }
        }
        return null;
	}

	public List<SearchNode> UniformCostSearch(){
        resetStats();

        Queue<SearchNode> frontier = new PriorityQueue<SearchNode>(
                (a,b) -> {return (int) Math.signum(((SearchNode)a).getCost() - ((SearchNode)b).getCost());}
                );
        HashMap<SearchNode, SearchNode> visited = new HashMap<SearchNode, SearchNode>();

        frontier.add(startNode);
        visited.put(startNode, null);

        while(!frontier.isEmpty()){
            //====for stats purposes===
            incrementNodeCount();
            updateMemory(frontier.size() + visited.size());
            //=========================

            SearchNode currentNode = frontier.poll();
            if(currentNode.goalTest()){
                return backchain(currentNode, visited);
            }
            List<SearchNode> children = currentNode.getSuccessors();
            for(SearchNode child : children){
                if(!visited.containsKey(child)) {//if not in the visted hashMap add it
                    visited.put(child, currentNode);
                    frontier.add(child);
                }
            }
        }
        return null;
    }
	
	
}
