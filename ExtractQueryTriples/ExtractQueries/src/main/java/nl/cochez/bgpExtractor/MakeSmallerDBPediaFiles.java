package nl.cochez.bgpExtractor;
import java.io.*;  
import java.util.Scanner;  
public class MakeSmallerDBPediaFiles {
	public static void main(String args[])  
	{  
	 try{  
	  // Reading file and getting no. of files to be generated  
	File path = new File("biep/");
	File [] files = path.listFiles();
	for (int h = 0; h < files.length; h++){
		   if (files[h].isFile()){ 
			   String inputfile = "biep/" + files[h].getName();
		      System.out.println(inputfile);
	  double nol = 800000.0; //  No. of lines to be split and saved in each output file.  
	  File file = new File(inputfile);  
	  Scanner scanner = new Scanner(file);  
	  int count = 0;  
	  while (scanner.hasNextLine())   
	  {  
	   scanner.nextLine();  
	   count++;  
	  }  
	  System.out.println("Lines in the file: " + count);     // Displays no. of lines in the input file.  
	  scanner.close();
	  double temp = (count/nol);  
	  int temp1=(int)temp;  
	  int nof=0;  
	  if(temp1==temp)  
	  {  
	   nof=temp1;  
	  }  
	  else  
	  {  
	   nof=temp1+1;  
	  }  
	  System.out.println("No. of files to be generated :"+nof); // Displays no. of files to be generated.  

	  //---------------------------------------------------------------------------------------------------------  

	  // Actual splitting of file into smaller files  

	  FileInputStream fstream = new FileInputStream(inputfile); DataInputStream in = new DataInputStream(fstream);  

	  BufferedReader br = new BufferedReader(new InputStreamReader(in)); String strLine;  

	  for (int j=1;j<=nof;j++)  
	  {  
	   FileWriter fstream1 = new FileWriter("selectQueriesBACKUP/"+  files[h].getName() + "-" +  j);     // Destination File Location  
	   BufferedWriter out = new BufferedWriter(fstream1);   
	   for (int i=1;i<=nol;i++)  
	   {  
	    strLine = br.readLine();   
	    if (strLine!= null)  
	    {  
	     out.write(strLine);   
	     if(i!=nol)  
	     {  
	      out.newLine();  
	     }  
	    }  
	   }  
	   out.close();  
	  }  

	  in.close();  
	 }}}catch (Exception e)  
	 {  
	  System.err.println("Error: " + e.getMessage());  
	 }  

	}  

	}   