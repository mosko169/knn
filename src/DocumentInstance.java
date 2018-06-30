import java.util.List;

/**
 * represents a single document, its tf-idf vector and its true label
 */
public class DocumentInstance {
	public DocumentInstance(int docId, String label, List<Double> tfidfVector) {
		this.docId = docId;
		this.label = label;
		this.tfidfVector = tfidfVector;
	}
	
	public int docId;
	public String label;
	public List<Double> tfidfVector;
}
