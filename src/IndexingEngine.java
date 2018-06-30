import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;


public class IndexingEngine {
	/**
	 * Initializes Indexing Engine
	 * @throws Exception
	 */
	IndexingEngine() throws Exception {
		this._index = new RAMDirectory();

		_normalizingStrings.put("-", " ");
	}

	/**
	 * indexes the given documents
	 * @throws Exception
	 */
	public void addToIndex(String documentsFilePath) throws Exception {
		indexDocuments(CsvParser.parse(documentsFilePath));
	}

	public void addToIndex(List<String[]> documents) throws Exception {
		indexDocuments(documents);
	}
	
	/**
	 * retrieves the created index
	 * @throws IOException 
	 */
	public Directory getIndex() throws IOException {
		return this._index;
	}

	/**
	 * normalizing method used for both documents and queries
	 * @param input - the string to normalize
	 * @return the normalized string
	 * @throws IOException
	 */
	public String normalizeString(String input) throws IOException {
		input = input.toLowerCase();
		for (Map.Entry<String, String> e : this._normalizingStrings.entrySet()) {
			input = input.replaceAll(e.getKey(), e.getValue());
		}
		return input;
	}

	/**
	 * performs the indexing
	 * @param documents - an array of documents
	 * @throws IOException
	 */
	private void indexDocuments(List<String[]> documents) throws IOException {
		IndexWriter indexWriter = new IndexWriter(this._index, new IndexWriterConfig(new StandardAnalyzer()));
		for (String[] document : documents) {
			String documentContent = normalizeString(document[Config.CONTENT_FIELD]);
			String documentTitle = normalizeString(document[Config.TITLE_FIELD]);
			Document d = new Document();
			FieldType f = new FieldType();
			f.setStoreTermVectors(true);
			f.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			Field content = new Field(LuceneConstants.CONTENTS, documentContent, f);
			Field title = new Field(LuceneConstants.TITLE, documentTitle, f);
			Field documentClass = new Field(LuceneConstants.CLASS, document[1], f);
			d.add(content);
			d.add(title);
			d.add(documentClass);
			indexWriter.addDocument(d);
		}
		indexWriter.close();
	}

	private Directory _index;
	protected HashMap<String, String> _normalizingStrings = new HashMap<>();
}
