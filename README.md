# BHomeAlarm

App Android per la gestione della centrale antifurto Bticino 3500/3500N tramite SMS.

## Funzionalita Principali

- **Controllo Allarme**: Attiva, disattiva e verifica lo stato del sistema
- **Scenari**: 16 scenari predefiniti + scenario personalizzato
- **Zone**: Gestione fino a 8 zone configurabili
- **Utenti**: Rubrica con 16 utenti e permessi configurabili
- **Dual-SIM**: Supporto dispositivi dual-SIM
- **Notifiche**: Notifiche push per le risposte dell'allarme

## Requisiti

- Android 6.0 (API 23) o superiore
- Permessi SMS (invio e ricezione)
- SIM card attiva
- Centrale Bticino 3500 o 3500N con modulo GSM

## Architettura

Il progetto segue il pattern **MVC (Model-View-Controller)**:

```
it.bhomealarm/
├── model/          # Entities Room, DAOs, Repository
├── view/           # Activities, Fragments, Adapters
├── controller/     # Business logic controllers
├── service/        # SMS e Notification services
├── util/           # Utilities e helpers
└── callback/       # Interfaces
```

## Stack Tecnologico

- **Linguaggio**: Java
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **UI**: Material Design 3
- **Database**: Room
- **Build System**: Gradle

## Documentazione

Consulta la cartella `docs/` per la documentazione completa:

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - Architettura MVC dettagliata
- [FEATURES.md](docs/FEATURES.md) - Lista funzionalita
- [SCREENS.md](docs/SCREENS.md) - Design UI/UX
- [DATA_MODEL.md](docs/DATA_MODEL.md) - Modello dati Room
- [SMS_PROTOCOL.md](docs/SMS_PROTOCOL.md) - Protocollo comunicazione SMS
- [MIGRATION.md](docs/MIGRATION.md) - Piano di sviluppo

## Build

```bash
# Build debug
./gradlew assembleDebug

# Installa su dispositivo
./gradlew installDebug
```

## Stato del Progetto

**Issue #1 - Scheletro progetto: COMPLETATO**

### Completato
- [x] Model Layer (entities, DAOs, database, repository)
- [x] Utility Layer (Constants, SmsParser, PhoneNumberUtils, PermissionHelper)
- [x] Callback Interfaces
- [x] ViewModels (MVVM pattern)
- [x] Service Layer (SmsService, SmsReceiver, NotificationService)
- [x] Layout XML per tutti i Fragment
- [x] Risorse (strings, colors, themes Material Design 3)
- [x] Navigation Graph con animazioni
- [x] Icone vector drawable

### Da Implementare
- [ ] Adapter RecyclerView (Users, Scenarios, Zones, ConfigSteps, SmsLog)
- [ ] Completare Fragment (collegare UI ai ViewModel)
- [ ] Fragment mancanti (Splash, Disclaimer, SetupPhone)
- [ ] Dialogs (SimSelection, Confirmation, PhoneInput)
- [ ] Collegamento SMS reale nei ViewModel
- [ ] Registrazione receiver in AndroidManifest
- [ ] Testing

Vedi [docs/TODO.md](docs/TODO.md) per la lista dettagliata.
