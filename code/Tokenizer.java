package code;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

public class Tokenizer {
    public ArrayList<String> passage=new ArrayList<String>();
    public ArrayList<Integer> senLen=new ArrayList<Integer>();
    public ArrayList<String> sentence=new ArrayList<String>();
    public ArrayList<ArrayList<String>> word=new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> stemmerWord=new ArrayList<ArrayList<String>>();
    HashMap<String,Integer> stopword=new HashMap<String,Integer>();
    public void readStopwords(String stopwordPath) throws IOException
    {	
		File tmpfile =new File(stopwordPath);
		if (!tmpfile.exists()){
			System.out.println("stopwords file does not exist!");
			System.exit(0);
		}
    	FileReader inFReader=new FileReader(stopwordPath);
        BufferedReader inBReader=new BufferedReader(inFReader);
        String tmpWord;
        int i=0;
        while((tmpWord=inBReader.readLine())!=null)
        {
            i++;
            stopword.put(tmpWord, i);
        }
        inBReader.close();
    }
	
    public boolean ifWords_Eng(String tmpWord)
    {
        if (tmpWord.charAt(0)>='A' && tmpWord.charAt(0)<='Z') return true;
        if (tmpWord.charAt(0)>='a' && tmpWord.charAt(0)<='z') return true;
        return false;
    }
    public boolean ifStopwords(String tmpWord)
    {
        if (stopword.get(tmpWord.toLowerCase())!=null) return true;
        return false;
    }

    public void stemmerWord() {
    	int numOfWord = word.size();
    	for(int i = 0; i < numOfWord; ++i) {
    		ArrayList<String> stemmerW = new ArrayList<String>();
    		for(int j = 0; j < word.get(i).size(); ++j) {
    			Stemmer stemmer = new Stemmer();
        		int letterNumOfWord = word.get(i).get(j).length();
        		for(int k = 0; k < letterNumOfWord; ++k) {
        			stemmer.add(word.get(i).get(j).charAt(k));
        		}
        		stemmer.stem();
        		String tmpW = stemmer.toString();
        		stemmerW.add(tmpW);
    		}
    		stemmerWord.add(stemmerW);
    	}
    }
    
    public ArrayList<String> tokenize_Eng(String inFile, String stopwordPath) throws IOException
    {
        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(inFile),
                new CoreLabelTokenFactory(), "");
        int len=0;
        int wlen=0;
		if (stopwordPath.equals("y"))
			readStopwords("stopword_Eng");
		else
		if (!stopwordPath.equals("n"))
			readStopwords(stopwordPath);
        String token,tmpSen;
        tmpSen=new String();
        boolean ifend=false;
        while (ptbt.hasNext())
        {
            CoreLabel label = ptbt.next();
            token=label.toString();

            if (ifend==false)
            {

                if (token.equals(".") || token.equals("?") ||token.equals("!"))
                {
                    ifend=true;
                }
                if (token.equals("-LRB-") || token.equals("-RRB-") || token.equals("-LCB-")|| token.equals("-RCB-") || token.equals("\""))
                    continue;
                if (token.equals("'") || token.equals("`") || token.equals("''") || token.equals("``") || token.equals("_") || token.equals("--") || token.equals("-")){
                    continue;
                }
                if (token.equals("'s") || token.equals(".") || token.equals("?") || token.equals("!") || token.equals(",") || token.equals("'re") || (token.equals("'ve")))
                    tmpSen+=token;
                else
                tmpSen+=" "+token;

                if (ifWords_Eng(token))
                    wlen++;
                if (!token.equals("'s"))
                    len++;
                if (ifWords_Eng(token) && !ifStopwords(token))
                    sentence.add(token.toLowerCase());
            }else
            {
                if (token.equals("'") || token.equals("`") || token.equals("''") || token.equals("``") || token.equals(" ")){
                    continue;
                }
                if (token.equals("."))
                {

                    tmpSen+=token;
                    len++;
                }else
                {
                    if (len>1 && wlen*2>=len) {
                        passage.add(tmpSen);
                        senLen.add(len);
                        word.add(sentence);
                    }
                    ifend=false;
                    tmpSen=token;
                    sentence=new ArrayList<String>();
                    wlen=0;
                    if (ifWords_Eng(token))
                        wlen++;
                    len=1;
                    if (ifWords_Eng(token) && !ifStopwords(token))
                        sentence.add(token.toLowerCase());
                }
            }
        }
        if (ifend && len>1 && wlen*2>=len)
        {
            passage.add(tmpSen);
            word.add(sentence);
            senLen.add(len);
        }
        stemmerWord();
        return passage;
    }

    public ArrayList<String> tokenize_Chn(String inFile, String stopwordPath) throws IOException
    {
        StringBuffer buffer=new StringBuffer();
        String line; 
		if (!stopwordPath.equals("n") && !stopwordPath.equals("y"))
			readStopwords(stopwordPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "utf-8"));
        line = reader.readLine(); 
        while (line != null) {
            buffer.append(line);
            buffer.append("\n");
            line = reader.readLine();
        }
        reader.close();
        Pattern pattern = Pattern.compile(".*?[¡££¿£¡]");
        Matcher matcher = pattern.matcher(buffer);
        Pattern p2=Pattern.compile("[\u4e00-\u9fa5]");
        while (matcher.find()) {
            String sen=matcher.group();
            passage.add(sen);
            senLen.add(sen.length());
            List<Term> parse=ToAnalysis.parse(sen);
            ArrayList<String> tmpsen=new ArrayList<>();
            for (Term x:parse){
                Matcher m2=p2.matcher(x.getName());
                if (m2.find()) {
					if (!ifStopwords(x.getName()))
                    tmpsen.add(x.getName());
                }
            }
            word.add(tmpsen);
        }
        stemmerWord();
        return passage;
    }

}
