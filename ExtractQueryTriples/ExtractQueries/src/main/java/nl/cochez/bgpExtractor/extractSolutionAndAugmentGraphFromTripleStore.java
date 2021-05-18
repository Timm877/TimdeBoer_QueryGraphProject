package nl.cochez.bgpExtractor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Known limitations: paths are not taken into account!!
 * @author cochez, Timm877
 * 
 * This program takes as input the model and the query logs.
 * Then extract the BGP's from the queries, and bindvariables of the solution to get a set of triples of query solution
 * Then:
For each new triple consisting of subject predicate object, we:
1) create triple: subject connectRelation uniqueAugmentedSubjectID 
2) create triple: object connectRelation uniqueAugmentedObjectID
3) create triple: uniqueAugmentedSubjectID predicate uniqueAugmentedObjectID

Explanation: the extracted BGPs from the querries may have multiple entries for the same subject pred obj (or one of those)
1) With connectRelation, we couple the original subject with a unique "copy" of subject for each entry in the BGPfile
2) We do the same for the object
3) And then we connect the copies of subject and object with the original relation

The output is then the augmented graph in ntriples format
 *
 */

public class extractSolutionAndAugmentGraphFromTripleStore {
    // load the graph (only one time)
	// then for each querylog, run code to extract BGP
	// Right now a example dataset is used
	// For the "real" dataset, I would have to import the DPPedia graph 
	// and all the cleaned query logs from the DAS5 server
    public static int uniqueID = 0; // define an uniqueID so for every triple of resultString we get an unique set of 3 new triples
	public static void main(String[] args) throws IOException {	
		// here we load the model from a triple store
    	HTTPRepository repository = new HTTPRepository("http://192.168.68.101:7200/repositories/DBPedia-2016-10");
    	RepositoryConnection connection = repository.getConnection();
		// Iterate over all query logs
		File path = new File("query_logs/");
	    File [] files = path.listFiles();

	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile()){ 
	            System.out.println("Parsing through " + files[i] + " to get query solution triples..." );
	            extractBGP(files[i], connection);
	        }
	    } 	 

	    System.out.println("Finished.");	  
	    connection.close();
	}

	private static void extractBGP (File queryfile, RepositoryConnection connection) throws FileNotFoundException {	
		// query file  as input to this program with graph model
		// file has one SELECT query for each line
	    String query_str = ReadFile(queryfile);
		String[] parts = query_str.split("SELECT");
		for (int i = 1; i < parts.length; i++){	
			Query q = QueryFactory.create("SELECT " + parts[i]);
			System.out.println(q);
			Op op = Algebra.compile(q);
			System.out.println(op);
		    PrintWriter out = new PrintWriter("ResultTriples/" + i +  ".nt"); //write triple file for each query
		    System.out.println("hoioi");
			op.visit(new AllBGPOpVisitor() {
				public final void visit(OpBGP opBGP) {
					// get each basic graph pattern (BGP) from the query.
					Query query = OpAsQuery.asQuery(opBGP);	
					System.out.println(query);;
					// Execute the query on our graph, for each result we bind variables to unique BGP; 
					// which we then all add together
		            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
		                    query.toString());           
		            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();		           

		            while (tupleQueryResult.hasNext()) {
		            			BindingSet bindingSet = tupleQueryResult.next();
								bindVariables(bindingSet, opBGP.getPattern(), out);	
							}
		            tupleQueryResult.close();
				}
			});
    	    out.close(); 
    	    
    	    // convert file to zip
    	    try {
    	    byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream("ResultTriples/" + i +  ".zip.");
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File("ResultTriples/" + i +  ".nt");
            FileInputStream fis = new FileInputStream(srcFile);
            zos.putNextEntry(new ZipEntry(srcFile.getName()));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            // close the InputStream
            fis.close();
            // close the ZipOutputStream
            zos.close();
    	    }
    	    catch (IOException ioe) {
                System.out.println("Error creating zip file" + ioe);
            }
            File srcFile2 = new File("ResultTriples/" + i +  ".nt");
            if (srcFile2.delete()) { 
                System.out.println("Deleted the file: " + srcFile2.getName());
              } else {
                System.out.println("Failed to delete the file.");
              } 
		}	
	}
		
	private static String ReadFile(File queryfile) {
	    String str_data = "";
	    try {
	      Scanner myReader = new Scanner(queryfile);
	      while (myReader.hasNextLine()) {
	        String data = myReader.nextLine();
	        str_data += data;
	      }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	    return str_data;
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
	    	String uniqueSubjID = "<" + subj + "_" + uniqueID + ">";
		    String uniqueObjID = "<"  + obj + "_" + uniqueID + ">";
		    System.out.println("triple result number" + uniqueID);
		    ++uniqueID;

	    	out.println("<" + subj + ">" + " " + connectRelURI + " " +  uniqueSubjID + " .") ;
	    	out.println(uniqueSubjID + " " + "<" + pred + ">" + " " +  uniqueObjID + " .") ;
	    	out.println(uniqueObjID + " " + connectRelURI + " " + "<" + obj + ">" + " .") ;	
			}  
		}
}