#!/usr/bin/env python3

import numpy as np
from collections import Counter,defaultdict
import random
import time
import sys

import click

def read_graph(graph) : 
    edges = defaultdict(int)
    num_edges = 0
    edge_position = []
    with open(graph, 'r') as f : 
        for line in f : 
            splitted = line.split(' ')
            if len(splitted)> 2 and splitted[0] == 'e' : 
                num_edges += 1
                label = int(splitted[3])
                edge_position.append(label)
                edges[label] += 1
    print('Found {} edges'.format(num_edges))
    return edges, edge_position

@click.command()
@click.argument('graph', type=click.Path(exists=True), metavar='<graph>')
@click.argument('num_users', type=click.INT, metavar='<users>')
@click.argument('outfile', type=click.STRING , metavar='<output>')
@click.option('-d','--dispersion', type=click.FLOAT, default=0.1, help='Percentage of weighted edges per edge label')
@click.option('-a','--alpha', type=click.FLOAT, default=5, help='Alpha parameter of the Beta distribution')
@click.option('-b','--beta', type=click.FLOAT, default=5, help='Beta parameter of the Beta distribution')
# Dispersion is the focus parameter with a better name
def generate_weights(graph, num_users, outfile, dispersion = 0.1, alpha = 5, beta = 5) : 
    """
        Generate a fixed number of <users> weights for each edge
        in the input <graph> following a beta distribution on 
        the weight value and a dispersion parameter on the
        percentage of edges weighted for each edge label. 

        The result is written in the <output> file. 

    """
    now = time.time()
    label_edges, edge_label = read_graph(graph)
    if dispersion <= 0 and dispersion > 1 : 
        print('ERROR: dispersion must be in (0,1] range')
        return 
    total_edges = sum(label_edges.values())
    print (total_edges)
    with open(outfile,'w') as out :
        ran_idx = np.arange(total_edges)
        for i in np.arange(num_users) : 
            user_weights = np.zeros(total_edges)
            random.shuffle(ran_idx)
            weighted_label = {}
            for l,n in label_edges.items() : 
                weighted_label[l] = max (1, round(dispersion * n)) 
            weighted = sum(weighted_label.values())
            for idx in ran_idx : 
                label = edge_label[idx]
                if weighted_label[label] : 
                    weighted_label[label] -= 1
                    user_weights[idx] = np.random.beta(alpha, beta)
                    weighted -= 1
                if weighted <= 0 : 
                    break
            out.write('{}\n'.format(" ".join(map(str, user_weights))))
            # print('.',end='')
            sys.stdout.flush()
            if ((i+1) % 100 == 0): 
                print('')
    print('Generated {} users in {}s\n'.format(num_users, time.time() - now))

if __name__ == '__main__':
    generate_weights()
