package E5_ver1;


import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.tika.exception.TikaException;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

public class LuceneTester{
	
	//���� ��ť��Ʈ�� �ִ� ������ �ε����� ����� ����, ����η� ���߿� �������־�� ��.
	String indexDir = "C:\\Users\\user\\Documents\\lucene_example\\index";
	String dataDir = "C:\\Users\\user\\Documents\\lucene_example\\sample_document";
	
	Indexer indexer;
	Searcher searcher;
	static WriteFile WriteFile;
	private static TcpIpServer tcpIpServer;
	static String ForSend_string;
	static JSONObject ForSend_json;
	
	public static void main(String[] args) throws ParseException, SAXException, TikaException{
		LuceneTester tester;
		try{
			tester = new LuceneTester();
			WriteFile = new WriteFile();
			Scanner scanner = new Scanner(System.in);
			//�� machine�� JVM heap size�� ���. indexing�� �ϴٰ� OutOfMemoryError�� �߻��� �� �ֱ� ����.
			long heapSize = Runtime.getRuntime().totalMemory() / (1024 * 1024);
			System.out.println("Heap Size : " + heapSize + "MB");
			
			System.out.print("Index(y/n):");
			String IndexOrNot = scanner.nextLine();
			if(IndexOrNot.compareTo("y")==0){
				tester.createIndex();
			}
			scanner.close();
			//Setup tcpip server for query from C# front-end
			tcpIpServer = new TcpIpServer();
			tcpIpServer.setup(LuceneConstants.PORT);
			tcpIpServer.accept();
			//������ �ϳ� �ö����� �ش��ϴ� �˻��� �����ϴ� ����.
			while(true){
				String input = "";  // Query from c#
				ForSend_string = "[Search Result]\r\n"; // ���������� ��������� �ƴ��ϳ�, ������ �˻��� �Ϸ�Ǿ��ٴ� ť�� �ֱ� ���� ����
				ForSend_json = new JSONObject();
				
				System.out.print("search query: ");
				input = tcpIpServer.read();
				//input = input.substring(1);
				System.out.println(input);
				/********���� �˻��ÿ� ���� �Ǵ� ��ġ*************************/
				if((input.substring(0, 1).compareTo("m"))==0){
					//System.out.println("Main Search!" + input.substring(0, 1) +"  " + input.substring(1));
					tester.search(input.substring(1)); // search ����
				}
				/********���� �˻��ÿ� ����Ǵ� ��ġ*************************/
				if((input.substring(0, 1).compareTo("s"))==0){
					//System.out.println("Sub Search!" + input.substring(0, 1) +"  " + input.substring(1));
					tester.sub_search(input.substring(1)); // sub-search ����
				}
				//�˻� ����� out.txt�� json format���� ����. tcp������ ť�� �����ִ� �뵵
				WriteFile.write(ForSend_json.toJSONString());
				tcpIpServer.write(ForSend_string);
				
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/////////////////////////////////////////////////////
	// ���� ��ť��Ʈ�� ���� �ε����� �����ϴ� �Լ�.
	private void createIndex() throws IOException, SAXException, TikaException{
		indexer = new Indexer(indexDir);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + "File indexed, time taken: " + (endTime-startTime)+" ms");
	}
	
	// �־��� ������ ���� search�� �����ϴ� �Լ�. recursion search�� algorithm�� ���� �κ�!
	@SuppressWarnings({ "unchecked", "resource" })
	private void search(String searchQuery) throws IOException, ParseException{
		searcher = new Searcher(indexDir);
		
		int numSearched = 0; // ã���� �� ���� ��
		//int i = 0; // for index number

		
		//Document doc_1st = new Document(); // ���� ��ġ���� ù��°�� ���ϵ� ����, ���� �˻���� �������� ���ٰ� �����Ǵ� ����.
		
		ArrayList<String> doc_list = new ArrayList<String>();
		
		TopDocs hits;
		Document doc;
		
		long startTime = System.currentTimeMillis();
		
		// ��� �˻� ����� �������� ForSend_json�� json���·� �����ؼ� out.txt�� ����ȴ�.
		//* ���� ������ ���� �˻� *//
		JSONObject main_query = new JSONObject();
		hits = searcher.search(searchQuery);
		numSearched += hits.totalHits;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Analyzer analyzer = new KoreanAnalyzer();
		
		for(ScoreDoc scoreDoc : hits.scoreDocs){
			//i += 1;
			doc = searcher.getDocument(scoreDoc);
			// �˻��� ��ť��Ʈ�� �ּҸ� ����. �߰������� C#���� ã�ƿ��� ��???
			doc_list.add(doc.get(LuceneConstants.FILE_PATH));
			// map���ٰ� analyze�� ���� ����鿡�� ���� �־ �ܾ���� ��� ��� ���Դ����� ����Ʈ����� ���������.
			//System.out.println(doc.get(LuceneConstants.FILE_PATH));
			String filename = doc.get(LuceneConstants.FILE_PATH);
			int start = filename.lastIndexOf('\\');
			int fine = filename.lastIndexOf('.');
			filename = filename.substring(start + 1, fine);
			//System.out.println(filename);
			TokenStream stream = analyzer.tokenStream("map", new StringReader(filename));
			//OffsetAttribute offsetAttribute = stream.getAttribute(OffsetAttribute.class);
			@SuppressWarnings("deprecation")
			TermAttribute termAttribute = stream.getAttribute(TermAttribute.class);
			
			while(stream.incrementToken()){
				//int startOffset = offsetAttribute.startOffset();
				//int endOffset = offsetAttribute.endOffset();
				String term = termAttribute.term();
				//System.out.println("term: " + term);
				if(map.containsKey(term)){
					map.put(term, map.get(term)+1);
				}
				else{
					map.put(term, 1);
				}
			}
				
			
		}
		//System.out.println(map);
		LinkedHashMap lmap = sortHashMapByValuesD(map);
		//System.out.println(lmap);
		//System.out.println(lmap.keySet().toArray()[lmap.size()-1]);
		
		ArrayList<String> term_list = new ArrayList<String>();
		for(int i=0; i<lmap.size(); i++){
			if(i==LuceneConstants.SUGGESTION_NUM) break;
			term_list.add(lmap.keySet().toArray()[lmap.size()-i-1].toString());
		}
		System.out.println(term_list);
		main_query.put("result", doc_list.toString());
		main_query.put("numSearched", hits.totalHits);
		main_query.put("suggestion_keyword", term_list.toString());
		ForSend_json.put("main_query", main_query);
		doc_list.clear();
		

		long endTime = System.currentTimeMillis();
		System.out.println(numSearched+" documents found. Time: "+(endTime - startTime));
		

	}
	
	@SuppressWarnings("unchecked")
	private void sub_search(String searchQuery) throws IOException, ParseException
	{
		searcher = new Searcher(indexDir);
		
		int numSearched = 0; // ã���� �� ���� ��
		ArrayList<String> doc_list = new ArrayList<String>();
		
		TopDocs hits;
		Document doc;
		
		long startTime = System.currentTimeMillis();
		// ��� �˻� ����� �������� ForSend_json�� json���·� �����ؼ� out.txt�� ����ȴ�.
		//* ���� ������ ���� �˻� *//
		JSONObject sub_query = new JSONObject();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Analyzer analyzer = new KoreanAnalyzer();
		
		
		//hits = searcher.search(searchQuery);
		hits = searcher.content_search(searchQuery);
		numSearched += hits.totalHits;
		for(ScoreDoc scoreDoc : hits.scoreDocs){
			doc = searcher.getDocument(scoreDoc);
			// �˻��� ��ť��Ʈ�� �ּҸ� ����. �߰������� C#���� ã�ƿ��� ��???
			doc_list.add(doc.get(LuceneConstants.FILE_PATH));
			
			String filename = doc.get(LuceneConstants.FILE_PATH);
			int start = filename.lastIndexOf('\\');
			int fine = filename.lastIndexOf('.');
			filename = filename.substring(start + 1, fine);
			//System.out.println(filename);
			TokenStream stream = analyzer.tokenStream("map", new StringReader(filename));
			//OffsetAttribute offsetAttribute = stream.getAttribute(OffsetAttribute.class);
			@SuppressWarnings("deprecation")
			TermAttribute termAttribute = stream.getAttribute(TermAttribute.class);
			
			while(stream.incrementToken()){
				//int startOffset = offsetAttribute.startOffset();
				//int endOffset = offsetAttribute.endOffset();
				String term = termAttribute.term();
				//System.out.println("term: " + term);
				if(map.containsKey(term)){
					map.put(term, map.get(term)+1);
				}
				else{
					map.put(term, 1);
				}
			}
		}
		
		//System.out.println(map);
		LinkedHashMap lmap = sortHashMapByValuesD(map);
		//System.out.println(lmap);
		//System.out.println(lmap.keySet().toArray()[lmap.size()-1]);
		
		ArrayList<String> term_list = new ArrayList<String>();
		for(int i=0; i<lmap.size(); i++){
			if(i==LuceneConstants.SUGGESTION_NUM) break;
			term_list.add(lmap.keySet().toArray()[lmap.size()-i-1].toString());
		}
		System.out.println(term_list);
		
		sub_query.put("suggestion_keyword", term_list.toString());	
		sub_query.put("result", doc_list.toString());
		sub_query.put("numSearched", hits.totalHits);
		ForSend_json.put("sub_query", sub_query);
		doc_list.clear();
		
		long endTime = System.currentTimeMillis();
		System.out.println(numSearched+" documents found. Time: "+(endTime - startTime));
		
	}
		
	public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
		   List mapKeys = new ArrayList(passedMap.keySet());
		   List mapValues = new ArrayList(passedMap.values());
		   Collections.sort(mapValues);
		   Collections.sort(mapKeys);

		   LinkedHashMap sortedMap = new LinkedHashMap();

		   Iterator valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = passedMap.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((String)key, (int)val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}
	
	
}