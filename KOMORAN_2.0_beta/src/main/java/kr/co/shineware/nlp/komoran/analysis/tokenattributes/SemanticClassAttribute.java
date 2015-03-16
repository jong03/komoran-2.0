package kr.co.shineware.nlp.komoran.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;

public interface SemanticClassAttribute extends Attribute {
	public String semanticClass();

	public void setSemanticClass(String semanticClass);
}
