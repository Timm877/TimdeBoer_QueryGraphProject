This folder contains code written in Java to extract the triples from the queries requested by users.  
A connection with a triple store is made, then each query is simplified and the BGPs are extracted.  
For each of the triples resulting from the BGPs, an unique detour consisting of 3 triples is made.  
Finally, these detour triples are added to the triple store.  

We would like to thank OpenLink Software for hosting the http://dbpedia.org/sparql platform and providing us with log files to analyse.