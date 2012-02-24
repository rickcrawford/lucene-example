/*
 * Copyright 2012 Rick Crawford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * sample application that writes to a file store
 *   run ./ramdisk.sh to create a ram disk on OSX
 *   
 * @author rick crawford
 *
 */
public class WriteIndex {

	public static final String INDEX_DIRECTORY = "/Volumes/ramdisk/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		File docs = new File("documents");
		File indexDir = new File(INDEX_DIRECTORY);
		
		Directory directory = FSDirectory.open(indexDir);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		IndexWriter writer = new IndexWriter(directory, conf);
		writer.deleteAll();
		
		for (File file : docs.listFiles()) {
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
			
			String text = handler.toString();
			String fileName = file.getName();		
			
			Document doc = new Document();
			doc.add(new Field("file", fileName, Store.YES, Index.NO));
			
			
			for (String key : metadata.names()) {
				String name = key.toLowerCase();
				String value = metadata.get(key);
				
				if (StringUtils.isBlank(value)) {
					continue;
				}
				
				if ("keywords".equalsIgnoreCase(key)) {
					for (String keyword : value.split(",?(\\s+)")) {
						doc.add(new Field(name, keyword, Store.YES, Index.NOT_ANALYZED));
					}
				}
				else if ("title".equalsIgnoreCase(key)) {
					doc.add(new Field(name, value, Store.YES, Index.ANALYZED));
				}
				else {
					doc.add(new Field(name, fileName, Store.YES, Index.NOT_ANALYZED));
				}
			}
			doc.add(new Field("text", text, Store.NO, Index.ANALYZED));
			writer.addDocument(doc);
			
		}
		
		writer.commit();
		writer.deleteUnusedFiles();
		
		System.out.println(writer.maxDoc() + " documents written");
	}

}
