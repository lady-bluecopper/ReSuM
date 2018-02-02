# ReSuM


## Overview

ReSuM is a framework to mine relevant patterns from large weighted and multi-weighted graphs.
Assuming that the importance of a pattern is determined not only by its frequency in the graph, but also by the edge weights of its appearances, we propose four scoring functions to compute the relevance of the patterns.
These functions satisfy the apriori property, and thus can rely on efficient pruning strategies.

The framework includes an exact and an approximate mining algorithm. 
The first is characterized by intelligent storage and computation of the pattern scores, while the second is based on the aggregation of similar weighting functions to allow scalability and avoid redundant computations. 





## Content

	run.sh ...........
	config.cfg ...............
	ReSuM/ ...................
	generate_weights.py ......


## Requirements

	Java JRE v1.8.0

## Input Format

### Graph File
This file contains the information about the nodes and the edges in the graph, and must comply with the following format:

	v <node_id> <node_label>
	v <node_id> <node_label>
	...
	e <node_id1> <node_id2> <edge_label>
	e <node_id1> <node_id2> <edge_label>
	...

In particular, it must first list all the graph nodes in ascending order of id, and then all the edges. 
Lines corresponding to nodes start with the letter *v*, while those corresponding to edges start with the letter *e*. 
ReSuM currently supports only double values as labels, and integer values as node and edge ids.

The datasets we used in our experiments are available on Google Drive at 
[this](https://drive.google.com/open?id=1SWrP48ENRV3QdIZN1l9yYSlOi_Nx6DR-) link.

### Weight File
This file contains the sets of edge weights for the input graph. Each line corresponds to a set of weights, one per graph edge, and must be a space-separated list of double values. 
Notice that the value at position *i* in the list is treated as the weight of the edge at position *i* in the graph file.

The weight file can be generated using the script *generate_weights.py* included in this package. 
To use the script you need to run the following command:

    python generate_weights.py <graph_file> <num_weight_sets> <output_file> -d <dispersion> -a <alpha> -b <beta>

This command generates *num*_*weight*_*sets* weights for each edge in *graph*_*file*, following a beta distribution with coefficients *alpha* and *beta*, and a dispersion parameter *dispersion* on the percentage of edges weighted for each edge label.
The result is written in *output*_*file*.


## Usage
You can use ReSuM either by using the script *run.sh* included in this package or by running the following commands. 

### Using the Script

1. **Input**: the name of the graph file must end with the extension *.lg*, while the name of the weight file must follow the pattern <*graph*_*file*>_<*dispersion>.w*, where *dispersion* indicates the percentage of graph edges weighted for each edge label. Both files must be in the same folder. 

2. **Settings**: the value of each parameter used by ReSuM must be set in the configuration file *config.cfg*. In particular:
 * General settings:
    * input_data: path to the folder that contains the graph file and the weight file.
    * output_data: path to the folder where the results will be saved.
    * tasks: space-separated list of the scoring functions to use to mine the weighted patterns (1 = ALL, 2 = ANY, 3 = SUM, 4 = AVG). The code will run once for each scoring function.
    * rels: space-separated list of relevance thresholds to use. The code will run once for each threshold value.
    * funcs: space-separated list of numbers of weight sets to test. The code will run once for each number of sets.
    * disp: dispersion value, as indicated in the name of the weight file.
    * multirun: number of runs for each configuration of the parameters.
 * Dataset-related settings:
    * Dataset names: filenames of the datasets to test.
    * Default values: comma-separated list of default values and information about the graph, i.e., default frequency threshold, default relevance threshold, is-directed, num of buckets to use in the clustering step, has-labels-on-the-nodes
    * Frequencies: comma-separated list of frequency thresholds to test
    * Experimental flags: test to perform among (1) test many frequency thresholds, (2) test many relevance thresholds, (3) test many scoring functions, (4) compute clustering, (5) run ReSuM-approximate.
  * ReSuM-approximate settings:
    * clustType: clustering strategy to use for the aggregation of the weighting functions. The values allowed are *bucket* and *full*, which correspond to the bucket-based and the full-vector strategy, respectively. This strategy is used to create the feature vectors of the weighting functions.
    * buck: number of buckets to use to create the feature vectors, when the clustering type is *bucket*.
    * smart: if true, the seed set used by the clustering algorithm is initialized by selecting the most diverse feature vectors. If false, the vectors in the seed set are chosen randomly. 
    
3. **Declaration of the datasets**: the arrays that store the names, the frequencies, and the experimental flags of each dataset to test must be declared at the beginning of the script *run.sh*. For example in the script *run.sh* you can write:
        
        declare -A datasets
        datasets[$amazon_db]=$amazon_defaults
        declare -A test_freqs
        test_freqs[$amazon_db]=$amazon_freqs
        declare -A flags
        flags[$amazon_db]=$amazon_flags

  while in the configuration file *config.cfg*:

        amazon_db='amazon'
        amazon_defaults=130,0.0001,true,10,false
        amazon_freqs=190,180,140,133,120,115
        amazon_flags='0,0,1,0,0' 

### Running the Commands

1. **Run ReSuM**:

        java -cp ReSuM.jar:lib/* sa.edu.kaust.grami.dijkstra.Main caching=true automorphism=true filename=<dataset_name> weightFile=<weight_file> disp=<dispersion> freq=<frequency_threshold> rel=<relevance_threshold> multipleRuns=<num_runs> task=<scoring_function> functions=<num_weights_per_edge> structureSize=<num_weights_per_edge>  ignoreLabels=<true_if_graph_has_no_node_labels> datasetFolder=<input_folder> outputFolder=<output_folder>
			

2. **Run ReSuM-approximate**:

        java -cp ReSuM.jar:lib/* eu.unitn.disi.db.resum.clustering.KGraphGen filename=<dataset_name> weightFile=<weight_file> buckets=<num_buckets> disp=<dispersion> clusteringType=<bucket_or_full> functions=<num_weights_per_edge> structureSize=<num_clusters> smart=<initialization_is_not_random> ignoreLabels=<true_if_graph_has_no_node_labels> datasetFolder=<input_folder> outputFolder=<output_folder>
        java -cp ReSuM.jar:lib/* sa.edu.kaust.grami.dijkstra.Main caching=true automorphism=true filename=<dataset_name> weightFile=<dataset_name>_<dispersion>_<num_clusters>.cw disp=<dispersion> freq=<frequency_threshold> rel=<relevance_threshold> task=<scoring_function> multipleRuns=<num_runs> functions=<num_weights_per_edge> structureSize=<num_clusters> clusteringType=<bucket_or_full> buckets=<num_buckets> ignoreLabels=<true_if_graph_has_no_node_labels> datasetFolder=<input_folder> outputFolder=<output_folder>
