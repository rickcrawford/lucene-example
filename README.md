Lucene Tika example project
==========================

This is a sample project for loading full text of documents into lucene using Tika.

There are 2 classes, `SearchIndex.java` and `WriteIndex.java`. For this project I included
a script to create a ramdisk for the index, which will disappear once you disconnect or restart. 
The `INDEX_DIRECTORY` constant has the location of the search index.

WriteIndex.java writes the index to the directory. Metadata stores the document metadata, ContentHandler 
stores the text content. The parser loads the file based on the content type. Tika understands how to pull
the content form many different file types.

```java
	Metadata metadata = new Metadata();
	ContentHandler handler = new BodyContentHandler();
	ParseContext context = new ParseContext();
	Parser parser = new AutoDetectParser();
	InputStream stream = new FileInputStream(file);
	try {
		parser.parse(stream, handler, metadata, context);
	}
	catch (TikaException e) {
		e.printStackTrace();
	} catch (SAXException e) {
		e.printStackTrace();
	}
	finally {
		stream.close();
	}
```

SearchIndex.java searches the index. 

```java
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
```
