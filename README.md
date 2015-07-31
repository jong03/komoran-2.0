Korean morphological analyzer by shineware
==========================================

Version: 2.0

# How to build

1. Download two files from shineware's blog.
* [shineware-common-2.0.jar](http://shineware.tistory.com/attachment/cfile9.uf@2752823C542945A30BE87B.jar>download)
* [shineware-ds-1.0.jar](http://shineware.tistory.com/attachment/cfile10.uf@22510A3C542945AB0DF2ED.jar>download)

2. Put `shineware-common-2.0.jar` into `lib/com/shineware/shineware-common/2.0`.

3. Put `shineware-ds-2.0.jar` into `lib/com/shineware/shinware-ds/1.0`.

4. Do `mvn package`.

# original post by @shineware

* 현재 komoran 2.0과 관련된 문서들을 정리 중에 있습니다. 조만간 재정비하겠습니다.
* 더불어 komoran 2.0 동작 및 구현 방식과 관련된 논문을 작성 중에 있습니다. 이 역시 조만간 업데이트 하도록 하겠습니다.
* 그리고 마지막으로 소스가 많이 더럽습니다......

komoran 2.0을 실행하기 위해서는 아래와 같은 라이브러리가 필요합니다.

shineware-common-2.0.jar (<a href=http://shineware.tistory.com/attachment/cfile9.uf@2752823C542945A30BE87B.jar>download</a>)

shineware-ds-1.0.jar (<a href=http://shineware.tistory.com/attachment/cfile10.uf@22510A3C542945AB0DF2ED.jar>download</a>)

자세한 사용법은 아래 링크에서 확인하실 수 있습니다.

블로그 : http://shineware.tistory.com/entry/KOMORAN-ver-24

데모는 아래 링크에서 확인하실 수 있습니다.

사이트 : http://www.shineware.co.kr

# mention by @jong03

maven 프로젝트로 재구성함 리펙토링중......무단으로 해도 되려나...
