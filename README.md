# UltimateTrashOptimize (GitHub Build, fixed)
- Java 8 / Spigot 1.16.5
- UTF-8 **without BOM**, no stray backslashes in Java sources
- /최적화 명령어 세트(자동 실행/취소, 바닥청소/픽셀몬 on|off, 월드 지정 1회, 청키 시작)
- 자동 오케스트레이션(시간창+인원 조건+전역 1회 카운트다운)
- GitHub Actions 포함 (.github/workflows/main.yml)

## Build
mvn -B -q -DskipTests clean package