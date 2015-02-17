

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class easySearch 
{
	static Analyzer analyzer =  new   StandardAnalyzer();
	static DefaultSimilarity dSimi=new  DefaultSimilarity();
	static DecimalFormat df = new DecimalFormat("###.##");
	static List<String> IDs;
	static IndexReader  reader;
	static int numberOfDoc;
	//document TF Map
	static SortedMap<Integer, SortedMap<Term, Integer>> documentTF=new TreeMap<Integer,SortedMap<Term,Integer>>();
	//Map for document query score
	static SortedMap<Double,List<String>> documentQueryScore=new TreeMap<Double, List<String>>();
	//Map for query no. of docs
	static SortedMap<Term,Integer> queryNoDocs=new TreeMap<Term, Integer>();
	
	static NavigableSet<Double> navigableSet;
	
	static SortedMap<Integer,Float> LengthOfDocMap=new TreeMap<Integer,Float>();
	public static void main(String[] args) throws Exception
	{
		int  firstDoc;//first document index
		String QueryString="happy people";//user Query String
        //getScoresForDocs(QueryString);
		//path for index
		QueryParser  Queryparser  =  new  QueryParser("TEXT",  analyzer); 
		Query  query   =   Queryparser.parse(QueryParser.escape(QueryString));
		reader= DirectoryReader.open(FSDirectory.open(new  File("C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\default")));
		Set<Term>  queryTerms  =  new  LinkedHashSet<Term>(); 
		query.extractTerms(queryTerms);
		List<AtomicReaderContext>  leafContexts  =  reader.getContext().reader().leaves();
		numberOfDoc=reader.maxDoc();
		//for each term in Set queryTerms
		for(Term eachterm :queryTerms)
			queryNoDocs.put(eachterm,reader.docFreq(new Term("TEXT", eachterm.text())));
		//Get the segments of the index
		for (int i =  0;  i <  leafContexts.size();  i++)
		{ 
			AtomicReaderContext leafContext=leafContexts.get(i);
			firstDoc=leafContext.docBase;
			//term frequency in doc
			SortedMap<Term,Integer> TermFreqOfDoc;
			for(Term term :queryTerms)
			{
				//Get the term frequency of "people" within each document containing it for <field>TEXT</field>
				DocsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), MultiFields.getLiveDocs(leafContext.reader()),"TEXT",   new   BytesRef(term.text()));
				@SuppressWarnings("unused")
				int doc;
				//given
				while (de!=null && (doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS)
				{	
					//if the termfreq val of doc in the map not NULL
					if(documentTF.get(de.docID()+firstDoc)!=null)
						documentTF.get(de.docID()+firstDoc).put(term, de.freq());
					//else, put the term and freq val in TermFreqOfDoc and put it in documentTFMap
					else
					{
						TermFreqOfDoc=new TreeMap<Term,Integer>();
						TermFreqOfDoc.put(term, de.freq());
						documentTF.put(de.docID()+firstDoc,TermFreqOfDoc);
					}
					
					LengthOfDocMap.put(de.docID()+firstDoc,dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(de.docID())));
				}
			}
			
			navigableSet=((TreeMap<Double, List<String>>) documentQueryScore).descendingKeySet();  
			
		}
		//for each entry in LengthofDocument Map
		for(Entry<Integer, Float> entry: LengthOfDocMap.entrySet())
		{	
			//find relevance score-- key, query and length of doc key for each term in queryTerms
			double relevanceScore=0;
			for(Term term:queryTerms)
				relevanceScore=relevanceScore+Tf_Idf(term,entry.getKey(),LengthOfDocMap.get(entry.getKey()));
			
			
			if(documentQueryScore.get(relevanceScore)!=null)
				documentQueryScore.get(relevanceScore).add(reader.document(entry.getKey()).getField("DOCNO").stringValue());
			else
			{
				IDs=new ArrayList<String>();
				IDs.add(reader.document(entry.getKey()).getField("DOCNO").stringValue());
				documentQueryScore.put(relevanceScore, IDs);
			}
		}
        //writing Output to File
		File fout=new File("C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\output\\Result_Task1.txt");
		FileOutputStream f=new FileOutputStream(fout);
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(f));

		bw.write("Score \t\t\t\t\t DocNos"+"\n");
		for(Double score:navigableSet)
			bw.write(df.format(score)+" \t\t "+documentQueryScore.get(score)+"\n");
		
		bw.close();
		f.close();

	}

	//method to calculate TF*IDF
	public static double Tf_Idf(Term term, int docId,float DocLength) 
	{	
		double tF=0;
		double iTF=0;
		//System.out.println(documentTF);
		if(documentTF.get(docId).get(term)!=null)
		{
			tF = documentTF.get(docId).get(term)/DocLength;
			iTF= Math.log10(1+(numberOfDoc/queryNoDocs.get(term)));
		}
		return tF*iTF;
	}

}