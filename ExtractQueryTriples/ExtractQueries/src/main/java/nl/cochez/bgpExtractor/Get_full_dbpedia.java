package nl.cochez.bgpExtractor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;


public class Get_full_dbpedia {
	public static void main(String[] args) throws IOException {
		// Load in DBPedia zipfiles as buffered reader files
		File [] zipFiles = new File("DBPedia_data/").listFiles();
		//Model model = ModelFactory.createDefaultModel();
    	
	    for (int i = 0; i < zipFiles.length; i++){
	    	if (zipFiles[i].isFile()){ 
	    		System.out.println("Parsing"+ zipFiles[i].getName());
	    		BufferedReader br2 = getBufferedReaderForCompressedFile("DBPedia_data/" + zipFiles[i].getName());
	    		String line;
	    		int j = 0;
	    		PrintWriter out = new PrintWriter("Bliepboep/" + zipFiles[i].getName() + ".nt");
	    		while ((line = br2.readLine()) != null) {	
	    			long count = line.chars().filter(ch -> ch == '"').count();
	    			long count2 = line.chars().filter(ch -> ch == '<').count();
	    			long count3 = line.chars().filter(ch -> ch == '>').count();
	    			if (count < 2 && count2 == 3 && count3 == 3) {
	    				out.println(line);
	    			} else {
	    				j = j+1;
	    				}
	    			}
	    		System.out.println("Found " + j + " literals in " + zipFiles[i].getName());
	    	    out.close();
	            br2.close();
	    		}
	    		}
	
	        }

		public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) {
		    FileInputStream fin = null;
			try {
				fin = new FileInputStream(fileIn);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		    BufferedInputStream bis = new BufferedInputStream(fin);
		    CompressorInputStream input = null;
			try {
				input = new CompressorStreamFactory().createCompressorInputStream(bis);
			} catch (CompressorException e) {
				e.printStackTrace();
			}
		    BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		    return br2;
		}
	}

