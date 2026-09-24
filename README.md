# Rytm

Rytm to aplikacja na Androida do planowania tygodnia. Pozwala tworzyć plany, przypisywać do nich zadania na konkretne dni i wybierać plan obowiązujący w danym tygodniu kalendarzowym.

Interfejs aplikacji jest dostępny po polsku i korzysta z jasnego, kremowego motywu z zielonym kolorem akcentu.

## Funkcje MVP

- tworzenie, edycja i usuwanie planów tygodnia,
- przypisywanie zadań do dni tygodnia,
- tytuł, notatka, godzina rozpoczęcia i zakończenia oraz kolor zadania,
- widok **Dziś** z oznaczaniem zadań jako wykonane,
- widok **Tydzień** z wyborem dnia i listą zadań uporządkowanych według godziny,
- widok **Plany** z oznaczeniem aktywnego planu,
- przypisywanie planu do bieżącego tygodnia,
- lokalny zapis danych w bazie SQLite przez Room,
- automatyczny wybór planu domyślnego, gdy tydzień nie ma osobnego przypisania,
- obsługa tygodni ISO rozpoczynających się w poniedziałek.

Po pierwszym uruchomieniu aplikacja tworzy przykładowy plan „Szkoła” z przykładowymi zadaniami.

## Technologie

- Kotlin,
- Jetpack Compose,
- Material 3,
- Room,
- ViewModel,
- Kotlin Coroutines,
- `StateFlow`,
- `java.time` i tygodnie ISO,
- Gradle Kotlin DSL.

## Architektura

Projekt korzysta z uproszczonej architektury MVVM:

```text
app/src/main/java/com/example/rytm/
├── MainActivity.kt
├── data/
│   ├── Converters.kt
│   ├── Daos.kt
│   ├── Entities.kt
│   ├── RytmDatabase.kt
│   └── RytmRepository.kt
└── ui/
    ├── RytmApp.kt
    └── RytmViewModel.kt
```

### Model danych

- `WeekPlan` — nazwa i kolor planu,
- `Task` — zadanie przypisane do planu i dnia tygodnia,
- `WeekAssignment` — przypisanie planu do tygodnia ISO.

Godziny zadań są przechowywane jako `LocalTime` z użyciem konwertera Room.

## Uruchomienie

Wymagane:

- Android Studio,
- JDK 11,
- Android SDK z API 37,
- urządzenie lub emulator z Androidem 7.0 albo nowszym.

Z poziomu katalogu projektu uruchom:

```bash
./gradlew assembleDebug
```

APK debug zostanie wygenerowany w:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Testy jednostkowe:

```bash
./gradlew test
```

## Nawigacja

Dolny pasek zawiera trzy główne zakładki:

- **Dziś** — zadania z bieżącego dnia aktywnego planu,
- **Tydzień** — wybór dnia i zadania w wybranym tygodniu,
- **Plany** — lista planów i wybór planu obowiązującego w bieżącym tygodniu.

Kliknięcie zadania otwiera formularz edycji. Zadania można oznaczać jako wykonane w widoku „Dziś”.

## Plan rozwoju

### Etap 2

- powiadomienia przed rozpoczęciem zadania,
- kopiowanie dnia lub całego planu,
- przypisywanie planów do przyszłych tygodni,
- rozbudowa edycji planu o wygodny wybór dnia.

### Etap 3

- widget na ekranie głównym,
- ciemny motyw,
- eksport i import kopii zapasowej,
- cykliczne powtarzanie planów co określoną liczbę tygodni.

## Licencja

Projekt jest aplikacją edukacyjno-produktywną rozwijaną lokalnie. Informacje o licencji można uzupełnić przed publiczną dystrybucją.
