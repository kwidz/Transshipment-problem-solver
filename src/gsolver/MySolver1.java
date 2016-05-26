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
public class MySolver1 extends GSolver {


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
    public MySolver1() {
        super();
    }

    /**
     * @param problem
     */
    public MySolver1(GTransshipmentProblem problem) {
        super(problem);
    }

    /**
     * solves the problem
     */
	/* (non-Javadoc)
	 * @see gsolver.GSolver#solve()
	 */
    public void solve() {
        System.out.println("My solver is solving") ;

        tabDepots = buildTabDepots() ;
        tabPlatforms = buildTabPlatforms() ;
        tabClients = buildTabClients() ;
        currentSolution = buildFirstAssignment() ;
        bestSolution = (GTransshipmentSolution) currentSolution.clone() ;

        recursiveSearch () ;

        System.out.println("Best solution="+bestSolution.toString()) ;
    }

    private GTransshipmentSolution buildFirstAssignment() {
        GTransshipmentSolution sol = new GTransshipmentSolution(problem) ;

        // First part : depot
        for (int i=0;i<problem.getNbrNodes();i++) {
            GNode node = problem.getNode(i) ;
            if (node.isDepot()) {
                // prise en compte de la capacité des arcs
                int qty=-node.getDemand();
                for (int j=0;j<node.getNbrEdges()-1;j++) {
                    int indice = node.getEdgeIndice(j) ;
                    if(qty>0) {
                        int capacity=node.getEdge(j).getCapacity();
                        if(capacity>=qty){
                            sol.setAssignement(indice, qty);
                            qty=0;
                        }
                        else{
                            sol.setAssignement(indice,capacity);
                            qty-=capacity;
                        }
                    }
                    else
                        sol.setAssignement(indice, 0) ;

                }
                // Last edge is fully loaded
                //int indice = node.getEdgeIndice(node.getNbrEdges()-1) ;
                //sol.setAssignement(indice, -node.getDemand()) ;
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
                    remainingStock-=qty;
                    tabRemainingDemand[client.getIndice()]-=qty;

                    sol.setAssignement(edge.getIndice(), qty) ;
                }
            }
        }

        sol.evaluate();

        System.out.println("First assignment="+sol.toString()) ;

        return sol;
    }

    private void recursiveSearch() {

        boolean chaineAmeliorante=true;
        while(chaineAmeliorante){

            chaineAmeliorante=false;
            

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
