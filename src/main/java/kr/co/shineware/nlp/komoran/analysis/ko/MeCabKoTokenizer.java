package kr.co.shineware.nlp.komoran.analysis.ko;

import java.io.IOException;
import java.io.Reader;
import java.util.Queue;

import kr.co.shineware.nlp.komoran.analysis.support.Lattice;
import kr.co.shineware.nlp.komoran.analysis.support.MeCabLoader;
import kr.co.shineware.nlp.komoran.analysis.support.Tagger;
import kr.co.shineware.nlp.komoran.analysis.tokenattributes.PartOfSpeechAttribute;
import kr.co.shineware.nlp.komoran.analysis.tokenattributes.SemanticClassAttribute;
import kr.co.shineware.nlp.komoran.core.analyzer.model.Pos;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 * Lucene/Solr용 Tokenizer.
 * 
 * @author bibreen <bibreen@gmail.com>
 * @author amitabul <mousegood@gmail.com>
 */
public final class MeCabKoTokenizer extends Tokenizer {
	private CharTermAttribute charTermAtt;
	private PositionIncrementAttribute posIncrAtt;
	private PositionLengthAttribute posLenAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	private PartOfSpeechAttribute posAtt;
	private SemanticClassAttribute semanticClassAtt;

	private String document;
	private String mecabDicDir;
	private MeCabLoader mecabLoader;
	private Lattice lattice;
	private Tagger tagger;
	private PosAppender posAppender;
	private int compoundNounMinLength;
	private TokenGenerator generator;
	private Queue<Pos> tokensQueue;

	/**
	 * MeCabKoTokenizer 생성자. Default AttributeFactory 사용.
	 * 
	 * @param input
	 * @param dicDir
	 *            mecab 사전 디렉터리 경로
	 * @param appender
	 *            PosAppender
	 * @param compoundNounMinLength
	 *            분해를 해야하는 복합명사의 최소 길이. 복합명사 분해가 필요없는 경우,
	 *            TokenGenerator.NO_DECOMPOUND를 입력한다.
	 */
	public MeCabKoTokenizer(Reader input, String dicDir, PosAppender appender,
			int compoundNounMinLength) {
		this(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, input, dicDir,
				appender, compoundNounMinLength);
	}

	/**
	 * MeCabKoTokenizer 생성자.
	 * 
	 * @param factory
	 *            the AttributeFactory to use
	 * @param input
	 * @param dicDir
	 *            mecab 사전 디렉터리 경로
	 * @param appender
	 *            PosAppender
	 * @param compoundNounMinLength
	 *            분해를 해야하는 복합명사의 최소 길이. 복합명사 분해가 필요없는 경우,
	 *            TokenGenerator.NO_DECOMPOUND를 입력한다.
	 */
	public MeCabKoTokenizer(AttributeFactory factory, Reader input,
			String dicDir, PosAppender appender, int compoundNounMinLength) {
		super(factory, input);
		posAppender = appender;
		mecabDicDir = dicDir;
		this.compoundNounMinLength = compoundNounMinLength;
		setMeCab();
		setAttributes();
	}

	private void setMeCab() {
		mecabLoader = MeCabLoader.getInstance(mecabDicDir);
		lattice = mecabLoader.createLattice();
		tagger = mecabLoader.createTagger();
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

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		
		if (isBegin()) {
			document = getDocument();
			createTokenGenerator();
		}

		if (tokensQueue == null || tokensQueue.isEmpty()) {
			tokensQueue = generator.getNextEojeolTokens();
			if (tokensQueue == null) {
				return false;
			}
		}
		
		Pos token = tokensQueue.poll();
		setAttributes(token);
		return true;
	}

	private boolean isBegin() {
		return generator == null;
	}

	private void createTokenGenerator() {
		lattice.set_sentence(document);
		tagger.parse(lattice);
		this.generator = new TokenGenerator(posAppender, compoundNounMinLength,
				lattice.bos_node());
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
	public final void end() throws IOException {
		super.end();
		// set final offset
		offsetAtt.setOffset(correctOffset(document.length()),
				correctOffset(document.length()));
		document = null;
	}

	@Override
	public final void reset() throws IOException {
		super.reset();
		generator = null;
		tokensQueue = null;
	}

	private String getDocument() throws IOException {
		StringBuilder document = new StringBuilder();
		char[] tmp = new char[1024];
		int len;
		while ((len = input.read(tmp)) != -1) {
			document.append(new String(tmp, 0, len));
		}
		return document.toString().toLowerCase();
	}
}