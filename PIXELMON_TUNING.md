# PIXELMON_TUNING.md (for CatServer 1.16.5 / Java 8)

## 권장 기본 설정
- `server.properties`
  - `view-distance=6` (7까지는 상황봐서)
  - `sync-chunk-writes=false` (가능하면 성능 개선)
- `spigot.yml`
  - `timeout-time: 180`
  - `world-settings.default.max-entity-collisions: 2`
  - `world-settings.default.merge-radius.item: 3.5`
  - `world-settings.default.merge-radius.exp: 3.5`
  - `world-settings.default.entity-activation-range: {animals: 16, monsters: 24, misc: 8}`
  - `world-settings.default.entity-tracking-range: {players:48, animals:36, monsters:36, misc:24, other:64}`

## 모드/플러그인
- Forge 모드: **Starlight (1.16.5: Starlight x Create 포크)** → 조명 엔진 가속
- Spigot 플러그인: **Chunky**(사전 프리젠), **WorldBorder**(경계 고정), **spark**(프로파일링)

## 운영 팁
- 프리젠은 **오프피크**에 한 월드씩 진행하고, 진행 중에는 본 플러그인의 **동적 쓰로틀** 기능에 맡기세요.
- Pixelmon 스폰/탐색 반경은 기본값에서 **-10~20%** 정도 완만히 하향하면 안정성↑
