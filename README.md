# Query Graph Project
Author: Tim de Boer 

This repository contains code for a short research project for the master AI at the Vrije Unviversiteit Amsterdam.  
The initial idea for this project is that we may be able to improve performance of large scale graph embedding methods  
by adding information about importance of triples, as measured on downstream ML tasks.

## How to read this repo

- DownloadDBPedia contains code used to download the required DBPedia files from DBPedias download page (which was later uploaded to remote database)   
- ExtractQueryTriples contains the code to extract triples from BasicGraphPatterns as a result from SPARQL queries  
- GetEmbeddings contains code to run the original graph and the graph augmented with the triples from ExtractQueryTriples  
- EvaluateEmbeddings contains code the evaluate the embeddings on downstream ML tasks from the original graph and the augmented graph  

## Main Requirements

'''
Java 11 JDK with additional libraries
Apache Maven
Python 3.9.1
PyTorch
Pytorch BigGraph
'''

All requirements are visible in requirements.txt  
It is recommended to create a virtual enviroment.

## Theoretical background information

We hypothesize that results of queries requested by users via https://dbpedia.org/sparql contain information about importance,
in the sense that important information will be requested via SPARQL more often than less important information.
From the results from the SPARQL queries, a list of BasicGraphPatterns (BGPs) is extracted.
In order to do receive the BGPs we first simplify the queries to SELECT queries without filters.
Each BasicGraphPattern comes in the form of: 
'''
<subject> <relation> ?x 
'''
We extract all possible triples in the Graph which satisfy the results the BasicGraphPattern.

These triples will then be uniquely added to the graph by creating a detour of three triples, as follows:
'''
<subject> <connectionRelation> <uniqueSubjectID>
<uniqueSubjectID> <relation> <uniqueObjectID> 
<uniqueObjectId> <connectionRelation> <object>
'''
By creating an unique detour for every triple, the graph is expanded for every triple.
The count of detour for a triple in the original graph gives an indication of importance.
The graph embedding methods will therefore assign greater importance to these triples, since wrong embeddings for these triples will bem ore costly than triples with less detours.

For more information, the reader is referred to [paper]
