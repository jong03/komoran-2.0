package kr.co.shineware.nlp.komoran.analysis.ko;

import java.util.LinkedList;

import kr.co.shineware.nlp.komoran.core.analyzer.model.Pos;

/**
 * TokenGenerator에서 token으로 뽑는 품사와 품사의 연접과 token으로 뽑는 품사의 선택 알고리즘을 담당하는 추상 클래스.
 * 
 * @author bibreen <bibreen@gmail.com>
 */
public abstract class PosAppender {
	/**
	 * left PosId와 right PosId가 어절의 형태로 붙을 수 있는 품사인지 여부를 반환한다.
	 */
	public abstract boolean isAppendable(Pos left, Pos right);

	/**
	 * 해당 POS가 인덱싱에서 제외되는 POS인 경우 true, 아니면 false를 반환한다.
	 */
	public abstract boolean isSkippablePos(Pos pos);

	/**
	 * 어절을 구성하는 POS 리스트에서 추가적인 POS가 token으로 뽑혀야 하는 경우, 해당 POS 리스트를 반환한다.
	 *
	 * @param poses
	 *            어절을 구성하는 POS 리스트
	 */
	public abstract LinkedList<Pos> extractAdditionalPoses(LinkedList<Pos> poses);

}
