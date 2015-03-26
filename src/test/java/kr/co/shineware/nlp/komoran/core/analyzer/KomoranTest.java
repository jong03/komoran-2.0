package kr.co.shineware.nlp.komoran.core.analyzer;

import java.util.List;
import static org.junit.Assert.*;
import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;

import org.junit.Test;

public class KomoranTest {

	@Test
	public void test() {
		//corpus_build에 있는 데이터로부터 models 생성
		ModelBuilder builder = new ModelBuilder();
		builder.buildPath("kr/co/shineWare/dic/corpus_build");
		builder.save("kr/co/shineWare/dic/models");
		
		System.out.println("+++++++++++++++++++++++++++++++++++++");
		//생성된 models를 이용하여 객체 생성
		Komoran komoran = new Komoran("kr/co/shineWare/dic/models");
		System.out.println("+++++++++++++++++++++++++++++++++++++");
		String in = "지금보다 어리고 민감하던 시절 아버지가 충고를 한마디 했는데";
		
		List<List<Pair<String, String>>> analyzeResultList = komoran.analyze(in);
		for (List<Pair<String, String>> wordResultList : analyzeResultList) {
			for(int i=0;i<wordResultList.size();i++){
				Pair<String, String> pair = wordResultList.get(i);
				System.out.print(pair.getFirst()+"/"+pair.getSecond());
				if(i != wordResultList.size()-1){
					System.out.print("+");
				}
			}
			System.out.println();
		}
	}

}
