# clean the data
def worldcup_data():
    import rdflib
    from collections import defaultdict
    
    g = rdflib.Graph()
    g.parse('data/worldcup_data',format="n3")

    def cleanRDF(s): 
        if isinstance(s, rdflib.URIRef):
            s=s.toPython().split('/')[-1]
            if '#' in s:
                s=s.split('#')[-1]
            return s
        elif isinstance(s, rdflib.Literal):
            return s.toPython()
        else:
            pass

    relations=['age','hasPosition','isMemberOf'] # many-to-one cases
    data=[]
    for h, r, t in g:
        h,r,t=list(map(cleanRDF,[h,r,t]))
        if r in relations:
            if r=='age':
                r='isAgeOf'
            data.append((h,r,t))
        #if r=='member' and 'Stage' not in h and 'team' not in h:# group vs team, one-to-many 
        #    data.append((h,'groupMember',t))
        
    entityDic={k:n for n,k in enumerate(set([j for i in data for j in [i[0],i[-1]]]))}
    relationDic={k:n for n,k in enumerate(set([i[1] for i in data]))}
    entityDic_={k:n for n,k in entityDic.items()}
    relationDic_={k:n for n,k in relationDic.items()}
    types={}
    types_=defaultdict(lambda:[])
    for h, r, t in g:
        h,r,t=list(map(cleanRDF,[h,r,t]))
        if r=='type' and h in entityDic:
            if t=='Group':
                if 'team' in h:
                    types[h]='Team'
                    types_['Team'].append(h)
                else:
                    continue
            else:
                types[h]=t
                types_[t].append(h)
    for i in entityDic:
        if i not in types:
            if isinstance(i,int):
                types[i]='age'
                types_['age'].append(i)
            else:
                types[i]='position'
                types_['position'].append(i)
    
    return data, entityDic, entityDic_, relationDic, relationDic_