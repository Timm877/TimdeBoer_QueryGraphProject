import json
import h5py

with open("data/FB15k/entity_names_all_0.json", "rt") as tf:
    names = json.load(tf)
offset = names.index("/m/05hf_5")

with h5py.File("model/fb15k/embeddings_all_0.v8.h5", "r") as hf:
    embedding = hf["embeddings"][...]

print('amount of entities in dataset: ' + str(len(embedding)))
print('each embedding has dim of: ' + str(len(embedding[0])))