import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * Sample application for searching an index
 * @author rick crawford
 *
 */
public class SearchIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		
		File indexDir = new File(WriteIndex.INDEX_DIRECTORY);
		
		Directory index = FSDirectory.open(indexDir);

        // Build a Query object
		Query query;
		try {
			query = new QueryParser(Version.LUCENE_35, "text", new StandardAnalyzer(Version.LUCENE_35)).parse("rick crawford");
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
        
		int hitsPerPage = 10;
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);
		
		System.out.println("total hits: " + collector.getTotalHits());		
		
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		for (ScoreDoc hit : hits) {
			Document doc = reader.document(hit.doc);
			System.out.println(doc.get("file") + "  (" + hit.score + ")");
		}
		
	}

}
