from rdflib import Graph
import bz2
from os import listdir
from os.path import isfile, join
from bs4 import BeautifulSoup
import requests
import wget

URL = "http://downloads.dbpedia.org/2016-10/links/"
r  = requests.get(URL)
data = r.text
soup = BeautifulSoup(data)

for link in soup.find_all('a')[2:]:
    datalink = URL + link.get('href')
    print(datalink)
    if datalink.endswith(".bz2"):
        wget.download(datalink, out = "DBPedia_data/")

URL = "http://downloads.dbpedia.org/2016-10/core/"
r  = requests.get(URL)
data = r.text
soup = BeautifulSoup(data)

for link in soup.find_all('a')[2:]:
    datalink = URL + link.get('href')
    print(datalink)
    if datalink.endswith(".bz2"):
        wget.download(datalink, out = "DBPedia_data/")
#these files are already downloaded but website says i should still download them:
#wget.download("http://downloads.dbpedia.org/2016-10/core-i18n/en/instance_types_lhd_dbo_en.ttl.bz2",out= "DBPedia_data/") 
#wget.download("http://downloads.dbpedia.org/2016-10/core-i18n/en/instance_types_lhd_ext_en.ttl.bz2",out= "DBPedia_data/")
