import json
import h5py
import torch
from torchbiggraph.model import ComplexDiagonalDynamicOperator, DotComparator

# Load entity count
with open("data/FB15k/entity_count_all_0.txt", "rt") as tf:
    entity_count = int(tf.read().strip())

# Load count of dynamic relations
with open("data/FB15k/dynamic_rel_count.txt", "rt") as tf:
    dynamic_rel_count = int(tf.read().strip())

# Load the operator's state dict
with h5py.File("model/fb15k/model.v8.h5", "r") as hf:
    operator_state_dict = {
        "real": torch.from_numpy(hf["model/relations/0/operator/rhs/real"][...]),
        "imag": torch.from_numpy(hf["model/relations/0/operator/rhs/imag"][...]),
    }
operator = ComplexDiagonalDynamicOperator(400, dynamic_rel_count)
operator.load_state_dict(operator_state_dict)
comparator = DotComparator()

# Load the offsets of the entities and the index of the relation type
with open("data/FB15k/entity_names_all_0.json", "rt") as tf:
    entity_names = json.load(tf)
src_entity_offset = entity_names.index("/m/0f8l9c")  # France
with open("data/FB15k/dynamic_rel_names.json", "rt") as tf:
    rel_type_names = json.load(tf)
rel_type_index = rel_type_names.index("/location/country/capital")

# Load the trained embeddings
with h5py.File("model/fb15k/embeddings_all_0.v8.h5", "r") as hf:
    src_embedding = torch.from_numpy(hf["embeddings"][src_entity_offset, :])
    dest_embeddings = torch.from_numpy(hf["embeddings"][...])

# Calculate the scores
scores, _, _ = comparator(
    comparator.prepare(src_embedding.view(1, 1, 400)).expand(1, entity_count, 400),
    comparator.prepare(
        operator(
            dest_embeddings,
            torch.tensor([rel_type_index]).expand(entity_count),
        ).view(1, entity_count, 400),
    ),
    torch.empty(1, 0, 400),  # Left-hand side negatives, not needed
    torch.empty(1, 0, 400),  # Right-hand side negatives, not needed
)

# Sort the entities by their score
permutation = scores.flatten().argsort(descending=True)
top5_entities = [entity_names[index] for index in permutation[:5]]

print(top5_entities)