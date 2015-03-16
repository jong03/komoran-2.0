package kr.co.shineware.nlp.komoran.core.analyzer.support;

import java.io.File;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.co.shineware.ds.trie.TrieDictionary;
import kr.co.shineware.ds.trie.model.TrieNode;
import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.constant.SCORE;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.analyzer.model.PrevNodes;
import kr.co.shineware.nlp.komoran.core.analyzer.lattice.Lattice;
import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularTrie;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.modeler.model.Transition;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

public class StringParser {
	

	private IrregularTrie irrTrie;
	private Transition transition;

	private PrevNodes<TrieNode<List<Pair<Integer,Double>>>> prevNodesRegular;
	private PrevNodes<TrieNode<List<IrregularNode>>> prevNodesIrregular;
	private List<Pair<Integer,IrregularNode>> prevNodesExpand; 
	private UnitParser unitParser;
	private Lattice lattice;
	private DictionaryFormatter dicFormatter;
	
	//for ruleParser
	private String ruleMorph;
	private String rulePos;
	private int ruleBeginIdx;
	
	public StringParser(String path){
		init(path);
		load(path);
	}

	/**
	 * 각종 리소스 초기화
	 */
	private void init(String path){
		this.transition = new Transition();
		this.unitParser = new KoreanUnitParser();
		this.irrTrie = new IrregularTrie();
		this.dicFormatter = new DictionaryFormatter(path);
	}
	
	/**
	 * 형태소 분석에 사용되는 실제 데이터를 로딩 <br>
	 * 파일명은 {@link FILENAME}에 기술되어 있음
	 * @param path
	 */
	private void load(String path){
		this.transition.load(path+File.separator+FILENAME.TRANSITION);
		this.irrTrie.load(path+File.separator+FILENAME.IRREGULAR_MODEL);
	}
	
	
	/**
	 * 입력된 텍스트 src를 공백 단위로 잘라 형태소 분석(기존 형태소 분석기와 동일 방법)
	 * @param src
	 * @return
	 */
	@Deprecated
	public List<List<Pair<String, String>>> analyzeWithoutSpace(String src){
		List<List<Pair<String,String>>> result = new ArrayList<List<Pair<String,String>>>();
		String[] tokens = src.split("[ ]+");
		for (String token : tokens) {
			result.addAll(this.analyze(token));
		}
		return result;
	}
	@Deprecated
	public List<List<List<Pair<String, String>>>> analyzeWithoutSpace(String src,int nbest){
		
		//word<rank<analyze<morph,pos>>>
		List<List<List<Pair<String,String>>>> result = new ArrayList<List<List<Pair<String,String>>>>();
		String[] tokens = src.split("[ ]+");
		for (String token : tokens) {
			//rank<words<analyze<morph,pos>>>
			List<List<List<Pair<String, String>>>> tokenResultList = this.analyze(token,nbest);
			//rank<analyze<morph,pos>>
			List<List<Pair<String,String>>> rankTokenResultList = new ArrayList<>();
			for (List<List<Pair<String, String>>> list : tokenResultList) {
				rankTokenResultList.add(list.get(0));
			}
			result.add(rankTokenResultList);
//			result.addAll(this.analyze(token,nbest));
		}
		return result;
	}
	
	public List<List<List<Pair<String, String>>>> analyze(String src, int nbest){
		if(nbest < 1){
			return new ArrayList<List<List<Pair<String, String>>>>();
		}
		if(src.trim().length() == 0){
			return new ArrayList<List<List<Pair<String, String>>>>();
		}
		//형태소 분석 및 품사 태거를 위한 객체 초기화
		this.lattice = null;
		this.lattice = new Lattice(dicFormatter.getTable());
		this.lattice.setTransition(this.transition);
		this.lattice.setNbest(nbest);

		this.prevNodesRegular = null;
		this.prevNodesRegular = new PrevNodes<TrieNode<List<Pair<Integer, Double>>>>();

		this.prevNodesIrregular = null;
		this.prevNodesIrregular = new PrevNodes<TrieNode<List<IrregularNode>>>();

		this.prevNodesExpand = null;
		this.prevNodesExpand = new ArrayList<>();
		
		this.ruleMorph = "";
		this.rulePos = "";
		this.ruleBeginIdx = 0;		
		
		//자소 단위로 분리
		String in = unitParser.parse(src.trim());
		
		for(int i=0;i<in.length();i++){
			//기분석 사전 매칭
			int skipIdx = dicFormatter.lookupFwd(in,i,this.lattice);
			//매칭된 인덱스 만큼 증가
			if(skipIdx != -1){
				i = skipIdx-1;
				continue;
			}
			
			//규칙 기반의 연속된 숫자, 영어, 한자, 외래어 파싱
			this.ruleParsing(in,i);
			//규칙 기반의 특수 문자 파싱
			this.symbolParsing(in,i);
			
			//불규칙 확장 파싱
			this.irregularExpandParsing(in,i,dicFormatter.getObservation().getTrieDictionary());
			//기본 파싱
			this.regularParsing(in,i,dicFormatter.getObservation().getTrieDictionary());
			//불규칙 파싱
			this.irregularParsing(in,i,this.irrTrie.getTrieDictionary());
			
			//현재 character가 공백인 경우 처리 
			if(in.charAt(i) == ' '){
				//이전 탐색 노드와 다음 탐색 노드 사이에서 출현하는 공백(space)을 응용하여
				//추후 백트래킹이 가능하게 함
				this.lattice.bridgingSpace(in,i);
				lattice.setPrevStartIdx(i+1);
			}
		}
		
		//규칙 기반의 파싱 중 남은 버퍼를 lattice 결과에 삽입
		this.consumeRuleParserBuffer(in);		

		//백트래킹할 end index 설정
		this.lattice.setEndIdx(in.length());
		
		//lattice의 end index로부터 백트래킹
		if(nbest > 1){
			return this.lattice.getNbest(in);
		}else{
			List<List<List<Pair<String, String>>>> maxResult = new ArrayList<List<List<Pair<String,String>>>>();
			maxResult.add(this.lattice.getMax(in));
			return maxResult;
		}
	}

	/**
	 * 입력된 텍스트 src로부터 형태소를  <br>
	 * @param src 형태소 분석 대상 text
	 * @return
	 */
	public List<List<Pair<String, String>>> analyze(String src){
		if(src.trim().length() == 0){
			return new ArrayList<List<Pair<String, String>>>();
		}
		//형태소 분석 및 품사 태거를 위한 객체 초기화
		this.lattice = null;
		this.lattice = new Lattice(dicFormatter.getTable());
		this.lattice.setTransition(this.transition);

		this.prevNodesRegular = null;
		this.prevNodesRegular = new PrevNodes<TrieNode<List<Pair<Integer, Double>>>>();

		this.prevNodesIrregular = null;
		this.prevNodesIrregular = new PrevNodes<TrieNode<List<IrregularNode>>>();

		this.prevNodesExpand = null;
		this.prevNodesExpand = new ArrayList<>();
		
		this.ruleMorph = "";
		this.rulePos = "";
		this.ruleBeginIdx = 0;		
		
		//자소 단위로 분리
		String in = unitParser.parse(src.trim());
		
		for(int i=0;i<in.length();i++){
			//기분석 사전 매칭
			int skipIdx = dicFormatter.lookupFwd(in,i,this.lattice);
			//매칭된 인덱스 만큼 증가
			if(skipIdx != -1){
				i = skipIdx-1;
				continue;
			}
			
			//규칙 기반의 연속된 숫자, 영어, 한자, 외래어 파싱
			this.ruleParsing(in,i);
			//규칙 기반의 특수 문자 파싱
			this.symbolParsing(in,i);
			
			//불규칙 확장 파싱
			this.irregularExpandParsing(in,i,dicFormatter.getObservation().getTrieDictionary());
			//기본 파싱
			this.regularParsing(in,i,dicFormatter.getObservation().getTrieDictionary());
			//불규칙 파싱
			this.irregularParsing(in,i,this.irrTrie.getTrieDictionary());
			
			//현재 character가 공백인 경우 처리 
			if(in.charAt(i) == ' '){
				//이전 탐색 노드와 다음 탐색 노드 사이에서 출현하는 공백(space)을 응용하여
				//추후 백트래킹이 가능하게 함
				this.lattice.bridgingSpace(in,i);
				lattice.setPrevStartIdx(i+1);
			}
		}
		
		//규칙 기반의 파싱 중 남은 버퍼를 lattice 결과에 삽입
		this.consumeRuleParserBuffer(in);	

		//백트래킹할 end index 설정
		this.lattice.setEndIdx(in.length());

		
		//lattice의 end index로부터 백트래킹
		return this.lattice.getMax(in);
	}
	
	/**
	 * 입력된 문자로부터 symbol을 구분하여 lattice에 삽입
	 * @param in
	 * @param i
	 */
	private void symbolParsing(String in, int i) {
		char ch = in.charAt(i);
		Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
		//숫자
		if(Character.isDigit(ch)){
			
		}
		else if(unicodeBlock == Character.UnicodeBlock.BASIC_LATIN){
			//영어
			if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z'))) {
				;
			}
			else if(dicFormatter.getObservation().getTrieDictionary().get(ch) != null){
				;
			}
			//symbol
			else{
				this.lattice.put(i, i+1, ""+ch, dicFormatter.getTable().getId(SYMBOL.SW), SCORE.SW);
			}
		}
		//한글
		else if(unicodeBlock == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO 
				|| unicodeBlock == UnicodeBlock.HANGUL_JAMO
				|| unicodeBlock == UnicodeBlock.HANGUL_JAMO_EXTENDED_A
				||unicodeBlock == UnicodeBlock.HANGUL_JAMO_EXTENDED_B
				||unicodeBlock == UnicodeBlock.HANGUL_SYLLABLES){
			;
		}
		//일본어
		else if(unicodeBlock == UnicodeBlock.KATAKANA
				|| unicodeBlock == UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS){
			
		}
		//중국어
		else if(UnicodeBlock.CJK_COMPATIBILITY.equals(unicodeBlock)				
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock)
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(unicodeBlock)
				|| UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(unicodeBlock)
				|| UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(unicodeBlock)){
			;
		}
		else{
			this.lattice.put(i, i+1, ""+ch, dicFormatter.getTable().getId(SYMBOL.SW), SCORE.SW);
		}
	}

	/**
	 * 규칙 기반의 파싱 중 남은 버퍼에 대해서 lattice에 삽입
	 * @param in
	 */
	private void consumeRuleParserBuffer(String in) {
		if(this.rulePos.trim().length() != 0){
			if(this.rulePos.equals("SL")){
				this.lattice.put(this.ruleBeginIdx, in.length(), this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SL);
			}else if(this.rulePos.equals("SH")){
				this.lattice.put(this.ruleBeginIdx, in.length(), this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SH);
			}else if(this.rulePos.equals("SN")){
				this.lattice.put(this.ruleBeginIdx, in.length(), this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SN);
			}
		}
	}

	/**
	 * 규칙 기반의 파싱 <br>
	 * 연속된 영어, 외래어, 한자, 숫자에 대해서 같은 품사가 연속된 구간에 동일한 품사 부착
	 * @param in
	 * @param i
	 */
	private void ruleParsing(String in, int i) {
		char ch = in.charAt(i);
		String curPos = "";
		if(StringUtil.isEnglish(ch)){
			curPos = "SL";
		}else if(StringUtil.isNumeric(ch)){
			curPos = "SN";
		}else if(StringUtil.isChinese(ch)){
			curPos = "SH";
		}else if(StringUtil.isForeign(ch)){
			curPos = "SL";
		}
		
		if(curPos.equals(this.rulePos)){
			this.ruleMorph += ch;
		}
		else{
			if(this.rulePos.equals("SL")){
				this.lattice.put(this.ruleBeginIdx, i, this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SL);
			}else if(this.rulePos.equals("SN")){
				this.lattice.put(this.ruleBeginIdx, i, this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SN);
			}else if(this.rulePos.equals("SH")){
				this.lattice.put(this.ruleBeginIdx, i, this.ruleMorph, dicFormatter.getTable().getId(this.rulePos), SCORE.SH);
			}

			this.ruleBeginIdx = i;
			this.ruleMorph = ""+ch;
			this.rulePos = curPos;
		}
	}

	
	/**
	 * 이전 탐색 시 불완전 파싱된 불규칙 데이터를 확장하여 파싱
	 * @param in
	 * @param i
	 * @param trieDictionary
	 */
	private void irregularExpandParsing(String in, int i,
			TrieDictionary<List<Pair<Integer, Double>>> trieDictionary) {

		char key = in.charAt(i);

		List<Pair<Integer,IrregularNode>> tmpPrevNodesExpand = new ArrayList<>();
		for (Pair<Integer, IrregularNode> prevExtendNodes : this.prevNodesExpand) {
			int beginIdx = prevExtendNodes.getFirst();
			int endIdx = i+1;
			List<Pair<Integer,Double>> observationScoreList = trieDictionary.get(prevExtendNodes.getSecond().getLastMorph()+key);
			boolean hasChildren = trieDictionary.hasChildren();
			if(observationScoreList != null){
				for (Pair<Integer, Double> pair : observationScoreList) {
					Double innerScore = this.calIrregularScore(prevExtendNodes.getSecond().getTokens(),pair.getFirst());
					if(innerScore == null) continue;
					double score = pair.getSecond() + innerScore;
					this.lattice.put(beginIdx, endIdx, prevExtendNodes.getSecond().getMorphFormat()+key, prevExtendNodes.getSecond().getFirstPosId(), pair.getFirst(), score);

					innerScore = null;
				}
			}
			if(hasChildren){
				IrregularNode irrNode = new IrregularNode();
				irrNode.setFirstPosId(prevExtendNodes.getSecond().getFirstPosId());
				irrNode.setLastMorph(prevExtendNodes.getSecond().getLastMorph()+key);
				irrNode.setMorphFormat(prevExtendNodes.getSecond().getMorphFormat()+key);
				irrNode.setTokens(prevExtendNodes.getSecond().getTokens());
				tmpPrevNodesExpand.add(new Pair<Integer, IrregularNode>(beginIdx, irrNode));
				irrNode = null;
			}
			//init
			observationScoreList = null;
		}
		this.prevNodesExpand = tmpPrevNodesExpand;
		//init
		tmpPrevNodesExpand = null;
	}
	
	/**
	 * 불규칙의 관측 확률 및 전이 확률을 계산
	 * @param tokens
	 * @param nextPosId
	 * @return
	 */
	private Double calIrregularScore(List<Pair<String, Integer>> tokens, Integer nextPosId) {
		double score = 0.0;
		int prevPosId = -1;
		Double transitionScore = 0.0;
		for(int i=0;i<tokens.size()-1;i++){
			Pair<String,Integer> token = tokens.get(i);
			String morph = token.getFirst();
			int posId = token.getSecond();
			//관측 확률
			List<Pair<Integer,Double>> posScoreList = dicFormatter.getObservation().getTrieDictionary().get(morph);
			for (Pair<Integer, Double> pair : posScoreList) {
				if(pair.getFirst() == posId){
					score += pair.getSecond();
					break;
				}
			}

			//전이 확률
			if(prevPosId != -1){
				transitionScore = this.transition.get(prevPosId, posId); 
				if(transitionScore == null){
					return null;
				}
				score += transitionScore;
			}
			prevPosId = posId;
			//init
			posScoreList = null;
			token = null;			
		}
		if(prevPosId != -1){
			transitionScore = this.transition.get(prevPosId, nextPosId);
		}else{
			transitionScore = null;
		}
		if(transitionScore == null){
			return null;
		}else{
			return score+transitionScore;
		}		
	}

	/**
	 * 불규칙 파싱 <br>
	 * 사전 및 이전 노드와의 결합을 통해서 lattice에 노드 삽입 <br>
	 * 불규칙 확장을 위한 내용 포함(???)<br>
	 * @param in
	 * @param i
	 * @param trieDictionary
	 */
	private void irregularParsing(String in, int i,
			TrieDictionary<List<IrregularNode>> trieDictionary) {
		char key = in.charAt(i);
		//for prev nodes
		Set<Integer> beginIdxSet = this.prevNodesIrregular.getNodeMap().keySet();
		Set<Integer> removeBeginIdxSet = new HashSet<>();
		for (Integer beginIdx : beginIdxSet) {		
			trieDictionary.setCurrentNode(this.prevNodesIrregular.get(beginIdx));
			List<IrregularNode> irregularNodeList = trieDictionary.get(key);
			if(irregularNodeList != null){
				for (IrregularNode irregularNode : irregularNodeList) {
					this.lattice.put(beginIdx, i+1, irregularNode.getMorphFormat(), irregularNode.getFirstPosId(), irregularNode.getLastPosId(), irregularNode.getInnerScore());
					this.prevNodesExpand.add(new Pair<Integer, IrregularNode>(beginIdx, irregularNode));
					if(irregularNode.getLastPosId() == dicFormatter.getTable().getId(SYMBOL.EC)){
						this.lattice.put(beginIdx, i+1, irregularNode.getMorphFormat(), irregularNode.getFirstPosId(), dicFormatter.getTable().getId(SYMBOL.EF), irregularNode.getInnerScore());
					}
				}
			}
			if(trieDictionary.hasChildren()){
				this.prevNodesIrregular.insert(beginIdx, trieDictionary.getCurrentNode());
			}
			else{
				removeBeginIdxSet.add(beginIdx);
			}

			//init
			irregularNodeList = null;
		}
		this.prevNodesIrregular.remove(removeBeginIdxSet);

		//for current key retrieval
		trieDictionary.setCurrentNode(null);
		List<IrregularNode> irregularNodeList = trieDictionary.get(key);
		if(irregularNodeList != null){
			for (IrregularNode irregularNode : irregularNodeList) {
				this.lattice.put(i, i+1, irregularNode.getMorphFormat(), irregularNode.getFirstPosId(), irregularNode.getLastPosId(), irregularNode.getInnerScore());
				this.prevNodesExpand.add(new Pair<Integer, IrregularNode>(i, irregularNode));
			}
		}
		if(trieDictionary.hasChildren()){
			this.prevNodesIrregular.insert(i, trieDictionary.getCurrentNode());
		}
		//init
		beginIdxSet = null;
		irregularNodeList = null;
		removeBeginIdxSet = null;
	}


	/**
	 * 일반적 파싱 <br>
	 * 이전 노드와 현재 노드를 결합 후 사전에 존재하는지 여부 파악하여 lattice에 노드 삽입<br>
	 * 단, 단일 문자 하나인 경우에도 사전에 존재한다면 lattice에 노드 삽입 가능<br> 
	 * @param in
	 * @param i
	 * @param trieDictionary
	 */
	private void regularParsing(String in, int i,TrieDictionary<List<Pair<Integer, Double>>> trieDictionary) {

		char key = in.charAt(i);

		//for prev nodes
		Set<Integer> beginIdxSet = this.prevNodesRegular.getNodeMap().keySet();
		Set<Integer> removeBeginIdxSet = new HashSet<>();
		for (Integer beginIdx : beginIdxSet) {

			trieDictionary.setCurrentNode(this.prevNodesRegular.get(beginIdx));
			List<Pair<Integer, Double>> posIdScoreList = trieDictionary.get(key);
			if(posIdScoreList != null){
				//it will be changed that insert lattice method
				this.lattice.put(beginIdx,i+1,posIdScoreList,in.substring(beginIdx, i+1));
				
				//EC를 삽입한 경우 EF도 추가적으로 삽입
				if(posIdScoreList.get(posIdScoreList.size()-1).getFirst() == dicFormatter.getTable().getId(SYMBOL.EC)){
					List<Pair<Integer, Double>> posIdScoreListTmp = new ArrayList<Pair<Integer,Double>>();
					for (Pair<Integer, Double> pair : posIdScoreList) {
						posIdScoreListTmp.add(new Pair<Integer, Double>(pair.getFirst(),pair.getSecond()));
					}
					Pair<Integer, Double> lastIdConverted = posIdScoreListTmp.get(posIdScoreListTmp.size()-1);
					lastIdConverted.setFirst(dicFormatter.getTable().getId(SYMBOL.EF));
					posIdScoreListTmp.set(posIdScoreListTmp.size()-1, lastIdConverted);
					this.lattice.put(beginIdx,i+1,posIdScoreListTmp,in.substring(beginIdx, i+1));
				}
				posIdScoreList = null;
			}
			if(trieDictionary.hasChildren()){
				this.prevNodesRegular.insert(beginIdx, trieDictionary.getCurrentNode());
			}
			else{
				removeBeginIdxSet.add(beginIdx);
			}
		}
		this.prevNodesRegular.remove(removeBeginIdxSet);
		//for current key
		trieDictionary.setCurrentNode(null);
		List<Pair<Integer,Double>> posIdScoreList = trieDictionary.get(key);
		if(posIdScoreList != null){
			this.lattice.put(i,i+1,posIdScoreList,in.substring(i, i+1));
		}
		if(trieDictionary.hasChildren()){
			this.prevNodesRegular.insert(i, trieDictionary.getCurrentNode());
		}

		//init
		beginIdxSet = null;
		removeBeginIdxSet = null;
		posIdScoreList = null;
	}
	
}
