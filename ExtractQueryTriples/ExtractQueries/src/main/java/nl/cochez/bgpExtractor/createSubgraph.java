package nl.cochez.bgpExtractor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

// this function makes a connection white a triple store, then reads in the entities we want to have in our subgraph
// this this entity plus two hop-neigbours are added to the subgraph via construct queries
public class createSubgraph {
	public static void main(String[] args) throws IOException {	
	int count = 0;	
	File entities = new File("entities_evaluation_clean_joepie.txt");
	Scanner scanner = new Scanner(entities);
	
	//HTTPRepository repository = new HTTPRepository("http://192.168.178.119:7200/repositories/DBPedia-2016-10");
    HTTPRepository repository = new HTTPRepository("http://10.141.255.254:7200/repositories/DBPedia-2016-10");
    RepositoryConnection connection = repository.getConnection();	
    //HTTPRepository repository2 = new HTTPRepository("http://192.168.178.119:7200/repositories/Subgraph");
    HTTPRepository repository2 = new HTTPRepository("http://10.141.255.254:7200/repositories/Subgraph");
    RepositoryConnection connection2 = repository2.getConnection();	  

	while (scanner.hasNextLine())   {  
		count++;
		String entity = scanner.nextLine();
	    PrintWriter hop = new PrintWriter("subgraphquery.txt"); // keep track of results of query 
		hop.println(count);
		hop.close();
		String construct = "CONSTRUCT {    ?s1 ?p1 ?o1 .    ?o1 ?p2 ?entitiyInt .    ?entitiyInt ?p3 ?o3 .    ?o3 ?p4 ?o4 }   WHERE {    VALUES ?entitiyInt { " + entity + "}    OPTIONAL {    OPTIONAL {?s1 ?p1 ?o1 .}    ?o1 ?p2 ?entitiyInt .}    OPTIONAL {?entitiyInt ?p3 ?o3 .OPTIONAL {?o3 ?p4 ?o4}}}"; 					

		GraphQuery query = connection.prepareGraphQuery(construct);
		GraphQueryResult rs = query.evaluate();
		PrintWriter results = new PrintWriter("results.nt");
		while (rs.hasNext()) {
            Statement statement = rs.next();
            results.println(statement);
        }
		results.close();
		File srcFile = new File("results.nt");
        connection2.add(srcFile);   
	}
	scanner.close();
	connection2.close();
	connection.close();
	}
}