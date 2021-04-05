"""The program is ran from this file.
As default, a test program is ran using transE with a small world cup dataset.
To change the embedding method or dataset, run ...."""
import argparse
import sys
from data.worldcup import worldcup_data
from embedding_methods.transE import transE
from embedding_methods.ComplEx import ComplEx


embedding_dict = {'E1': transE,
                   'E2': ComplEx}
dataset_dict = {'D1': worldcup_data}                   

if __name__ == "__main__":
   parser = argparse.ArgumentParser()
   parser.add_argument('embedding_method',
                        help=f'Method used to embed graph data to vector representation. Options are: {embedding_dict}',
                        default='E1')
   
   parser.add_argument('dataset',
                        help=f'Dataset used. Options are: {embedding_dict}',
                        default='D1')
   # maybe add later: add or not add query's; print loss; save model yes or no

   args = parser.parse_args()

   #load the dataset
   data, entityDic, entityDic_, relationDic, relationDic_ = dataset_dict[args.dataset]()
   # I would call DataGenerator() here

   # add the query logs

   # embedding model
   # Call network.py from here
   Loss, posEnData, posReData = embedding_dict[args.embedding_method](data, entityDic, relationDic) #default: transE



