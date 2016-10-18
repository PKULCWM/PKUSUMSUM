package code;

import java.io.File;

public class Run {
	
	/**
	 * ============ Necessary Parameters ============
	 * -T <type>	Specify which task to do.
	 * 		1: single-document summarization, 2: multi-document summarization,
	 * 		3: topic-based multi-document summarization
	 * -topic <topicFile>	The path of the topic file. (Only for topic-based multi-document summarization task)
	 * -input <inputPath>	The path of the input.
	 * 		Single-document summarization task: The path of the input file and this file can only contain one document you want to get the summary from.
	 * 		Multi-document summarization task or topic-based multi-document summarization task: The path of the input directory and this directory can only contain one document set you want to get the summary from.
	 * -output <outputFile>	The path of the output file and one file only contains one summary.
	 * -L <language>	The language of the document. 1: Chinese, 2: English, 3: other Western languages
	 * -n <abNum>	The expected number of words in summary.
	 * -m <method>	Specify which method to solve the problem.
	 * 		Single-document summarization task: 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular;
	 * 		Multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular, 7: ClusterCMRW;
	 * 		Topic-based multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 8: ManifoldRank.
	 * -stop <stopwordPath>	Choose whether you need remove the stop words.
	 * 		If you need remove the stop words, you should input the path of stop word list. 
	 * 		Or we have prepared an English stop words list as file ¡°stopword_Eng¡±, you can use it by input ¡°y¡±.
	 * 		If you don¡¯t need remove the stop words, please input ¡°n¡±.
	 * 
	 * ============ Optional Parameters ============
	 * [-s <stemmerOrNot>]	Choose if you want to stem the input. (Only for English document) 
	 * 		1: stem, 2: no stem, default = 1
	 * [-R <ReMethod>]	Specify which redundancy removal method to use. ILP and Submodular needn't extra redundancy removal. default = 3 for ManifoldRank, default = 1 for the other methods which need redundancy removal
	 *		1: MMR
	 *		2: threshold: If the similarity between an unchosen sentence and the sentence chosen this time is upper than the threshold, this unchosen sentence will be deleted from candidate set.
	 *		3: sum punishment: The scores of all unchosen sentences will decrease the product of penalty ratio and the similarity with the sentence chosen this time.
	 * [-p <RePara>]	The parameter of redundancy removal methods. default = 0.7
	 *		For MMR and sum punishment: it represents the penalty ratio. 
	 *		For threshold: it represents the threshold.
	 * [-beta <beta>]	[0, 1] A scaling factor of sentence length when we choose sentences. default = 0.1
	 *  [-] LexPageRank-specific parameters
	 * [-link <linkThresh>]	[0, 1] If the similarity of two sentence is greater than this parameter they can link. default = 0.1
	 *	[-] ClusterCMRW-specific parameters
	 * [-Alpha <AlphaC>]	[0, 1] The ratio controlling the expected cluster number for the document set. default = 0.1
	 * [-Lamda <LambdaC>]	[0, 1] The combination weight controlling the relative contributions from the source cluster and the destination cluster. default = 0.8
	 *	[-] Submodular-specific parameters
	 * [-sub <op>]	Type of submodular method. 
	 *		1: Li's paper (Li at el, 2012)
	 *		2: modification method from Lin's paper (Lin and Bilmes, 2010) default = 2
	 * [-A <AlphaS>]	[0, 1] Threshold coefficient. default = 0.5
	 * [-lam <LambdaS>]	[0, 1] Trade-off coefficient. 
	 *		default = 0.15 in multi-document task and default = 0.5 in single-document task
	 * 
	 **/
	
	public static String type = "-1", topic = "-1", inputPath = "-1", outputFile = "-1";
	public static String language = "-1", abNum = "-1", method = "-1", stopwordPath = "-1";
	public static String stemmerOrNot = "1", ReMethod = "1", RePara = "0.7", beta = "0.1"/*, alpha = "0.85", eps = "0.00001"*/;
	public static String linkThresh = "0.1", AlphaC = "0.1", LambdaC = "0.8", op = "2", AlphaS = "0.5", LambdaS = "-1";
	public static String[] arg = new String[13];
	
	public static void main(String[] args) throws Exception {
	
		for (int i = 0; i < args.length; ) {
			if (args[i].compareTo("-T") == 0) {
				type = args[++i];
				++i;
			}
			else if (args[i].compareTo("-topic") == 0) {
				topic = args[++i];
				++i;
			}
			else if (args[i].compareTo("-input") == 0) {
				inputPath = args[++i];
				++i;
			}
			else if (args[i].compareTo("-output") == 0) {
				outputFile = args[++i];
				++i;
			}
			else if (args[i].compareTo("-m") == 0) {
				method = args[++i];
				++i;
			}
			else if (args[i].compareTo("-L") == 0) {
				language = args[++i];
				++i;
			}
			else if (args[i].compareTo("-n") == 0) {
				abNum = args[++i];
				++i;
			}
			else if (args[i].compareTo("-stop") == 0) {
				stopwordPath = args[++i];
				++i;
			}
			else if (args[i].compareTo("-s") == 0) {
				stemmerOrNot = args[++i];
				++i;
			}
			else if (args[i].compareTo("-R") == 0) {
				ReMethod = args[++i];
				++i;
			}
			else if (args[i].compareTo("-p") == 0) {
				RePara = args[++i];
				++i;
			}
			else if (args[i].compareTo("-beta") == 0) {
				beta = args[++i];
				++i;
			}
			else if (args[i].compareTo("-link") == 0) {
				linkThresh = args[++i];
				++i;
			}
			else if (args[i].compareTo("-Alpha") == 0) {
				AlphaC = args[++i];
				++i;
			}
			else if (args[i].compareTo("-Lamda") == 0) {
				LambdaC = args[++i];
				++i;
			}
			else if (args[i].compareTo("-sub") == 0) {
				op = args[++i];
				++i;
			}
			else if (args[i].compareTo("-A") == 0) {
				AlphaS = args[++i];
				++i;
			}
			else if (args[i].compareTo("-lam") == 0) {
				LambdaS = args[++i];
				++i;
			}
			else {
				System.out.println("Invalid parameter!");
				return;
			}
		}
		
		/**
		 * Judge the correctness of parameters.
		 * */
		if(type.equals("-1")) {
			System.out.println("Please choose the task you want to solve, 1 for single-document, "
					+ "2 for multi-document, 3 for topic-based multi-document.");
			return;
		}
		else if(type.equals("3") && topic.equals("-1")){
			System.out.println("Please input the path of topic file.");
			return;
		}
		else if(inputPath.equals("-1")) {
			System.out.println("Please input the path of input.");
			return;
		}
		else if(outputFile.equals("-1")) {
			System.out.println("Please input the path of output file.");
			return;
		}
			
		else if(language.equals("-1")) {
			System.out.println("Please choose the language of your input.");
			return;
		}
		else if(abNum.equals("-1")) {
			System.out.println("Please input expected number of words in summary.");
			return;
		}
		else if(method.equals("-1")) {
			System.out.println("Please choose the method you want to use.");
			return;
		}
		else if(stopwordPath.equals("-1")) {
			System.out.println("Please choose whether you need to remove the stopwords.");
			return;
		}
		else {
			if(!type.equals("1") && !type.equals("2") && !type.equals("3")) {
				System.out.println("Please choose the correct task! "
						+ "1: single-document summarization, 2:multi-document summarization, 3:topic-based multi-document summarization.");
				return;
			}
			
			File input = new File(inputPath);
			if (!input.exists()) {
				System.out.println("The path of input is not correct.");
				return;
			}
			
			File output = new File(outputFile);
			File outputDir = new File(output.getParent());
			if (!outputDir.exists()) {
				System.out.println("The directory of output file does not exist.");
				return;
			}
			
			if(!language.equals("1") && !language.equals("2") && !language.equals("3")) {
				System.out.println("Please choose the correct language! "
						+ "1:Chinese, 2:English, 3:other Western languages.");
				return;
			}
			
	    	if (!isIntegerNumber(abNum) || abNum.contains("-")) {
	    		System.out.println("The expected number of words in summary should be an integer.");
	    		return;
	    	}
	    	
	    	if(!stemmerOrNot.equals("1") && !stemmerOrNot.equals("2")) {
				System.out.println("Please choose if you want to stem the input! "
						+ "1: stem, 2:no stem.");
				return;
			}
	    	
	    	if(!stopwordPath.equals("y") && !stopwordPath.equals("n")){
	    		File stop = new File(stopwordPath);
	    		if(!stop.exists()) {
	    			System.out.println("Specify whether to remove the stopwords."
	    					+ "If you need to remove the stop words, you should provide the stopword list and specify the path of the stop word file. "
	    					+ "Note that we have prepared an English stopword list in the file \"/lib/stopword_Eng\", you can use it by input \"y\"."
	    					+ "If you don¡¯t need to remove the stop words, please input \"n\".");
					return;
	    		}
	    	}
	    	
	    	if(!isIntegerNumber(method) || Integer.parseInt(method)< 0 || Integer.parseInt(method)> 8) {
	    		System.out.println("Specify which method is used to solve the problem."
	    				+ "For single-document summarization: 1 - Lead, 2 - Centroid, 3 - ILP, 4 - LexPageRank, 5 -TextRank, 6 - Submodular;"
	    				+ "For multi-document summarization: 0 - Coverage, 1 - Lead, 2 - Centroid, 3 - ILP, 4 - LexPageRank, 5 - TextRank, 6 - Submodular, 7 - ClusterCMRW;"
	    				+ "For topic-based multi-document summarization: 0 - Coverage, 1 - Lead, 2 - Centroid, 8 - ManifoldRank.");
				return;
	    	}
	    	
	    	if(!ReMethod.equals("1") && !ReMethod.equals("2") && !ReMethod.equals("3")) {
	    		System.out.println("Specify a valid redundancy removal method! "
	    				+ "1 ¨C MMR-based method;"
	    				+ "2 ¨C Threshold-based method: if the maximum similarity between an unselected sentence and the already selected sentences"
	    				+ "is larger than a predefined threshold, this unselected sentence will be removed."
	    				+ "3 ¨C Penalty imposing method: after a summary sentence is selected, the score of each unselected sentence will be penalized by subtracting "
	    				+ "the product of a predefined penalty ratio and the similarity between the unselected sentence and the summary sentence.");
				return;
	    	}
	    	
	    	if(!isIntegerNumber(RePara) && !isFloatPointNumber(RePara)) {
	    		System.out.println("Specify the internal parameter of the redundancy removal methods with \"-p\". It should be a number and the default value is 0.7. "
	    				+ "For MMR and Penalty imposing method, it specifies the penalty ratio. "
	    				+ "For threshold-based method, it specifies the threshold value.");
				return;
	    	}
	    	
	    	if((!isIntegerNumber(beta) && !isFloatPointNumber(beta)) || Double.parseDouble(beta) > 1.0 || Double.parseDouble(beta) < 0.0) {
	    		System.out.println("Specify the scaling factor of sentence length when we choose sentences with \"-beta\", and its range is [0, 1].");
				return;
	    	}
	    	
	    	if((!isIntegerNumber(linkThresh) && !isFloatPointNumber(linkThresh)) || Double.parseDouble(linkThresh) > 1.0 || Double.parseDouble(linkThresh) < 0.0) {
	    		System.out.println("Specify the similarity threshold for linking two sentences with \"-link\". Its range is [0, 1] and the default value is 0.1.");
				return;
	    	}
	    	
	    	if((!isIntegerNumber(AlphaC) && !isFloatPointNumber(AlphaC)) || Double.parseDouble(AlphaC) > 1.0 || Double.parseDouble(AlphaC) < 0.0) {
	    		System.out.println("Specify the ratio for controlling the expected cluster number of the document set with \"-Alpha\". Its range is [0, 1] and has a default value of 0.1.");
				return;
	    	}
	    	
	    	if((!isIntegerNumber(LambdaC) && !isFloatPointNumber(LambdaC)) || Double.parseDouble(LambdaC) > 1.0 || Double.parseDouble(LambdaC) < 0.0) {
	    		System.out.println("Specify the combination weight for controlling the relative contributions from the source cluster and the destination cluster with \"-Lamda\"."
	    				+ "Its range is [0, 1] and has a default value of 0.8.");
				return;
	    	}
	    	
	    	if(!op.equals("1") && !op.equals("2")) {
	    		System.out.println("Please specify the type of the submodular method with \"-sub\"."
	    				+ "1:a method in Li's paper (Li at el, 2012);"
	    				+ "2:a modification method from Lin's paper (Lin and Bilmes, 2010)");
				return;
	    	}
	    	
	    	if((!isIntegerNumber(AlphaS) && !isFloatPointNumber(AlphaS)) || Double.parseDouble(AlphaS) > 1.0 || Double.parseDouble(AlphaS) < 0.0) {
	    		System.out.println("Specify the threshold coefficient with \"-A\". The range is [0, 1] and the default value is 0.5.");
				return;
	    	}
	    	
	    	if(!LambdaS.equals("-1")) {
	    		if((!isIntegerNumber(LambdaS) && !isFloatPointNumber(LambdaS)) || Double.parseDouble(LambdaS) > 1.0 || Double.parseDouble(LambdaS) < 0.0) {
		    		System.out.println("Specify the trade-off coefficient with \"-lam\". "
		    				+ "The range is [0, 1] and the default value is 0.15 for multi-document summarization and 0.5 for single-document summarization.");
					return;
		    	}
	    	}
	    	
			arg[0] = inputPath;
			arg[1] = outputFile;
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			
			
			/**
			 *  Solve the problem
			 *  Single-document summarization task: 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular;
			 * 	Multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular, 7: ClusterCMRW;
			 * 	Topic-based multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 8: ManifoldRank.
			 **/
			switch (method){
				case "0":{
					Coverage coverage = new Coverage();
					coverage.Summarize(arg);
					break;
				}
				case "1":{
					Lead lead = new Lead();
					lead.Summarize(arg);
					break;
				}
				case "2":{
					MEAD mead = new MEAD();
					arg[7] = ReMethod;
					arg[8] = RePara;
					arg[9] = beta;
					arg[10] = topic;
					mead.Summarize(arg);
					break;
				}
				case "3":{
					ILP ilp = new ILP();
		 			ilp.Summarize(arg);
		 			break;
				}
				case "4":{
					LexPageRank lexpagerank = new LexPageRank();
					arg[7] = ReMethod;
					arg[8] = RePara;
					arg[9] = beta;
					arg[10] = linkThresh;
		 			lexpagerank.Summarize(arg);
		 			break;
				}
				case "5":{
					TextRank textrank = new TextRank();
					arg[7] = ReMethod;
					arg[8] = RePara;
					arg[9] = beta;
					textrank.Summarize(arg);
					break;
				}
				case "6":{
					Submodular submodular = new Submodular();
					arg[7] = op;
					arg[8] = beta;
					arg[9] = AlphaS;
					arg[10] = LambdaS;
					submodular.Summarize(arg);
					break;
				}
				
				case "7":{
					ClusterCMRW clusterCMRW = new ClusterCMRW();
					arg[7] = ReMethod;
					arg[8] = RePara;
					arg[9] = beta;
					arg[10] = AlphaC;
					arg[11] = LambdaC;
					clusterCMRW.Summarize(arg);
					break;
				}
				case "8":{
					ManifoldRank manifoldRank = new ManifoldRank();
					arg[7] = ReMethod;
					arg[8] = RePara;
					arg[9] = beta;
					arg[10] = topic;
		 			manifoldRank.Summarize(arg);
					break;
				}
				default:
					System.out.println("No this method!");
					break;
			}
		}		
	}
	
	public static boolean isIntegerNumber(String number){  
        number=number.trim();  
        String intNumRegex="\\-{0,1}\\d+";
        if(number.matches(intNumRegex))  
            return true;  
        else  
            return false;  
    }  
      
    public static boolean isFloatPointNumber(String number){  
        number=number.trim();  
        String pointPrefix="(\\-|\\+){0,1}\\d*\\.\\d+";
        String pointSuffix="(\\-|\\+){0,1}\\d+\\.";
        if(number.matches(pointPrefix)||number.matches(pointSuffix))  
            return true;  
        else  
            return false;  
    }  
}
