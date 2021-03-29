# TRANS-E
def transE(data, entityDic, relationDic):
    from copy import deepcopy
    import numpy as np
    import random

    import torch
    import torch.nn as nn
    import torch.nn.functional as F
    import torch.optim as optim

    '''initialize the transE method as pytorch nn module'''
    class transE(nn.Module):
        def __init__(self, entity_size, relation_size, embedding_size):
            super(transE, self).__init__()

            self.W_en = nn.Embedding(entity_size, embedding_size)
            self.W_re = nn.Embedding(relation_size, embedding_size)

        def forward(self, pos_h, pos_r,  pos_t, neg_h, neg_r, neg_t): 
            pos_h_e = self.W_en(pos_h)
            pos_t_e = self.W_en(pos_t)
            pos_r_e = self.W_re(pos_r)
            neg_h_e = self.W_en(neg_h)
            neg_t_e = self.W_en(neg_t)
            neg_r_e = self.W_re(neg_r)
                
            posError = torch.sum((pos_h_e + pos_r_e - pos_t_e) ** 2)
            negError = torch.sum((neg_h_e + neg_r_e - neg_t_e) ** 2)
            return posError

    # Split the tripleList into batches 
    def getBatchList(tripleList, num_batches):
        batchSize = len(tripleList) // num_batches
        batchList = [0] * num_batches
        for i in range(num_batches - 1):
            batchList[i] = tripleList[i * batchSize : (i + 1) * batchSize]
        batchList[num_batches - 1] = tripleList[(num_batches - 1) * batchSize : ]
        return batchList

    # randomly generate negative samples by corrupting head or tail with equal probabilities,
    # without checking whether false negative samples exist.
    def getBatch(tripleList, entity_size):
        #import random
        newTripleList = [corrupt_head(triple, entity_size) if random.random() < 0.5 
                        else corrupt_tail(triple, entity_size) for triple in tripleList]
        ph, pt ,pr = list(zip(*tripleList))
        nh, nt, nr = list(zip(*newTripleList))
        return ph, pt, pr, nh, nt, nr

    def corrupt_head(triple, entity_size):
        return (np.random.randint(entity_size),triple[1],triple[2])

    def corrupt_tail(triple, entity_size):
        return (triple[0],triple[1],np.random.randint(entity_size))

    entity_size=len(entityDic)
    relation_size=len(relationDic)
    embedding_size=5
    #triples=list(zip(range(9),[0]*10,range(1,10)))
    triples= [(entityDic[i],relationDic[j],entityDic[k]) for i,j,k in data]
    nBatch=10
    trainBatchList=getBatchList(triples, nBatch)
    transe = transE(entity_size, relation_size, embedding_size)

    # training
    num_epochs = 1000
    learning_rate = 0.001
    optimizer = optim.Adam(transe.parameters(), lr=learning_rate)
    Loss=[]
    posEnData={}
    posReData={}
    for epo in range(num_epochs):
        total_loss = 0
        for batchList in trainBatchList:
            optimizer.zero_grad()
            pos_h_batch, pos_r_batch, pos_t_batch, neg_h_batch,neg_r_batch, neg_t_batch=getBatch(batchList,entity_size)
            pos_h_batch = torch.LongTensor(pos_h_batch)
            pos_t_batch = torch.LongTensor(pos_t_batch)
            pos_r_batch = torch.LongTensor(pos_r_batch)
            neg_h_batch = torch.LongTensor(neg_h_batch)
            neg_t_batch = torch.LongTensor(neg_t_batch)
            neg_r_batch = torch.LongTensor(neg_r_batch)
            
            loss = transe(pos_h_batch, pos_r_batch, pos_t_batch, neg_h_batch,neg_r_batch, neg_t_batch)
        
            total_loss += loss.item()
            #transe.W_in.weight.data[0][0]=0# fix the position of "9"
            #transe.W_in.weight.data[1][0]=0#
            loss.backward()
            optimizer.step()
        if epo % 100 == 0:    
            print(f'Loss at epo {epo}: {total_loss/len(triples)}')# print average loss within an epoch
        posEnData[epo]=deepcopy(transe.W_en.weight.data.numpy())
        posReData[epo]=deepcopy(transe.W_re.weight.data.numpy())
        Loss.append(total_loss/len(triples))

    return Loss, posEnData, posReData
