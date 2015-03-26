package kr.co.shineware.nlp.komoran.core.analyzer.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.core.analyzer.lattice.Lattice;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

public class DictionaryFormatter {
	
	private PosTable table;
	
	public PosTable getTable() {
		return table;
	}

	public void setTable(PosTable table) {
		this.table = table;
	}

	private Observation observation;
	
	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}


	private HashMap<String, List<Pair<String, Integer>>> fwd;	
	private UnitParser unitParser;
	
	public DictionaryFormatter(String path){
		this.init();
		this.load(path);
	}
	
	private void init(){

		this.table = new PosTable();
		this.observation = new Observation();
		this.unitParser = new KoreanUnitParser();
	
	}
	
	private void load(String path){
		this.table.load(path+File.separator+FILENAME.POS_TABLE);
		this.observation.load(path+File.separator+FILENAME.OBSERVATION);
	}

	/**
	 * 기분석 사전 매칭
	 * @param in
	 * @param i
	 * @return
	 */
	public int lookupFwd(String in, int i, Lattice lattice) {
		if(this.fwd == null)return -1;
		if(i == 0 || in.charAt(i-1) ==' '){
			int nextSpaceIdx = in.indexOf(" ", i);
			String toMatchToken = null;
			if(nextSpaceIdx == -1){
				toMatchToken = in.substring(i);
				nextSpaceIdx = in.length();
			}else{
				toMatchToken = in.substring(i,nextSpaceIdx);
			}
			if(toMatchToken.trim().length() == 0){
				return -1;
			}
			List<Pair<String,Integer>> fwdResult = this.fwd.get(toMatchToken);
			if(fwdResult != null){
				String morph = "";
				int firstPosId = -1;
				int lastPosId = -1;
				for(int j=0;j<fwdResult.size();j++){
					Pair<String,Integer> morphPosPair = fwdResult.get(j);
					morph += morphPosPair.getFirst();
					if(j==0){
						firstPosId = morphPosPair.getSecond();
					}
					if(j== fwdResult.size()-1){
						lastPosId = morphPosPair.getSecond();
						break;
					}
					morph += "/"+this.table.getPos(morphPosPair.getSecond())+" ";
					//init
					morphPosPair = null;
				}
				morph = morph.trim();
				lattice.put(i, nextSpaceIdx, morph, firstPosId, lastPosId, 0.0);
				lattice.bridgingSpace(in,nextSpaceIdx);
				lattice.setPrevStartIdx(nextSpaceIdx+1);
				//init
				fwdResult = null;
				morph = null;
				return nextSpaceIdx+1;
			}else{
				return -1;
			}
		}
		return -1;
	}	

	/**
	 * 사용자 사전 추가
	 * @param userDic
	 */
	public void addUserDic(String userDic){
		try {
			BufferedReader br = new BufferedReader(new FileReader(userDic));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();				
				if(line.length() == 0 || line.charAt(0) == '#')continue;
				int lastIdx = line.lastIndexOf("\t");

				String morph;
				String pos;
				if(lastIdx == -1){
					morph = line.trim();
					pos = "NNP";
				}else{
					morph = line.substring(0, lastIdx);
					pos = line.substring(lastIdx+1);
				}
				this.observation.put(morph, this.table.getId(pos), 0.0);

				line = null;
				morph = null;
				pos = null;
			}
			br.close();

			//init
			br = null;
			line = null;
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}
	
	/**
	 * 사용자 사전 설정
	 * @param userDic
	 */
	public void setUserDic(String userDic) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(userDic));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();				
				if(line.length() == 0 || line.charAt(0) == '#')continue;
				int lastIdx = line.lastIndexOf("\t");

				String morph;
				String pos;
				if(lastIdx == -1){
					morph = line.trim();
					pos = "NNP";
				}else{
					morph = line.substring(0, lastIdx);
					pos = line.substring(lastIdx+1);
				}
				this.observation.put(morph, this.table.getId(pos), 0.0);

				line = null;
				morph = null;
				pos = null;
			}
			br.close();

			//init
			br = null;
			line = null;
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}

	/**
	 * 기분석 사전 설정
	 * @param filename
	 */
	public void setFWDic(String filename) {		
		try {
			CorpusParser corpusParser = new CorpusParser();
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			this.fwd = new HashMap<String, List<Pair<String, Integer>>>();
			while ((line = br.readLine()) != null) {
				String[] tmp = line.split("\t"); //$NON-NLS-1$
				if (tmp.length != 2 || tmp[0].charAt(0) == '#'){
					tmp = null;
					continue;
				}
				ProblemAnswerPair problemAnswerPair = corpusParser.parse(line);
				List<Pair<String,Integer>> convertAnswerList = new ArrayList<>();
				for (Pair<String, String> pair : problemAnswerPair.getAnswerList()) {
					convertAnswerList.add(
							new Pair<String, Integer>(
									this.unitParser.parse(pair.getFirst()), this.table.getId(pair.getSecond())));
				}
				this.fwd.put(this.unitParser.parse(problemAnswerPair.getProblem()),
						convertAnswerList);
				tmp = null;
				problemAnswerPair = null;
				convertAnswerList = null;
			}			
			br.close();

			//init
			corpusParser = null;
			br = null;
			line = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
