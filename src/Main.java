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

import gproblem.GTransshipmentProblem;
import gsolution.GTransshipmentSolution;
import gsolver.*;

/**
 * 
 * @author Olivier Grunder
 * @version 0.03
 * @date 22 avril 2011
 *
 */
public class Main {
	
	final static String OPTION_HELP_SHORT = "-h" ;
	final static String OPTION_HELP_LONG  = "--help" ;
	
	final static String OPTION_EVAL_SHORT = "-e" ;
	final static String OPTION_EVAL_LONG  = "--eval" ;

	final static String OPTION_ENUM_SHORT = "-se" ;
	final static String OPTION_ENUM_LONG  = "--solve-enum" ;

    final static String OPTION_BRANCHBOUND_LONG  = "--solve" ;
    final static String OPTION_BRANCH_BOUND_SHORT  = "-s" ;

	final static String OPTION_GENERATE_SHORT = "-g" ;
	final static String OPTION_GENERATE_LONG  = "--generate" ;

	// challenge file names
	public static final String[] challengeFilenames = {
		"data/transshipment1.txt",
		"data/transshipment2.txt",
        "data/tshp006-01.txt",
        "data/tshp010-03.txt",
        "data/tshp020-01.txt",
        "data/tshp050-02.txt",
        "data/tshp100-03.txt",
        "data/tshp500-02.txt"
        } ;
	
	
	//	- 10 secondes pour les petites instances 
	//	- 30 secondes pour les moyennes instances 
	//	- 1 minutes pour les grandes instances 
	public static final long[] solvingTime = {
		-1,
		-1} ;


	/**
	 * 
	 */
	private static void help() {
		System.out.println("") ;
		System.out.println("Usage: script/run [options]") ;
		System.out.println("") ;
		System.out.println("with:") ;
		
		System.out.println("    --solve [n] ") ;
		for (int i=0;i<challengeFilenames.length;i++) {
			System.out.println("    \t n="+i+" for instance: "+challengeFilenames[i]) ;
		}
		System.out.println("") ;
		
		System.out.println ("    "+OPTION_HELP_SHORT+", "+OPTION_HELP_LONG) ;
		System.out.println ("    \t prints this message") ;
		System.out.println ("") ;
		
		System.out.println ("    "+OPTION_EVAL_SHORT+", "+OPTION_EVAL_LONG+" filename") ;
		System.out.println ("    \t evaluate a solution") ;
		System.out.println ("") ;

		System.out.println ("    "+OPTION_ENUM_SHORT+", "+OPTION_ENUM_LONG+" filename") ;
		System.out.println ("    \t solve the pb with full enumeration") ;
		System.out.println ("") ;
		
		System.out.println ("    "+OPTION_GENERATE_SHORT+", "+OPTION_GENERATE_LONG+" filename instance_size") ;
		System.out.println ("    \t generate an instance with <instance_size> nodes") ;
		
	}



	/**
	 * get a problem from either a filename or a number that is
	 * connected to challenge files
	 */
	private static GTransshipmentProblem getProblem(String filename) {
		int c = -1 ;
		GTransshipmentProblem pb=null ;

		try {
				c = new Integer(filename).intValue() ;
		}
		catch (Exception e) {}

		if (c>=0 && c<challengeFilenames.length) {
		    System.out.println ("Loading challenge file: "+challengeFilenames[c] ) ;
		    pb = new GTransshipmentProblem(challengeFilenames[c]) ;
		}
		else {
		    System.out.println ("Loading instance file: "+filename ) ;
		    pb = new GTransshipmentProblem(filename) ;
		}
		return pb ;
	}



	/**
	 * Solver Enumerate 
	 */
	private static void commandSolveEnum(String filename) {
		System.out.println ("commandSolveEnum") ;
		GTransshipmentProblem pb=getProblem(filename) ;
		if (pb!=null) {
			System.out.println("pb!=null") ;
		    GSolverEnumerate solv =	new GSolverEnumerate(pb) ;
			System.out.println("solv.start") ;
		    //solv.start() ;
			solv.solve();
		}
	}

    /*
    *
    * Solver MySolver
    *
     */
    private static void commandSolve(String filename) {
        System.out.println ("commandSolveEnum") ;
        GTransshipmentProblem pb=getProblem(filename) ;
        if (pb!=null) {
            System.out.println("pb!=null") ;
            MySolver solv =	new MySolver(pb) ;
            System.out.println("solv.start") ;
            //solv.start() ;
            solv.solve();
        }
    }

	/**
	 *
	 */
	private static void commandEval(String filename) {
		System.out.println ("commandEval") ;
		GTransshipmentProblem pb=getProblem(filename) ;
		if (pb!=null) {

			// Evaluation of a solution
			GTransshipmentSolution sol = new GTransshipmentSolution(pb) ;
			int[] tabAssignt = {-1,2,0,2,4,3,1,1,2,3,2,0,4,1,1,0,3,1,1,5,0,0} ; // Edge indices start from 1 indice!!!
			sol.setTabAssignment(tabAssignt) ;
			sol.evaluate() ;
			System.out.println("\nsolution sol="+sol.toString() +"\n") ;
		}
	}



	/**
	 * @param n 
	 *
	 */
	private static void commandGenerateInstance(String filename, int n) {
		System.out.println ("commandGenerateInstance") ;
		GTransshipmentProblem pb=GTransshipmentProblem.getRandomInstance(n) ;
		pb.save(filename) ;

	}



	/** ##########################################################################""
	 *  Argument Line Parser
	 */
	private static void argumentLineParser(String[] args) {
		String instanceFilename = null ;
		
    	if (args.length>0) {
    		System.out.println("args.length="+args.length) ;
    		int p = 0 ;
    		while (p<args.length) {
    			System.out.println("args[p]="+args[p]) ;
    			
    			if (args[p].equalsIgnoreCase(OPTION_HELP_SHORT) || args[p].equalsIgnoreCase(OPTION_HELP_LONG))
    				help() ;

    			// generate instance
    			else if (args[p].equalsIgnoreCase(OPTION_GENERATE_SHORT) || args[p].equalsIgnoreCase(OPTION_GENERATE_LONG)) {
    				String filename = null ;
    				if (p+2<args.length) {
    					filename = args[++p] ;
    					int n = new Integer(args[++p]).intValue() ;
						commandGenerateInstance(filename, n) ;
					}
    				else 
    					System.out.println("Problem filename and size of graph missing") ;
					return ;
    			}

				// Solve with enum 
    			else if (args[p].equalsIgnoreCase(OPTION_ENUM_SHORT) || args[p].equalsIgnoreCase(OPTION_ENUM_LONG)) {
    				String filename = null ;
    				if (p+1<args.length) {
    					filename = args[++p] ;
    					commandSolveEnum(filename) ;
    					
    					System.exit(0) ;
    				}
    				else 
    					System.out.println("Problem filename missing") ;
    			}
                else if (args[p].equalsIgnoreCase(OPTION_BRANCH_BOUND_SHORT) || args[p].equalsIgnoreCase(OPTION_BRANCHBOUND_LONG)) {
                    String filename = null ;
                    if (p+1<args.length) {
                        filename = args[++p] ;
                        commandSolve(filename) ;

                        System.exit(0) ;
                    }
                    else
                        System.out.println("Problem filename missing") ;
                }
				// eval
    			else if (args[p].equalsIgnoreCase(OPTION_EVAL_SHORT) || args[p].equalsIgnoreCase(OPTION_EVAL_LONG)) {
    				String filename = null ;
    				if (p+1<args.length) {
    					filename = args[++p] ;
    					commandEval(filename) ;
    					
    					System.exit(0) ;
    				}
    				else 
    					System.out.println("Problem filename missing") ;
    			}
    			else {
    				System.out.println("Argument line error : args["+p+"]="+args[p]+"") ;
    				
					System.exit(0) ;
    			}
    			p++ ;    			
    		}
    		
    	}
    	else 
    		help() ;
	}







	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		argumentLineParser(args) ;
	}



}
