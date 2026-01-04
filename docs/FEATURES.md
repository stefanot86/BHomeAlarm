# Funzionalità BHomeAlarm

## Panoramica

BHomeAlarm è un'applicazione Android per il controllo remoto di sistemi di allarme domestico tramite SMS.

---

## Funzionalità Core (Ereditate)

### 1. Controllo Allarme

| Funzione | Comando SMS | Descrizione |
|----------|-------------|-------------|
| **Attiva (Arm)** | `SCE:XX` o `CUST:...` | Attiva l'allarme con scenario selezionato |
| **Disattiva (Disarm)** | `SYS OFF` | Disattiva completamente l'allarme |
| **Verifica Stato** | `SYS?` | Richiede lo stato corrente del sistema |

#### Scenari di Attivazione
- **Scenari Predefiniti**: 16 scenari configurabili dall'allarme (S01-S16)
- **Scenario Personalizzato**: Selezione manuale delle zone da attivare

### 2. Configurazione Sistema

#### Sequenza CONF1-CONF5
L'app recupera la configurazione completa dell'allarme tramite 5 messaggi:

| Config | Contenuto |
|--------|-----------|
| CONF1 | Versione firmware, flag abilitazioni, nomi zone |
| CONF2 | Nomi scenari (parte 1: S01-S08) |
| CONF3 | Nomi scenari (parte 2: S09-S16) |
| CONF4 | Rubrica utenti (parte 1: R01-R08) |
| CONF5 | Rubrica utenti (parte 2: R09-R16, RJO=Joker) |

### 3. Gestione Zone

- **8 zone** configurabili
- Ogni zona ha:
  - Nome (ricevuto da CONF1)
  - Stato attivo/disattivo (per scenari custom)
- Visualizzazione lista zone con checkbox per selezione

### 4. Gestione Scenari

- **16 scenari** predefiniti
- Ogni scenario ha:
  - Nome (ricevuto da CONF2/CONF3)
  - Maschera zone associate
- Possibilità di creare scenario "Custom" selezionando zone manualmente

### 5. Gestione Utenti (Rubrica)

- **16 utenti** nella rubrica dell'allarme
- Ogni utente ha:
  - Nome (ricevuto da CONF4/CONF5)
  - Permessi configurabili:
    - **RX1**: Riceve notifiche tipo 1
    - **RX2**: Riceve notifiche tipo 2
    - **VERIFY**: Può verificare stato
    - **CMD_ON_OFF**: Può attivare/disattivare

### 6. Supporto Dual-SIM

- Rilevamento automatico dispositivi dual-SIM
- Dialog di selezione SIM prima dell'invio
- Memorizzazione SIM preferita
- Compatibile con Android 5.1+ (API 22)

### 7. Persistenza Dati

- Salvataggio locale di:
  - Numero telefonico allarme
  - Configurazione zone/scenari/utenti
  - Preferenze utente
  - Flag sistema (disclaimer accettato, ecc.)

---

## Nuove Funzionalità

### 9. Notifiche Push

**Descrizione**: Notifiche Android quando l'app riceve risposte dall'allarme.

#### Tipi di Notifica

| Tipo | Trigger | Priorità |
|------|---------|----------|
| Risposta Allarme | SMS ricevuto dall'allarme | Alta |
| Stato Cambiato | Allarme armato/disarmato | Media |
| Errore Comunicazione | Timeout o errore SMS | Alta |
| Configurazione Completa | CONF1-5 completati | Bassa |

#### Caratteristiche
- **Canale dedicato** per Android 8+ (Oreo)
- **Suono e vibrazione** configurabili
- **Azione rapida** dalla notifica (es. "Verifica Stato")
- **Icone distintive** per tipo di notifica

#### Implementazione

```java
// Canale notifiche
NotificationChannel channel = new NotificationChannel(
    "alarm_responses",
    "Risposte Allarme",
    NotificationManager.IMPORTANCE_HIGH
);

// Notifica con azione
NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_responses")
    .setSmallIcon(R.drawable.ic_alarm)
    .setContentTitle("Allarme Attivato")
    .setContentText("Sistema armato con scenario Casa")
    .addAction(R.drawable.ic_status, "Verifica", pendingIntent);
```

---

## Funzionalità UI/UX

### 10. Material Design 3

- **Dynamic Colors**: Colori adattati al wallpaper (Android 12+)
- **Tema scuro**: Supporto automatico dark mode
- **Componenti MD3**:
  - TopAppBar con scrolling behavior
  - FloatingActionButton Extended
  - Cards con elevation
  - Chips per selezione
  - BottomSheet per opzioni
  - Snackbar per feedback

### 11. Feedback Visivo

- **Progress indicator** durante comunicazione SMS
- **Status indicator** colorato (verde/rosso/giallo)
- **Animazioni** di transizione tra schermate
- **Toast/Snackbar** per conferme azioni

### 12. Localizzazione

Lingue supportate:
- Italiano (it)
- Inglese (en) - default
- Francese (fr)

---

## Requisiti Funzionali Dettagliati

### RF-001: Setup Iniziale

**Precondizioni**: Prima installazione o dati resettati

**Flusso**:
1. Mostra splash screen con logo e versione
2. Mostra schermata Disclaimer
3. Se accettato, richiedi numero telefonico allarme
4. Naviga a schermata principale

**Postcondizioni**: App configurata per l'uso

---

### RF-002: Attivazione Allarme

**Precondizioni**:
- Numero allarme configurato
- Configurazione caricata (CONF1 minimo)

**Flusso**:
1. Utente seleziona "Attiva" dal menu
2. Mostra lista scenari disponibili
3. Utente seleziona scenario (o "Personalizzato")
4. Se dual-SIM, mostra dialog selezione SIM
5. Invia comando SMS
6. Mostra progress durante attesa
7. Ricevi e processa risposta
8. Mostra notifica e aggiorna UI

**Comandi SMS**:
- Scenario predefinito: `SCE:XX` (XX = numero scenario)
- Scenario custom: `CUST:ZZZZZZZZ` (Z = 0/1 per ogni zona)

**Postcondizioni**: Allarme attivato, log salvato

---

### RF-003: Disattivazione Allarme

**Flusso simile a RF-002**

**Comando SMS**: `SYS OFF`

---

### RF-004: Verifica Stato

**Flusso simile a RF-002**

**Comando SMS**: `SYS?`

**Risposta attesa**: Stato corrente del sistema

---

### RF-005: Configurazione Automatica

**Precondizioni**: Numero allarme configurato

**Flusso**:
1. Utente avvia "Configura" da Settings
2. Mostra schermata progresso
3. **Loop per CONF1-5**:
   - Invia comando `CONFx?`
   - Attendi risposta (timeout 60s)
   - Parsa risposta
   - Aggiorna database
   - Aggiorna UI progresso
4. Mostra riepilogo configurazione
5. Salva dati

**Gestione errori**:
- Timeout: Riprova o Annulla
- Errore parsing: Segnala e continua
- Errore generico: Mostra dettagli

---

### RF-006: Gestione Utenti

**Flusso**:
1. Mostra lista utenti da rubrica
2. Click su utente → schermata permessi
3. Modifica checkbox permessi
4. "Applica a tutti" opzionale
5. Invia comando SET all'allarme

**Comando SMS**: `SET:UXPPPP` (U=user, P=permessi)

---

### RF-007: Notifiche Push

**Trigger**: SMS ricevuto da numero allarme

**Flusso**:
1. BroadcastReceiver intercetta SMS
2. Verifica mittente = numero allarme
3. Parsa contenuto
4. Crea notifica appropriata
5. Mostra notifica con suono/vibrazione
6. Aggiorna UI se app in foreground

---

## Requisiti Non Funzionali

### RNF-001: Compatibilità

- **Min SDK**: 23 (Android 6.0 Marshmallow)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 35 (Android 15)
- **Copertura**: ~99% dispositivi Android attivi

### RNF-002: Performance

- Avvio app: < 2 secondi
- Invio SMS: < 1 secondo
- Parsing risposta: < 500ms
- Transizioni UI: 60 fps

### RNF-003: Affidabilità

- Gestione graceful di errori SMS
- Retry automatico con backoff
- Persistenza stato anche su crash
- Logging per debug

### RNF-004: Sicurezza

- Nessun dato sensibile in log
- Permessi minimi necessari
- Validazione input

### RNF-005: Usabilità

- UI intuitiva senza manuale
- Feedback per ogni azione
- Messaggi errore comprensibili
- Accessibilità (TalkBack support)

---

## Matrice Funzionalità vs Schermate

| Funzionalità | Schermate Coinvolte |
|--------------|---------------------|
| Setup iniziale | Splash, Disclaimer, SetupPhone |
| Controllo allarme | Main, ConfigProgress |
| Visualizza config | Settings, Scenarios, Zones, Users |
| Modifica permessi | Users, UserPermissions |
| Scenario custom | Scenarios, Zones |
| Notifiche | (Background) |

---

## Priorità Implementazione

### Fase 1 - MVP
1. Struttura progetto e navigazione
2. Setup numero telefonico
3. Invio/ricezione SMS base
4. Configurazione CONF1 (versione + zone)
5. Comando Arm/Disarm semplice
6. Persistenza Room base

### Fase 2 - Core Features
1. Configurazione completa CONF1-5
2. Lista scenari e selezione
3. Scenario personalizzato con zone
4. Supporto Dual-SIM
5. Gestione utenti e permessi

### Fase 3 - Polish
1. Notifiche push
2. Material Design 3 completo
3. Localizzazione
4. Gestione errori avanzata

### Fase 4 - Extra
1. Widget home screen
2. Backup cloud
3. Statistiche utilizzo
4. Temi personalizzati
