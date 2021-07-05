package nl.cochez.bgpExtractor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

/**
 * Known limitations: paths are not taken into account!!
 * @author cochez, Timm877
 
 *  In this program, first connections are made with the original graph, and the augmented graph.
	at start, both graph contain the same entities.
	then for each querylog, we run code to extract BGPs
	From these BGPs, the result triples in the graph is extracted via bindvariables of 
	the solution to get a set of triples of query solution
 * Then:
For each new triple consisting of subject predicate object, we:
1) create triple: subject connectRelation uniqueAugmentedSubjectID 
2) create triple: object connectRelation uniqueAugmentedObjectID
3) create triple: uniqueAugmentedSubjectID predicate uniqueAugmentedObjectID

Explanation: the extracted BGPs from the querries may have multiple entries for the same subject pred obj (or one of those)
1) With connectRelation, we couple the original subject with a unique "copy" of subject for each entry in the BGPfile
2) We do the same for the object
3) And then we connect the copies of subject and object with the original relation

These triples are then sent to the augmented graph in ntriples format
 *
 */

public class AugmentGraph {

    public static int lines = 0;
    public static int linesTotal = 0;
    public static int amountofQueries = 0;
    public static int count = 0;
	public static void main(String[] args) throws IOException {	
		// here we load the model from a triple store	
		// Iterate over all query logs
		File path = new File("/var/scratch/wbr950/selectQueriesBACKUP/"); ///var/scratch/wbr950/
	    File [] files = path.listFiles();
	    HTTPRepository repository = new HTTPRepository("http://10.141.255.254:7200/repositories/Subgraph"); //10.141.255.254:
	    RepositoryConnection connection = repository.getConnection();	
        HTTPRepository repository2 = new HTTPRepository("http://10.141.255.254:7200/repositories/augmentedSubgraph");
    	RepositoryConnection connection2 = repository2.getConnection();    
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile()){ 
	            extractBGP(files[i], connection, connection2);
	        }
	    } 	 
	    System.out.println("Finished.");	  
	    connection.close();
	    connection2.close();
	    
	}

	private static void extractBGP (File queryfile, RepositoryConnection connection, RepositoryConnection connection2) throws FileNotFoundException {	
		// query file  as input to this program with graph model
		// file has one SELECT query for each line
    	Scanner scanner = new Scanner(queryfile);
    	PrintWriter hop = new PrintWriter("ResultTriples/" + queryfile.getName() +  ".nt"); // keep track of results of query 
    	
  	  while (scanner.hasNextLine())   {  	
  		    count++; 
  		    try {
			Query q = QueryFactory.create(scanner.nextLine());			    
			Op op = Algebra.compile(q);		
		    PrintWriter out = new PrintWriter("ResultTriples/" + count +  ".nt"); //write triple file for each query
			op.visit(new AllBGPOpVisitor() {
				public final void visit(OpBGP opBGP) {
					 // don't do anything with query which only has one triple:
				    if (opBGP.getPattern().size() < 2) {
				    	return;
				    }			  
					// get each basic graph pattern (BGP) from the query.
					Query query = OpAsQuery.asQuery(opBGP);	
					
					// Execute the query on our graph, for each result we bind variables to unique BGP; 
					// which we then all add together
					try {
		            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
		                    query.toString());           
		            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();		           
		            
		            while (tupleQueryResult.hasNext()) {
		            			BindingSet bindingSet = tupleQueryResult.next();
								bindVariables(bindingSet, opBGP.getPattern(), out);	
								// if output exceeds 1000, stop.
							    if (lines >= 1000) {
							    	break;
							    }
							}
		            tupleQueryResult.close();
					}   catch (QueryEvaluationException e) {
						e.printStackTrace();				
				}
				}
			});			
    	    out.close(); 
    	    
    	    // don't do anything with query output whose output is more than 1000 lines:
    	    if (lines >= 1000) {
    	    	// delete the just made file again
    	    	System.out.println(count);
    	    	File srcFile = new File("ResultTriples/" + count +  ".nt");
                if (srcFile.delete()) { 
                    System.out.println("Deleted the file because the output was to big: " + srcFile.getName());
                  } else {
                    System.out.println("Failed to delete the file.");
                  } 
                // put lines back at zero for next query
                //System.out.println("output too big. Line amount: "+ lines);
                lines = 0;
                //System.out.println("Line back at zero: "+ lines);
    	    	continue;
    	    } 
    	    // lines could be zero so just delete file
    	    if (lines == 0) {
    	    	// delete the just made file again
    	    	System.out.println(count);
    	    	File srcFile = new File("ResultTriples/" + count +  ".nt");
                if (srcFile.delete()) { 
                    System.out.println("Deleted the file because the output was to big: " + srcFile.getName());
                  } else {
                    System.out.println("Failed to delete the file.");
                  } 
                // put lines back at zero for next query
              //  System.out.println("output too big. Line amount: "+ lines);
                lines = 0;
               // System.out.println("Line back at zero: "+ lines);
    	    	continue;
    	    }          	    
    	    // if output is valid (above 0 below 1000), let's move on
    	    // log and put everything back 
    	    amountofQueries++;
    	    linesTotal = linesTotal + lines;
    	    hop.println("output good. Line amount: "+ lines + ". Total lines added: " + linesTotal + ". Query num: " + amountofQueries + ". For query: " + op);
    	    
    	    lines = 0;
            File srcFile2 = new File("ResultTriples/" + count +  ".nt");            	
        	try {
        		connection2.add(srcFile2, RDFFormat.NTRIPLES);
        	} catch (RDFParseException | RepositoryException | IOException e) {
        		e.printStackTrace();
        		continue;
        	}   
        	connection2.close();

            if (srcFile2.delete()) { 
                System.out.println("Deleted the file: " + srcFile2.getName());
              } else {
                System.out.println("Failed to delete the file.");
                continue;
              }    
            
  		  } catch (org.apache.jena.query.QueryParseException  e) {
		    	System.out.println("Error creating query" + e);
		    	continue;
		    }
		}	
  	scanner.close();
  	hop.close();
	}

	private static void bindVariables (BindingSet solution, BasicPattern bgp, PrintWriter out) {
	
	    String connectRelURI = "<http://example.com/connectRel>"; // a relationship ID to augment graph with

		for (Triple triple : bgp) {
			Node subject= triple.getSubject();
			Node predicate= triple.getPredicate();
			Node object = triple.getObject();
			//out.println(triple);
			String subj = null;
			String pred = null;
			String obj = null;
			
			if (object.isLiteral()){
				continue;
			}
			
			if (subject.isLiteral()){
				continue;
			}
			
			if (subject.isVariable()) {
				//replace it
				for (Binding binding : solution) {
					if (subject.getName().equals(binding.getName())) {
						subj = binding.getValue().toString();
			}
			}
			} else {
				subj = subject.toString();
			}
			if (predicate.isVariable()) {
				//replace it
				for (Binding binding : solution) {
					if (predicate.getName().equals(binding.getName())) {
						pred = binding.getValue().toString();
			}
			}
			} else {
				pred = predicate.toString();
			}

			if (object.isVariable()) {
				for (Binding binding : solution) {
					if (object.getName().equals(binding.getName())) {
						obj = binding.getValue().toString();
			}
			}
			} else {
				if (object.isLiteral()){
					continue;
				}
					obj = object.toString();
			 }
		    // Now we have the query solution as a triple, now we use those to augment the graph with
			// 3 triples per triple.
			//out.println(subj + pred + obj);
	    	String uniqueSubjID = "<" + subj + "_" + lines + count + "aapnootmies>";
		    String uniqueObjID = "<"  + obj + "_" + lines + count + "aapnootmies>";
		    ++lines;
		    
		    String trip1 = "<" + subj + ">" + " " + connectRelURI + " " +  uniqueSubjID + " .";
		    String trip2 = uniqueSubjID + " " + "<" + pred + ">" + " " +  uniqueObjID + " .";
		    String trip3 = uniqueObjID + " " + connectRelURI + " " + "<" + obj + ">" + " .";
		    
		    // skip results which are literals as literals dont work for this approach
		    if (trip1.chars().filter(ch -> ch == '"').count() < 2) {
		    	out.println(trip1) ;
		    }
		    if (trip2.chars().filter(ch -> ch == '"').count() < 2) {
		    	out.println(trip2) ;
		    }
		    if (trip3.chars().filter(ch -> ch == '"').count() < 2) {
		    	out.println(trip3) ;
		    }
			}  
		}
}