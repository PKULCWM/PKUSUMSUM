package code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
 * args[7]:Specify which redundancy removal method to use. ILP and Submodular needn't extra redundancy removal. default = 3 for ManifoldRank, default = 1 for the other methods which need redundancy removal
 * 		1: MMR
 *		2: threshold: If the similarity between an unchosen sentence and the sentence chosen this time is upper than the threshold, this unchosen sentence will be deleted from candidate set.
 *		3: sum punishment: The scores of all unchosen sentences will decrease the product of penalty ratio and the similarity with the sentence chosen this time.
 * args[8]:The parameter of redundancy removal methods. default = 0.7
 *		For MMR and sum punishment: it represents the penalty ratio. 
 *		For threshold: it represents the threshold.
 * args[9]:[0, 1] A scaling factor of sentence length when we choose sentences. default = 0.1
 * args[10]:[0, 1] If the similarity of two sentence is greater than this parameter they can link. default = 0.1
 * */

public class LexPageRank {
	public Doc myDoc = new Doc();
    public int sumNum = 0;
    public int[][] linkOrNot;
    public int[] C;
    
    public void Summarize(String args[]) throws IOException
    {
    	if(args[3].equals("3")){
			System.out.println("The LexPageRank method can't solve topic-based multi-document summarization task.");
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
            myDoc.readfile(myfile.list(),args[0],args[2], args[6]);
        }
    	
    	/* Calculate sentences' similarity matrix and construct link or not matrix depending on the similarity matrix */
    	myDoc.calcTfidf(Integer.parseInt(args[3]), Integer.parseInt(args[5]));
    	myDoc.calcSim();
    	C = new int[myDoc.snum];
    	linkOrNot = new int[myDoc.snum][myDoc.snum];
    	double linkThresh = 0.1;
    	if (Double.parseDouble(args[10])>=0){
    		linkThresh = Double.parseDouble(args[10]);
        }
    	for (int i = 0; i < myDoc.snum; ++i) {
    		C[i] = 0;
    		for(int j = 0; j < myDoc.snum; ++j) {
    			if (j == i) linkOrNot[i][j] = 0;
    			else if(100 * myDoc.sim[i][j] >= linkThresh) {
    				C[i]++;
    				linkOrNot[i][j] = 1;
    			}else {
    				linkOrNot[i][j] = 0;
    			}
    		}
    	}
    	
    	/* Calculate the lexPageRank score of sentences */
		double[] uOld = new double[myDoc.snum];
		double[] u = new double[myDoc.snum];
		for(int i = 0; i < myDoc.snum; ++i) {
			uOld[i] = 1;
			u[i] = 1;
		}
		
		double eps = 0.00001, alpha = 0.85 , minus = 1.0;
		
		while (minus > eps) {
			uOld = u;
			for (int i = 0; i < myDoc.snum; ++i) {
				double nowSum = 0.0;
			    for(int j = 0; j < myDoc.snum; ++j) {
			    	if(linkOrNot[i][j] == 1) {
			    		nowSum = nowSum + uOld[j] / (1.0 * C[j]);
			    	}
			    }
			    u[i] = (1 - alpha) + alpha * nowSum;
			}
			minus = 0.0;
			for (int i = 0; i < myDoc.snum; i++) {
				double add = java.lang.Math.abs(u[i] - uOld[i]);
				minus += add;
			}
		}
    	
		/* Set redundancy removal method and parameter */
		double threshold = 0.7, Beta = 0.1;
		
		if (Double.parseDouble(args[8])>=0){
			threshold = Double.parseDouble(args[8]);
        }
		
		if (Double.parseDouble(args[9])>=0){
			Beta = Double.parseDouble(args[9]);
        }
	
		/* Remove redundancy and get the abstract */
		if (args[7].equals("-1"))
			myDoc.pickSentenceMMR(u, threshold, Beta);
        if (args[7].equals("1"))
            myDoc.pickSentenceMMR(u, threshold, Beta);
        else
        if (args[7].equals("2"))
            myDoc.pickSentenceThreshold(u, threshold, Beta);
        else
        if (args[7].equals("3"))
            myDoc.pickSentenceSumpun(u, threshold);
        
        /* Output the abstract */
    	try{
    		File outfile = new File(args[1]);
    		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile),"utf-8");
    		BufferedWriter writer = new BufferedWriter(write);
    		for (int i : myDoc.summaryId){
    			writer.write(myDoc.originalSen.get(i));
    			writer.write("\n");
            }
    		writer.close();
    	}
    	catch(Exception e){
    		System.out.println("There are errors in the output.");
    		e.printStackTrace();
    	}
    }
}
