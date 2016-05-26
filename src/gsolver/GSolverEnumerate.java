/**
 * 
 *     This file is part of ag41-2016P-challenge-transshipment
 *     
 *     ag41-2016P-challenge-transshipment is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     
 *     ag41-2016P-challenge-transshipment is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     
 *     You should have received a copy of the GNU General Public License
 *     along with ag41-2016P-challenge-transshipment.  If not, see <http://www.gnu.org/licenses/>.
 *     
 */
package gsolver;

import java.util.Vector;

import javax.swing.text.TabableView;

import gproblem.GEdge;
import gproblem.GNode;
import gproblem.GTransshipmentProblem;
import gsolution.GTransshipmentSolution;
import gsolver.GSolver;


/**
 * 
 * @author Olivier Grunder
 * @version 0.01
 * @date 7 march 2016
 *
 */
public class GSolverEnumerate extends GSolver {
	
	private static final int KEY_DEPOT = 0 ;
	private static final int KEY_PLATFORM = 1 ;
	
	private static final String TAB_KEY_NAME[]={"D","PF"} ; 
	
	private GNode[] tabDepots = null ;
	private GNode[] tabPlatforms = null ;
	private GNode[] tabClients = null ;
	
	// remaining demand of clients
	private int[] tabRemainingDemand=null ;
	
	// Qty of products in platforms 
	private int[] tabQty = null ; 

	/**
	 * 
	 */
	public GSolverEnumerate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param problem
	 */
	public GSolverEnumerate(GTransshipmentProblem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	/**
	 * solves the problem
	 */
	/* (non-Javadoc)
	 * @see gsolver.GSolver#solve()
	 */
	public void solve() {
		System.out.println("solve") ;
		
		tabDepots = buildTabDepots() ;
		tabPlatforms = buildTabPlatforms() ;
		tabClients = buildTabClients() ;
		currentSolution = buildFirstAssignment() ;
		bestSolution = (GTransshipmentSolution) currentSolution.clone() ;
		
		recursiveSearch (KEY_DEPOT, 0) ;
		
		System.out.println("Best solution="+bestSolution.toString()) ;
		
	}


	
	/**
	 * 
	 * @return
	 */
	private GTransshipmentSolution buildFirstAssignment() {
		GTransshipmentSolution sol = new GTransshipmentSolution(problem) ;
		
		// First part : depot 
		for (int i=0;i<problem.getNbrNodes();i++) {
			GNode node = problem.getNode(i) ;
			if (node.isDepot()) {
				// All edges are set to 0, except
				for (int j=0;j<node.getNbrEdges()-1;j++) {
					int indice = node.getEdgeIndice(j) ;
					sol.setAssignement(indice, 0) ;
				}
				// Last edge is fully loaded
				int indice = node.getEdgeIndice(node.getNbrEdges()-1) ;
				sol.setAssignement(indice, -node.getDemand()) ;				
			}
		}

		// Second part : platform 
		tabRemainingDemand = new int[problem.getNbrNodes()+1] ;
		for (int i=0;i<problem.getNbrNodes();i++)
			tabRemainingDemand[problem.getNode(i).getIndice()] = problem.getNode(i).getDemand() ;
			
		for (int i=0;i<problem.getNbrNodes();i++) {
			GNode node = problem.getNode(i) ;
			if (node.isPlatform()) {
				// How many part comes to the platform ?
				int total = 0 ;
				for (int j=0;j<problem.getNbrEdges();j++) {
					GEdge edge = problem.getEdge(j) ;
					// If edge arrives to the platform
					if (edge.getEndingNode().getIndice()==node.getIndice()) {
						// get the qty of the assignement of this edge
						total += sol.getAssignement(edge.getIndice()) ;
					}
				}
				
				int remainingStock = total ;
				// All edges are set to 0, except
				for (int j=0;j<node.getNbrEdges();j++) {
					GEdge edge = node.getEdge(j) ;
					GNode client = edge.getEndingNode() ;
					int remainingDemand = tabRemainingDemand[client.getIndice()] ;
					
					int qty = Math.min(remainingStock, remainingDemand) ;
 
					sol.setAssignement(edge.getIndice(), qty) ;
				}
			}
		}
		
		sol.evaluate();
		
		System.out.println("First assignment="+sol.toString()) ;

		return sol;
	}

	/**
	 *  recursive search following the basic principle :
	 *  1. assign a flow to every depot
	 *  2. assign a flow to every platform
	 *  
	 *  @param key : DEPOT/PLATFORM
	 *  @param indiceTab : indice of node in tabDepots or tabPlatforms
	 *   
	 */
	private void recursiveSearch(int key, int indiceTab) {
		
		// Test if solution is finished
		if (key==KEY_PLATFORM && indiceTab>=tabPlatforms.length) {
			// Bug v0.03 : If client demand is not satisfied, the solution is not feasible
			if (!currentSolution.isClientDemandSatisfied()) 
				return ;
			
			double eval = currentSolution.evaluate() ;
//System.out.println("evaluation of full currentSolution="+currentSolution) ;
//System.exit(0) ;
			if (bestSolution==null || eval<bestSolution.getEvaluation()) {
System.out.println("eval="+eval+" ; bestSolution.getEvaluation()="+bestSolution.getEvaluation()) ;				
				bestSolution = (GTransshipmentSolution) currentSolution.clone() ;
				System.out.println("New best solution found: "+bestSolution.toString()+"\n") ;
			}
			return ;
		}
		else {
			GNode currentNode = null ;
			int nodeDemand = 0 ;
			switch (key) {
			case KEY_DEPOT : 
				currentNode=tabDepots[indiceTab] ; 
				nodeDemand = Math.abs(currentNode.getDemand()) ;
				break ;
			case KEY_PLATFORM : 
				currentNode=tabPlatforms[indiceTab] ; 
				
				if (indiceTab==0) { // Do it only when platform assignment is reached
					// compute the number of products in platforms
					tabQty = new int[problem.getNbrNodes()+1] ;
					for (int i=0;i<tabQty.length;i++) tabQty[i] = 0 ;
					
					for (int i=0;i<problem.getNbrNodes();i++) {
						if (problem.getNode(i).isPlatform()) {
							tabQty[problem.getNode(i).getIndice()] = 0 ;
							for (int j=0;j<problem.getNbrEdges();j++) {
								GEdge edge = problem.getEdge(j) ;
								// If edge arrives to the platform
								if (edge.getEndingNode().getIndice()==problem.getNode(i).getIndice()) {
									// get the qty of the assignement of this edge
									tabQty[problem.getNode(i).getIndice()] += currentSolution.getAssignement(edge.getIndice()) ;
								}
							}
//System.out.println("Platform "+problem.getNode(i).getIndice()+" : qty="+tabQty[problem.getNode(i).getIndice()]) ;
						}
					}
				}
				nodeDemand = tabQty[currentNode.getIndice()] ;
				break ;
			}
			assert (currentNode!=null) : currentNode ;
			
			// starting point is first edge is set to demand of node, other are set to 0, 
			// last edge is used to make the complementary to reach the total demand
			// at each step, 1 is added to the qty of 1st edge, if total demand is exceeded,
			// then the demand of the first edge is reset to 0, and 1 is added to next edge.
			// this process is repeated on next edge, ie if the increment makes it reach a bigger value
			// than the demand, then it is reset to 0, and 1 is added to next edge (edge 3).
			// The process stops when the edge before the last one cannot be incremented anymore.
			// For example, for 3 edges and a demand of 3, we get 
			// Edge 1 : 3 0 1 2 0 1 0
			// Edge 2 : 0 1 1 1 2 2 3 -> max is reached 
			// -----------------------------------------------
			// Edge 3 : 0 2 1 0 1 0 0
			boolean allCombinationsExplored=false ;
			while (!allCombinationsExplored) {

				// change the assignment at that level : add 1 to first edge assignment
				int startEdge = 0 ; // indice of starting edge in tabEdges of currentNode
				boolean finished = false ;
				do {
					int edgeIndice = currentNode.getEdgeIndice(startEdge) ; 
					int qty = currentSolution.getAssignement(edgeIndice)+1 ;
					int capa = problem.getEdgeFromIndice(edgeIndice).getCapacity() ;

					if ( qty > nodeDemand  || qty > capa) {
						currentSolution.setAssignement(edgeIndice, 0) ;						
						startEdge++ ;
						if (startEdge>=currentNode.getNbrEdges()-1) { // All possible assignment have been considered for currentNode
							allCombinationsExplored = true ;
							finished = true ;							
						}							
					}
					else {
						currentSolution.setAssignement(edgeIndice, qty) ;
						finished = true ;
					}
					
				} while (finished!=true ) ;
				
				// qty of last edge = demand - sum of the qty of the other edges
				int totqty = 0 ;
				for (int i=0;i<currentNode.getNbrEdges()-1;i++) 
					totqty += currentSolution.getAssignement(currentNode.getEdgeIndice(i)) ;
				
				int lastqty = nodeDemand - totqty ;
				
				if (lastqty>=0) {
					currentSolution.setAssignement(currentNode.getEdgeIndice(currentNode.getNbrEdges()-1), lastqty) ;	
					
					if (!allCombinationsExplored ) {

                        // Recurs...
						switch (key) {
						case KEY_DEPOT : 
							if (indiceTab+1>=tabDepots.length) {
								recursiveSearch(KEY_PLATFORM, 0) ;
							}
							else
								recursiveSearch(KEY_DEPOT, indiceTab+1) ;
							break ;
						case KEY_PLATFORM : 
							recursiveSearch(KEY_PLATFORM, indiceTab+1) ;
							break ;
						}
					}
				}
//				else 
//					System.out.println(" backtracking, lastqty="+lastqty) ;
				
				
				
			}
			
		}
	}

	
	
	/**
	 * 
	 * @return the list of client nodes
	 */
	private GNode[] buildTabClients() {
		int nbrNode = 0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()>0)
				nbrNode++ ;
		}
		
		GNode[] tabnodes = new GNode[nbrNode] ;
		int i=0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()>0)
				tabnodes[i++] = node ;
		}
		
		return tabnodes;
	}

	
	/**
	 * 
	 * @return the list of platform nodes
	 */
	private GNode[] buildTabPlatforms() {
		int nbrNode = 0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()==0)
				nbrNode++ ;
		}
		
		GNode[] tabnodes = new GNode[nbrNode] ;
		int i=0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()==0)
				tabnodes[i++] = node ;
		}
		
		return tabnodes;
	}


	/**
	 * 
	 * @return the list of depot nodes
	 */
	private GNode[] buildTabDepots() {
		int nbrNode = 0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()<0)
				nbrNode++ ;
		}
		
		GNode[] tabnodes = new GNode[nbrNode] ;
		int i=0 ;
		for (GNode node: problem.getTabNodes()) {
			if (node.getDemand()<0)
				tabnodes[i++] = node ;
		}
		
		return tabnodes;
	}


}
