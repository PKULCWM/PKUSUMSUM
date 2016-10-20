package code;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import lpsolve.*;

/**
 * args[0]:The path of the input.
 * 		Single-document summarization task: The path of the input file and this file can only contain one document you want to get the summary from.
 * 		Multi-document summarization task or topic-based multi-document summarization task: The path of the input directory and this directory can only contain one document set you want to get the summary from.
 * args[1]:The path of the output file and one file only contains one summary.
 * args[2]:The language of the document. 1: Chinese, 2: English, 3: other Western languages
 * args[3]:Specify which task to do.
 * 		1: single-document summarization, 2: multi-document summarization,
 * 		3: topic-based multi-document summarization
 * args[4]:The expected number of words in summary.
 * args[5]:Choose if you want to stem the input. (Only for English document) 
 * 		1: stem, 2: no stem, default = 1
 * args[6]:Choose whether you need remove the stop words.
 * 		If you need remove the stop words, you should input the path of stop word list. 
 *  	Or we have prepared an English stop words list as file ¡°stopword_Eng¡±, you can use it by input ¡°y¡±.
 *   	If you don¡¯t need remove the stop words, please input ¡°n¡±.
 * 
 * */

public class ILP {
	public Doc myDoc = new Doc();
	public ArrayList<HashMap<String, Double> > sentenceTfIdf;
	public HashMap<String, Double> allConcepts;
	public ArrayList<String> content;
	public double[] S, W, yesOrNo, res;
	public int[][] O;
	public int conNum;
	
	public void Summarize(String args[]) throws Exception
    {
		if(args[3].equals("3")){
			System.out.println("The ILP method can't solve topic-based multi-document summarization task.");
			return;
		}
		
    	/* Read files */
    	if (args[3].equals("1"))
        {
    		String[] singleFile = new String[1];
            singleFile[0] = args[0];
            myDoc.maxlen = Integer.parseInt(args[4]);
            myDoc.readfile(singleFile, " ", args[2], args[6]);
        }
    	else if (args[3].equals("2"))
        {
    		File myfile = new File(args[0]);
            myDoc.maxlen = Integer.parseInt(args[4]);
            myDoc.readfile(myfile.list(), args[0], args[2], args[6]);
            
        }
    	
    	/* Calculate tf*idf */    	
    	myDoc.calcTfidf(Integer.parseInt(args[3]), Integer.parseInt(args[5]));
    	myDoc.calcSim();
    	
    	/* Get S array store the length of sentences */
    	S = new double[myDoc.snum];
    	for(int i = 0; i < myDoc.snum; ++i) {
    		S[i] = (double) myDoc.senLen.get(i);
    	}
    	
    	/* Get all concepts */
		getConcepts(myDoc.sen);
		int lenST = sentenceTfIdf.size();
		allConcepts = sentenceTfIdf.get(lenST-1);
		conNum = allConcepts.size();
		
		/* Calculate W as concepts' weight */
		GetW();
		
		/* Calculate O as whether the certain concept in sentence */
		GetO();
		
		/* Solve the ILP model use lpsolve */
		yesOrNo = execute(myDoc.maxlen);
		int wordNum = 0;
		content = new ArrayList<String>();
		HashMap<Integer, Integer> choose = new HashMap<Integer,Integer>();
		for(int i = 0; i < myDoc.snum; ++i) {
			if(yesOrNo[i] == 1.0) {
				wordNum += myDoc.senLen.get(i);
				choose.put(i, i);
			}
		}
		if(wordNum <= myDoc.maxlen) {
			int num = 0;
			while(wordNum <= myDoc.maxlen && num < myDoc.snum) {
				if(!choose.containsKey(num)){
					wordNum += myDoc.senLen.get(num);
					choose.put(num, num);
				}
				num ++;
			}
		}
		Object[] keyArr = choose.keySet().toArray();
		Arrays.sort(keyArr);
		for  (Object key : keyArr) {
		    Object value = choose.get(key);
		    content.add(myDoc.originalSen.get((int)(value)));
		}
        
        /* Output the abstract */
    	try{
    		File outfile = new File(args[1]);
    		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile),"utf-8");
    		BufferedWriter writer = new BufferedWriter(write);
    		for (String i : content){
                //System.out.println(myDoc.originalSen.get(i));
    			writer.write(i);
    			writer.write("\n");
            }
    		writer.close();
    	}
    	catch(Exception e){
    		System.out.println("There are errors in the output.");
    		e.printStackTrace();
    	}
    }
	
	/* Get all concepts */
	public void getConcepts(ArrayList<ArrayList<String>> allSpaceComments){
		int sizeOfComment = allSpaceComments.size();
		
		sentenceTfIdf = new ArrayList<HashMap<String, Double> >();
		/* Calculate words' TF in sentence */		
		for(int i = 0; i < sizeOfComment; i++) {
			HashMap<String, Double> Label = new HashMap<String, Double>();
			ArrayList<String> labels = allSpaceComments.get(i);
			for (int j = 0; j < labels.size(); j++) {
				if (Label.containsKey(labels.get(j))) {
					Label.put(labels.get(j), Label.get(labels.get(j)) + 1.0 / labels.size());
				} else {				
					Label.put(labels.get(j), 1.0 / labels.size());
					
				}
			}
			sentenceTfIdf.add(Label);
		}
		
		/* Calculate words' IDF in sentence and update the words' TF*IDF in sentence */		
		for(int i = 0; i < sizeOfComment; i++) {
			Iterator<Entry<String, Double>> iter = sentenceTfIdf.get(i).entrySet().iterator();
			double IDF;
			while (iter.hasNext()) {
				IDF = 0.0;
				Entry<String, Double> entry = (Entry<String, Double>) iter.next();
				String key = (String) entry.getKey();
				for(int j = 0; j < sizeOfComment; j++) {
					HashMap<String, Double> now = sentenceTfIdf.get(j);
					if (now.containsKey(key)) { 
						IDF = IDF + 1;
					} 
				}
				double idf = Math.log(sizeOfComment / IDF);
				entry.setValue(entry.getValue() * idf);
			}
		}
		
		/* Get concepts and calculate their weight */
		HashMap<String, Double> All = new HashMap<String, Double>();
		for(int i = 0; i < sizeOfComment; i++) {
			Iterator<Entry<String, Double>> iter = sentenceTfIdf.get(i).entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Double> Now = iter.next();
				String now = Now.getKey();
				if(All.containsKey(now)){
					All.put(now, All.get(now)+1.0);
				}
				else{
					All.put(now,1.0);
				}
			}
		}
		
		/* Leave the words with tf > 5 as concepts */
		HashMap<String, Double> AllLabel = new HashMap<String, Double>();
		Iterator<Entry<String, Double>> iter = All.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Double> Now = iter.next();
			double now = Now.getValue();
			if(now > 5.0) {
				AllLabel.put(Now.getKey(),0.0);
			}
		}
		
		/* update the concepts' weight by adding the tf*idf */
		for(int i = 0; i < sizeOfComment; i++) {
			Iterator<Entry<String, Double>> iter1 = sentenceTfIdf.get(i).entrySet().iterator();
			
			while (iter1.hasNext()) {
				Entry<String, Double> Now = iter1.next();
				String now = Now.getKey();
				
				if(AllLabel.containsKey(now)){
					AllLabel.put(now, AllLabel.get(now) + Now.getValue() / sizeOfComment);
				}
			}
		}
		
		sentenceTfIdf.add(AllLabel);
	}
	
	/* Solve ILP use LpSolve */
	public double[] execute(int abNum) throws LpSolveException {
        LpSolve lp;
        
        int ret = 0;
        res = new double[myDoc.snum + conNum];

        lp = LpSolve.makeLp(0, myDoc.snum+conNum);
        
        /* We will build the model row by row
           So we start with creating a model with 0 rows and senNum+conNum columns */
        
        if(lp.getLp() == 0)
        	ret = 1; /* couldn't construct a new model... */
        int numOfCon = 0;
        
        if(ret == 0){
      	
        	/* Let us name our variables. Not required, but can be usefull for debugging */
        	for(int i = 1; i <= myDoc.snum; ++i) {
        		lp.setColName(i, "S"+i);
        		lp.setBinary(i, true);
        	}
        	for(int i = 1; i <= conNum; ++i) {
        		lp.setColName(myDoc.snum+i, "C"+i);
        		lp.setBinary(myDoc.snum+i, true);
        	}

        	lp.setAddRowmode(true);  /* Makes building the model faster if it is done rows by row */

        	/* Construct row length <= abNum */
        	int[] cols = new int[myDoc.snum];
        	numOfCon = 0;
        	for(int i = 1; i <= myDoc.snum; ++i) {
        		cols[i-1] = i; 
        		numOfCon++;
        	}
        	
        	/* Add the row to lp_solve */
        	lp.addConstraintex(numOfCon, S, cols, LpSolve.LE, abNum);
        }

        if(ret == 0){
        
        	for(int i = 0; i < conNum; ++i) {
        		int[] colno = new int[myDoc.snum + 1];
      		  	double[] row = new double[myDoc.snum + 1];
      		  	numOfCon = 0;
      		  	for(int j = 0; j < myDoc.snum; ++j) {
      		  		numOfCon ++;
      		  		colno[j] = j+1;
      		  		row[j] = (double)O[j][i];
      		  	}
      		  	numOfCon ++;
      		  	colno[myDoc.snum] = myDoc.snum + i + 1;
      		  	row[myDoc.snum] = -1.0;
      		  	/* Add the row to lp_solve */
	        	lp.addConstraintex(numOfCon, row, colno, LpSolve.GE, 0.0);
      	  	}
        }

        if(ret == 0) {
      	  
        	for(int i = 0; i < conNum; i++) {
        		for(int j = 0; j < myDoc.snum; j++) {
        			int[] colno = new int[2];
	        		double[] row = new double[2];
	        		numOfCon = 0;
	        		numOfCon ++;
	        		colno[0] = j+1;
	        		row[0] = (double)O[j][i];
	        		numOfCon ++;
	        		colno[1] = i+1+myDoc.snum;
	        		row[1] = -1.0;
		    		/* Add the row to lp_solve */
			        lp.addConstraintex(numOfCon, row, colno, LpSolve.LE, 0.0);
		    	}
		    }
       }

       if(ret == 0) {
    	   lp.setAddRowmode(false); /* Rowmode should be turned off again when done building the model */
    	   int[] colno = new int[conNum];
    	   double[] row = new double[conNum];
    	   numOfCon = 0;
		      for(int i = 0; i < conNum; i++) {
		    	  numOfCon ++;
		    	  colno[i] = i+1+myDoc.snum;
		    	  row[i] = W[i];
		      }
		      /* set the objective in lp_solve */
		      lp.setObjFnex(numOfCon, row, colno);
       }

       if(ret == 0) {
      	   /* Set the object direction to maximize */
    	   lp.setMaxim();

      	   /* I only want to see importand messages on screen while solving */
      	   lp.setVerbose(LpSolve.IMPORTANT);

      	   /* Now let lp_solve calculate a solution */
      	   ret = lp.solve();
      	   if(ret == LpSolve.OPTIMAL)
      		   ret = 0;
      	   else
      		   ret = 5;
       }

       if(ret == 0) {
           /* A solution is calculated, now lets get some results */
      	   /* Variable values */
      	   lp.getVariables(res);
      	   /*for(int j = 0; j < (senNum + conNum); j++)
      		   System.out.println(lp.getColName(j + 1) + ": " + res[j]);*/
       }

       /* Clean up such that all used memory by lp_solve is freeed */
       if(lp.getLp() != 0)
    	   lp.deleteLp();

       return res;
    }
	
	/* Calculate W as concepts' weight */	
	public void GetW(){
		
		W = new double[conNum];
		Iterator<Entry<String, Double>> iter2 = allConcepts.entrySet().iterator();
		int it = 0;
		while (iter2.hasNext()) {
			Entry<String, Double> Now = iter2.next();
			W[it++] = Now.getValue();  
		} 
	}
	
	/* Calculate O as whether the certain concept in sentence */
	public void GetO(){
		
		O = new int[myDoc.snum][conNum];
		for(int i = 0; i < myDoc.snum; i++){
			HashMap<String, Double> Label = new HashMap<String, Double>();
			Label = sentenceTfIdf.get(i);
			Iterator<Entry<String, Double>> iter1 = allConcepts.entrySet().iterator();
			int itt = 0;
			while (iter1.hasNext()) {
				Entry<String, Double> Now = iter1.next();
				if(Label.containsKey(Now.getKey())){
					O[i][itt++] = 1;
				}
				else{
					O[i][itt++] = 0;
				}
			}
		}
	}
	
}
