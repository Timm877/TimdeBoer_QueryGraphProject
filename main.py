"""The program is ran from this file. The requirements are checked first.
As default, a test program is ran using transE with a small world cup dataset.
To change the embedding method or dataset, run ...."""

from data_cleaning import worldcup_data
from embedding_methods.transE import transE
from embedding_methods.ComplEx import ComplEx

if __name__ == "__main__":

   #load the dataset
   data, entityDic, entityDic_, relationDic, relationDic_ = worldcup_data()

   #evt. add the query logs

   # each embedding model
   Loss, posEnData, posReData = transE(data, entityDic, relationDic)
   #Loss, posEnData, posReData = ComplEx(data, entityDic, relationDic)

