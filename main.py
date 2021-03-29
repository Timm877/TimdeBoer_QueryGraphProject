"""The program is ran from this file. The requirements are checked first.
As default, a test program is ran using transE with a small world cup dataset.
To change the embedding method or dataset, run ...."""

from data_cleaning import worldcup_data
from embedding_methods import transE

data, entityDic, entityDic_, relationDic, relationDic_ = worldcup_data()
Loss, posEnData, posReData = transE(data, entityDic, relationDic)




