package testing;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

/**
 * Author: Timm877
 * A class to test GraphDB connection for a local (embedded) GraphDB database (no networking needed).
 */
public class TestGraphDB {
	    public void hello() throws Exception {
	        // Open connection to a new temporary repository
	        // (ruleset is irrelevant for this example)
	    	HTTPRepository repository = new HTTPRepository("http://192.168.68.101:7200/repositories/DBPedia-2016-10");

	    	RepositoryConnection connection = repository.getConnection();

	        try {
	            // Preparing a SELECT query for later evaluation
	            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,
	                    "SELECT ?x WHERE {" +
	                            "BIND('Hello world!' as ?x)" +
	                            "}");

	            // Evaluating a prepared query returns an iterator-like object
	            // that can be traversed with the methods hasNext() and next()
	            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
	            while (tupleQueryResult.hasNext()) {
	                // Each result is represented by a BindingSet, which corresponds to a result row
	                BindingSet bindingSet = tupleQueryResult.next();

	                // Each BindingSet contains one or more Bindings
	                for (Binding binding : bindingSet) {
	                	System.out.println(binding);
	                    // Each Binding contains the variable name and the value for this result row
	                    String name = binding.getName();
	                    Value value = binding.getValue();

	                    System.out.println(name + " = " + value);
	                }

	                // Bindings can also be accessed explicitly by variable name
	                //Binding binding = bindingSet.getBinding("x");
	            }

	            // Once we are done with a particular result we need to close it
	            tupleQueryResult.close();

	            // Doing more with the same connection object
	            // ...
	        } finally {
	            // It is best to close the connection in a finally block
	            connection.close();
	        }
	    }

	    public static void main(String[] args) throws Exception {
	        new TestGraphDB().hello();
	    }
	}

