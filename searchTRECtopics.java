import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class searchTRECtopics {
	static String indexPath="C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\default";
	static int id=51;
	String output="C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\output\\Result_Task2.txt";
static	HashMap<String,Double> docScore; 
	
public static void main(String[] args) throws IOException,NullPointerException, ParseException {
	String topicsPath="C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\topics.51-100";
	String titlequery = "C:\\Users\\Manasa\\Desktop\\titlequeryresults.txt";
	String descquery = "C:\\Users\\Manasa\\Desktop\\descqueryresults.txt";
	File file=new File(topicsPath);
	String read = FileUtils.readFileToString(file);
	String titledesc[]=StringUtils.substringsBetween(read, "<title>","<desc>");
	appendFile(titlequery);
	for(String title:titledesc){
	title=StringUtils.chop(title);
	title=StringUtils.lowerCase(title);
	title=title.trim().replaceAll("[\\t\\n\\r]"," ").replaceAll("-", " ").replaceAll("topic:", "").replaceAll("[^a-zA-Z 0-9]","").trim();
	@SuppressWarnings("unused")
	double relevancescore_title=searchIndex(title,true,titlequery);
	}
	appendFile(descquery);
	String descs[]=StringUtils.substringsBetween(read, "<desc>","<smry>") ;
	for(String desc:descs){
		System.out.println(desc);
		desc=StringUtils.chop(desc);
		desc=StringUtils.lowerCase(desc);
		desc=desc.trim().replaceAll("[\\t\\n\\r]"," ").replaceAll("-"," ").replaceAll("description:","").replaceAll("[^a-zA-Z 0-9]","").trim();
		@SuppressWarnings("unused")
		double relevancescore_desc=searchIndex(desc,true,descquery);
	}
	
}
public static double RelevanceScore(String queryString,Query query,HashMap<String,Double> docScoreInformation,boolean flag) throws IOException{
	 double relevancescore=0;
	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
	int totalCount=reader.maxDoc();
	double FreqMap[]=new double[totalCount];
	double DocLengMap[]=new double[totalCount];
	IndexSearcher searcher = new IndexSearcher(reader);
	TotalHitCountCollector collect = new TotalHitCountCollector();
	searcher.search(query,collect);
	int DocswithTerm=collect.getTotalHits();
	DefaultSimilarity dSimi=new DefaultSimilarity();
	List<AtomicReaderContext> leafContexts = reader.getContext().reader().leaves();
	double IDF;
	if(DocswithTerm == 0){
		IDF=0;
	}
	else{
	IDF=Math.log(1+ (totalCount / DocswithTerm));
	}
	int counter2=0;
	for (int i = 0; i < leafContexts.size(); i++) {
		AtomicReaderContext leafContext=leafContexts.get(i);
		int firstDoc=leafContext.docBase;
		int noOfDoc=leafContext.reader().maxDoc();
		 for (int docId = firstDoc; docId < firstDoc+noOfDoc; docId++) {
			 double normalLen=dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId-firstDoc));
			 DocLengMap[counter2]=normalLen;
			 counter2++;
		 }
		 @SuppressWarnings("unused")
		int doc;	 
		
		 DocsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),MultiFields.getLiveDocs(leafContext.reader()),"TEXT", new BytesRef(queryString));
		 if(de!=null){
		 while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
			 FreqMap[de.docID()+firstDoc]=de.freq(); 
		 }
		}
		 
	}
		 Terms vocabulary= MultiFields.getTerms(reader, "DOCNO");
	     TermsEnum iterator= vocabulary.iterator(null);
	     BytesRef byteRef = null;
		 int counter=0;
		 while((byteRef = iterator.next()) != null) {
			 double docrelevancescore=0;
			 double doclength=DocLengMap[counter];
			 docrelevancescore =  (FreqMap[counter]/doclength) * IDF;
			 if(flag){
				String docno= byteRef.utf8ToString();
			 if(!docScore.containsKey(docno)){
				 docScore.put(docno, docrelevancescore);
			 }
			 else{
				 double temp= docScore.get(docno) +docrelevancescore;
				 docScore.put(docno, temp);
			 }
			 
			}
			 counter++;
			 relevancescore += docrelevancescore;
		 } 
		
		 return relevancescore;
}
public static void WriteTopDocs(int id, HashMap<String,Double> docScore,String opfile){
	String result="";
	int count=1;
List<Entry<String,Double>> hash =  new LinkedList<Entry<String,Double>>(docScore.entrySet());
	
	Collections.sort(hash,new Comparator<Entry<String,Double>>(){
			public int compare(Entry<String,Double> e1,Entry<String,Double> e2){
				return e2.getValue().compareTo(e1.getValue());
			}
	});
	Map<String,Double> Edges = new LinkedHashMap<String,Double>();
	boolean status=false;
	for(Entry<String, Double> entry:hash){
		Edges.put(entry.getKey(), entry.getValue());
	}
	  for(Map.Entry<String,Double> entry: Edges.entrySet()) {
		  if(count>1000) 
			  break;	  
	    result = id + "\t" +"Q0"+ "\t" + entry.getKey() + "\t"+ count + "\t"+ entry.getValue() +"\t"+ "run_1"+"\n";	
	    try{
			FileWriter filewriter = new FileWriter(opfile,true);
			BufferedWriter bufferwriter = new BufferedWriter(filewriter);
	        bufferwriter.write(result);
	        bufferwriter.newLine();
	        bufferwriter.flush();
	        bufferwriter.close();
	        filewriter.close();      
	        status=true;
	}
		catch(IOException e){
   		e.printStackTrace();
   	}
	    if(!status){
			System.out.println("\n Results could not be written to file. Check again. ");
			System.exit(1);
		}
	    count++;
	}	
}

public static double searchIndex(String queryString,boolean flag,String filename) throws ParseException, IOException{
	docScore=new HashMap<String,Double>();
	Analyzer analyzer = new StandardAnalyzer();
	QueryParser parser = new QueryParser("TEXT", analyzer);
	Query query = parser.parse(queryString);
	Set<Term> queryTerms = new LinkedHashSet<Term>();
	query.extractTerms(queryTerms);
	double revelanceScore=0;
	for (Term t : queryTerms) {
	query = parser.parse(t.text());
	double tempScore=RelevanceScore(t.text(),query,docScore,flag);
	revelanceScore += tempScore;
	}
	if(flag){
	if(id>100) id=51;
	WriteTopDocs(id,docScore,filename);
	id++;
	
	}
	return revelanceScore;
 }

public void ReqTopDocs(String querystring ,Similarity similarityobj) throws ParseException, IOException{
	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
	IndexSearcher searcher = new IndexSearcher(reader);
	searcher.setSimilarity(similarityobj); 
	Analyzer analyzer = new StandardAnalyzer();
	QueryParser parser = new QueryParser("TEXT", analyzer);
	Query query = parser.parse(querystring);
	TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
	searcher.search(query, collector);
	ScoreDoc[] docs = collector.topDocs().scoreDocs;
	for (int i = 0; i < docs.length; i++) {
			@SuppressWarnings("unused")
			Document doc = searcher.doc(docs[i].doc);
	}
	reader.close();
}
public static boolean appendFile(String path){
	boolean status=false;
	try{
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
		status = f.createNewFile();
	}
	catch(Exception e){
		 e.printStackTrace();
	}
	
	return status;
}
}