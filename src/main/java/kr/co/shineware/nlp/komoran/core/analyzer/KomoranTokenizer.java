package kr.co.shineware.nlp.komoran.core.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Queue;

import kr.co.shineware.nlp.komoran.analysis.ko.TokenGenerator;
import kr.co.shineware.nlp.komoran.analysis.tokenattributes.PartOfSpeechAttribute;
import kr.co.shineware.nlp.komoran.analysis.tokenattributes.SemanticClassAttribute;
import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.core.analyzer.model.Pos;
import kr.co.shineware.nlp.komoran.core.analyzer.support.StringParser;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

public class KomoranTokenizer extends Tokenizer {
	
	private CharTermAttribute charTermAtt;
	private PositionIncrementAttribute posIncrAtt;
	private PositionLengthAttribute posLenAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	private PartOfSpeechAttribute posAtt;
	private SemanticClassAttribute semanticClassAtt;
	
	private StringParser stringParser;
	private Queue<Pos> tokensQueue;


	protected KomoranTokenizer(AttributeFactory factory, Reader input, String path) {
		super(factory, input);
		// TODO Auto-generated constructor stub
		this.setAttributes();
		this.stringParser = new StringParser(path);
	}
	
	private void setAttributes() {
		charTermAtt = addAttribute(CharTermAttribute.class);
		posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		posLenAtt = addAttribute(PositionLengthAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		posAtt = addAttribute(PartOfSpeechAttribute.class);
		semanticClassAtt = addAttribute(SemanticClassAttribute.class);
	}
	
	private void setAttributes(Pos token) {
		posIncrAtt.setPositionIncrement(token.getPositionIncr());
		posLenAtt.setPositionLength(token.getPositionLength());
		offsetAtt.setOffset(correctOffset(token.getStartOffset()),
				correctOffset(token.getEndOffset()));
		charTermAtt.copyBuffer(token.getSurface().toCharArray(), 0,
				token.getSurfaceLength());
		typeAtt.setType(token.getPosId().toString());
		posAtt.setPartOfSpeech(token.getMophemes());
		semanticClassAtt.setSemanticClass(token.getSemanticClass());
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
//		
//		if (isBegin()) {
//			createTokenGenerator();
//		}

//		if (tokensQueue == null || tokensQueue.isEmpty()) {
//			tokensQueue = generator.getNextEojeolTokens();
//			if (tokensQueue == null) {
//				return false;
//			}
//		}
		
		Pos token = tokensQueue.poll();
		setAttributes(token);
		// TODO Auto-generated method stub
		return true;
	}
//	
//	private boolean isBegin() {
//		return generator == null;
//	}
//
//	private void createTokenGenerator() {
//		lattice.set_sentence(document);
//		tagger.parse(lattice);
//		this.generator = new TokenGenerator(posAppender, compoundNounMinLength,
//				lattice.bos_node());
//	}
//	

}
