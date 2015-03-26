package kr.co.shineware.nlp.komoran.analysis.tokenattributes;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class SemanticClassAttributeImpl extends AttributeImpl implements
		SemanticClassAttribute, Cloneable {

	private String semanticClass;

	@Override
	public String semanticClass() {
		return semanticClass;
	}

	@Override
	public void setSemanticClass(String semanticClass) {
		this.semanticClass = semanticClass;
	}

	@Override
	public void clear() {
		this.semanticClass = null;

	}

	@Override
	public void copyTo(AttributeImpl target) {
		SemanticClassAttribute targetAttribute = (SemanticClassAttribute) target;
		targetAttribute.setSemanticClass(semanticClass);
	}

	@Override
	public void reflectWith(AttributeReflector reflector) {
		reflector.reflect(SemanticClassAttribute.class, "semanticClass",
				semanticClass());
	}
}
