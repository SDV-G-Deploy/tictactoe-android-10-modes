# Крестики-нолики (Android, 10 режимов)

Офлайн-приложение на Kotlin с 10 игровыми режимами в одном APK.

## Что внутри

- Главное меню (стартовый экран)
- Явный выбор режима (список + карточка с описанием)
- Экран игры с навигацией: **Рестарт / Сменить режим / Назад в меню**
- Общая и лёгкая архитектура игровой логики (`GameMode` + фабрика режимов)
- Лёгкий UI на XML + AppCompat/Material без тяжёлых библиотек

## Режимы

1. Классика 3x3
2. 4x4 (4 в ряд)
3. 5x5 (4 в ряд)
4. 5x5 (5 в ряд)
5. Misère 3x3
6. Ultimate (упрощённый)
7. Random Blocked Cells
8. Timed Turn
9. Bomb Cell
10. Gravity Mode

## Как выбрать и сменить режим

1. На стартовом экране выберите режим в списке **«Все режимы»**.
2. Нажмите **«Начать игру»**.
3. Во время партии нажмите **«Сменить режим»** (или **«Назад в меню»**) и выберите другой режим.
4. Кнопка **«Рестарт»** перезапускает текущий режим без выхода в меню.

## Архитектура (ключевые файлы)

- `app/src/main/java/com/example/tictactoe/MainActivity.kt` — меню, экран игры, навигация, отрисовка поля.
- `app/src/main/java/com/example/tictactoe/GameModes.kt` — модели состояния и реализация всех режимов.

## Сборка

### Требования
- JDK 17+
- Android SDK

### Debug
```bash
./gradlew assembleDebug
```
APK:
`app/build/outputs/apk/debug/app-debug.apk`

### Release
```bash
./gradlew assembleRelease
```
APK:
`app/build/outputs/apk/release/app-release-unsigned.apk`
