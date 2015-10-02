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

# Acknowledgements

This project is started from @shineware and refactoring by @jong03. Thanks for the great job.  

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

# License

* blog posting of @shineware: http://shineware.tistory.com/entry/KOMORAN-2x-%EB%9D%BC%EC%9D%B4%EC%84%BC%EC%8A%A4-%EB%B3%80%EA%B2%BD-%EA%B3%B5%EC%A7%80

> 자바 형태소 분석기 KOMORAN-2.0을 Apache License 2.0으로 공개합니다.
> 개발실력이 미천하여 이번에도 부끄러운 마음을 갖고 공개하였습니다. 
> 많은 채찍질과 도움으로 자연어처리 비전공자분들도 다양하게 활용할 수 있기를 희망합니다. 
> 아래 링크에서 소스코드를 다운 받으실 수 있습니다.
> https://github.com/shineware/komoran-2.0

Copyright shineware

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
