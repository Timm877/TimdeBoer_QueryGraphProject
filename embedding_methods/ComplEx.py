from copy import deepcopy
import numpy as np
import random
import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim

def ComplEx(data, entityDic, relationDic):

    class ComplEx(nn.Module):
        """
        Architechure for modelling anti-symmetric relations (which in real space
        settings can overflow parameters and poses difficulties in generalizations)
        using complex embedding vectors for entities and relations and using hermi-
        tian dot product which stores information of anti-symmetric relations.
        """
        def __init__(self, num_entities, num_relations, num_dim):
            super(ComplEx, self).__init__()
            self.num_dim = num_dim
            self.num_entities = num_entities
            self.num_relations = num_relations
            self.entity_embedding_r = nn.Embedding(num_entities, num_dim)  # entity embedding real part
            self.entity_embedding_i = nn.Embedding(num_entities, num_dim)  # entity embedding imaginary part
            self.relation_embedding_r = nn.Embedding(num_relations, num_dim)  # relation embedding real part
            self.relation_embedding_i = nn.Embedding(num_relations, num_dim) # relation embedding imaginary part
            self.sigmoid = nn.Sigmoid()
            self.softplus = nn.Softplus()

        def forward(self, x, y, r):
            error = self.softplus(-(torch.sum(self.relation_embedding_r(r) * self.entity_embedding_r(x) * self.entity_embedding_r(y), 1) \
					   + torch.sum(self.relation_embedding_r(r) * self.entity_embedding_i(x) * self.entity_embedding_i(y), 1) \
					   + torch.sum(self.relation_embedding_i(r) * self.entity_embedding_r(x) * self.entity_embedding_i(y), 1) \
					   - torch.sum(self.relation_embedding_i(r) * self.entity_embedding_i(x) * self.entity_embedding_r(y), 1)) ).mean()
            return error

    def getBatchList(tripleList, num_batches):
        batchSize = len(tripleList) // num_batches
        batchList = [0] * num_batches
        for i in range(num_batches - 1):
            batchList[i] = tripleList[i * batchSize : (i + 1) * batchSize]
        batchList[num_batches - 1] = tripleList[(num_batches - 1) * batchSize : ]
        return batchList

    entity_size= len(entityDic)
    relation_size= len(relationDic)
    embedding_size= 2
    triples = [(entityDic[i],relationDic[j],entityDic[k]) for i,j,k in data]
    nBatch=10
    trainBatchList=getBatchList(triples, nBatch)
    complexx = ComplEx(entity_size, relation_size, embedding_size)

    # training
    num_epochs = 1000
    learning_rate = 0.001
    optimizer = optim.Adam(complexx.parameters(), lr=learning_rate)
    Loss=[]
    posEnData={}
    posReData={}
    for epo in range(num_epochs):
        total_loss = 0
        for batchList in trainBatchList:
            optimizer.zero_grad()
            pos_h_batch, pos_r_batch, pos_t_batch = list(zip(*batchList))
            pos_h_batch = torch.LongTensor(pos_h_batch)
            pos_t_batch = torch.LongTensor(pos_t_batch)
            pos_r_batch = torch.LongTensor(pos_r_batch)
            
            loss = complexx(pos_h_batch, pos_t_batch, pos_r_batch)
        
            total_loss += loss.item()
            loss.backward()
            optimizer.step()
        if epo % 100 == 0:    
            print(f'Loss at epo {epo}: {total_loss/len(triples)}')# print average loss within an epoch
        posEnData[epo]=deepcopy(complexx.entity_embedding_r.weight.data.numpy())
        posReData[epo]=deepcopy(complexx.relation_embedding_r.weight.data.numpy())
        Loss.append(total_loss/len(triples))

    return Loss, posEnData, posReData