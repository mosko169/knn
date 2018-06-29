import java.util.List;

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
