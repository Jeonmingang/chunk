# UltimateTrashOptimize (1.16.5 / Java 8)

- 자동 **바닥청소(드롭 아이템)**, 자동 **비전설 픽셀몬 청소**만 제공합니다.
- 두 기능 모두 **카운트다운(60,30,10,5,4,3,2,1초)** 방송을 **한 번만(글로벌)** 띄웁니다.
- 문구/초/월드/탐지 규칙은 전부 `config.yml`에서 변경 가능합니다.

## 명령어
- `/uto reload` : 설정 리로드 및 스케줄 재시작
- `/uto status` : 상태 메시지

## 빌드
```bash
mvn -q -DskipTests clean package
```
- 산출물: `target/UltimateTrashOptimize-1.7.0.jar` (spigot-api 1.16.5, Java 8)

## 설정 키
- `cleanup.broadcastOnce: true` → 월드별 반복 방송 대신 한번만 방송
- `cleanup.ground.*` → 바닥청소
- `cleanup.pixelmon.*` → 비전설 픽셀몬 청소
