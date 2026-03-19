# Крестики-нолики (Android, 10 режимов)

Офлайн-приложение на Kotlin с **10 разными игровыми режимами** в одном APK.

## Что реализовано

- Экран выбора режима (Spinner)
- Общая кнопка **«Новая игра»** для любого режима
- Корректная смена ходов, проверка победы/ничьей
- Отдельная архитектура игровой логики (вынесена из `Activity`)
- Лёгкий UI на XML + AppCompat, без тяжёлых зависимостей

## 10 игровых режимов

1. **Классика 3x3** — 3 в ряд
2. **4x4 (4 в ряд)**
3. **5x5 (4 в ряд)**
4. **5x5 (5 в ряд)**
5. **Misère 3x3** — собрал линию, значит проиграл
6. **Ultimate (упрощённый)** — 9 мини-полей 3x3, цель: выиграть 3 мини-поля в линию
7. **Random Blocked Cells** — поле 5x5, 5 случайно заблокированных клеток
8. **Timed Turn** — 10 секунд на ход, иначе ход переходит сопернику
9. **Bomb Cell** — одна скрытая бомба: ход теряется, удаляется одна своя фишка
10. **Gravity Mode** — фишки падают вниз в колонке, победа: 4 в ряд

## Архитектура

Ключевые файлы:

- `app/src/main/java/com/example/tictactoe/MainActivity.kt`
  - UI, выбор режима, отрисовка сетки, таймерный тик
- `app/src/main/java/com/example/tictactoe/GameModes.kt`
  - Модели состояния
  - Интерфейс режима `GameMode`
  - Реализации режимов (`ConfigurableMode`, `UltimateMode`)
  - Фабрика `GameModesFactory`

## Сборка

### Требования
- JDK 17+
- Android SDK

### Debug
```bash
cd tictactoe-android
./gradlew assembleDebug
```
APK:
`app/build/outputs/apk/debug/app-debug.apk`

### Release
```bash
cd tictactoe-android
./gradlew assembleRelease
```
APK:
`app/build/outputs/apk/release/app-release-unsigned.apk`
