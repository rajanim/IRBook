# IRBook
Chapter wise algorithms and concepts .
Course also offered at San Francisco State University, CSC 849, Advisor: Prof. Kulkarni.


Part 1
-------
Simple inverted search index that performs boolean search (AND or OR)
  - Buffer read documents
  - Apply regex and perform standard tokenization on text & do case normalization.
  - Filter terms by linguistic preprocessing(Kstem) and stopwords
  - Create inverted index[term-document map] , write index to disc.

Input : part1_resource/docs.txt
Search Queries: part1_resource/queries.txt
Output : part1_resource/index.txt
GenerateIndex.java, Code to apply regex standard tokenization on text
SearchWithConjunctiveQuery.java, implements algorithm 1.3 (from text) for the intersection of two postings lists p1 and p2.


Part 2
--------
Inverted index with term positions and performs ranked retrieval search by applying TF-IDF
Input : part2_resource/input.txt
Search Queries: part2_resource/queries.txt
Output : part2_resource/index.txt
GenerateIndexWithPositions.java : Creates a positional inverted index.
SearchQueries.java Evaluates following queries, evaluates proximity operator queries such as n(t1, t2) with free terms, Example query: 2(laptop 15inch) fix repair and
applies TF-IDF scoring to matched documents and returns results sorted by score.


Part 3
--------
Applies Pseudo relevance feedback (as explained in chapter chapter 9.1.6), i) fetching top scored terms from matched documents and adding these terms to query, and other approach of fetching synonyms for query terms and adding them to search query.
Evaluates the above simple search engine (as explained in chapter 8) by comparing with similar other search engine and evaluates it with Cohen's Kappa. It also generates required qrels file for Trec Evaluation. [queries tested are in queries.txt]
Input : part3_resource/docs.txt
Output qrels : part3_resource/s
ComputeKappa.java - computes kohen's kappa
Other *.java files together form a simple search engine that creates inverted indexes and implements search functionalities as is in part 1 and 2. It is also inclusive of PRF.

