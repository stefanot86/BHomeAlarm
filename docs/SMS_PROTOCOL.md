# Protocollo SMS - BHomeAlarm

## Overview

L'applicazione comunica con il sistema di allarme domestico tramite SMS. Questo documento descrive il protocollo di comunicazione completo.

---

## Formato Messaggi

### Struttura Generale

**Messaggio OUT (App → Allarme)**:
```
COMANDO[PARAMETRI]
```

**Messaggio IN (Allarme → App)**:
```
COMANDO:DATI&DATI&DATI[#|&]
```

### Separatori

| Carattere | Significato |
|-----------|-------------|
| `:` | Separa comando da dati |
| `&` | Separa campi dati, indica continuazione |
| `#` | Indica fine comunicazione |
| `=` | Assegna valore a campo |

---

## Comandi Configurazione (CONF1-5)

### CONF1 - Configurazione Base

**Request**:
```
CONF1?
```

**Response**:
```
CONF1:VV.VV&FLAGS.PPPP&Z1=nome1&Z2=nome2&...&Z8=nome8&
```

| Campo | Descrizione | Esempio |
|-------|-------------|---------|
| `VV.VV` | Versione firmware | `08.99` |
| `FLAGS` | Tipo configurazione | `MAIN` o `OTHER` |
| `PPPP` | Permessi (4 digit) | `1111` |
| `Zn=nome` | Nome zona n | `Z1=Ingresso` |

**Parsing FLAGS.PPPP**:
```
MAIN.1111
  │    │└── CMD_ON_OFF (1=abilitato)
  │    └─── VERIFY (1=abilitato)
  │    └──── RX2 (1=abilitato)
  │    └───── RX1 (1=abilitato)
  └────────── MAIN=tutti i permessi, OTHER=limitato
```

**Esempio completo**:
```
CONF1:08.99&MAIN.1111&Z1=Ingresso&Z2=Soggiorno&Z3=Cucina&Z4=Camera&Z5=Bagno&Z6=NE&Z7=NE&Z8=NE&
```

---

### CONF2 - Scenari (Parte 1)

**Request**:
```
CONF2?
```

**Response**:
```
CONF2:S01=nome1&S02=nome2&...&S08=nome8&
```

| Campo | Descrizione | Esempio |
|-------|-------------|---------|
| `Snn=nome` | Nome scenario nn | `S01=Casa` |

**Note**:
- Scenari 01-08
- `NE` = Not Enabled (scenario non configurato)

**Esempio**:
```
CONF2:S01=Casa&S02=Notte&S03=Fuori&S04=Vacanza&S05=NE&S06=NE&S07=NE&S08=NE&
```

---

### CONF3 - Scenari (Parte 2)

**Request**:
```
CONF3?
```

**Response**:
```
CONF3:S09=nome9&S10=nome10&...&S16=nome16&
```

| Campo | Descrizione | Esempio |
|-------|-------------|---------|
| `Snn=nome` | Nome scenario nn | `S09=Perimetrale` |

**Esempio**:
```
CONF3:S09=Perimetrale&S10=Volumetrico&S11=NE&S12=NE&S13=NE&S14=NE&S15=NE&S16=NE#
```

---

### CONF4 - Rubrica Utenti (Parte 1)

**Request**:
```
CONF4?
```

**Response**:
```
CONF4:R01=nome1&R02=nome2&...&R08=nome8&
```

| Campo | Descrizione | Esempio |
|-------|-------------|---------|
| `Rnn=nome` | Nome utente nn | `R01=Mario` |

**Esempio**:
```
CONF4:R01=Mario&R02=Anna&R03=Luca&R04=NE&R05=NE&R06=NE&R07=NE&R08=NE&
```

---

### CONF5 - Rubrica Utenti (Parte 2) + Joker

**Request**:
```
CONF5?
```

**Response**:
```
CONF5:R09=nome9&...&R16=nome16&RJO=joker#
```

| Campo | Descrizione | Esempio |
|-------|-------------|---------|
| `Rnn=nome` | Nome utente nn | `R09=Guest` |
| `RJO=nome` | Utente Joker | `RJO=Admin` |

**Esempio**:
```
CONF5:R09=Guest&R10=NE&R11=NE&R12=NE&R13=NE&R14=NE&R15=NE&R16=NE&RJO=Admin#
```

---

## Comandi Controllo

### ARM - Attivazione con Scenario

**Request**:
```
SCE:XX
```

| Parametro | Descrizione | Esempio |
|-----------|-------------|---------|
| `XX` | Numero scenario (01-16) | `SCE:01` |

**Response**:
```
OK:ARMED:scenario_name#
```
oppure
```
ERR:codice_errore#
```

---

### ARM Custom - Attivazione Zone Specifiche

**Request**:
```
CUST:ZZZZZZZZ
```

| Parametro | Descrizione | Esempio |
|-----------|-------------|---------|
| `ZZZZZZZZ` | 8 caratteri (0/1) per ogni zona | `CUST:11110000` |

**Esempio** (zone 1-4 attive):
```
CUST:11110000
```

**Response**:
```
OK:ARMED:CUSTOM#
```

---

### DISARM - Disattivazione

**Request**:
```
SYS OFF
```

**Response**:
```
OK:DISARMED#
```
oppure
```
ERR:codice_errore#
```

---

### STATUS - Verifica Stato

**Request**:
```
SYS?
```

**Response**:
```
STATUS:stato&dettagli#
```

| Stato | Significato |
|-------|-------------|
| `ARMED` | Sistema attivo |
| `DISARMED` | Sistema disattivo |
| `ALARM` | Allarme in corso |
| `TAMPER` | Manomissione rilevata |

**Esempio**:
```
STATUS:ARMED&SCE=Casa&ZONES=1234#
```

---

### SET - Configurazione Permessi Utente

**Request**:
```
SET:UXXPPPP
```

| Parametro | Descrizione | Esempio |
|-----------|-------------|---------|
| `XX` | Numero utente (01-16) | `01` |
| `PPPP` | Permessi (4 digit binari) | `1101` |

**Esempio** (utente 01, RX1+RX2+CMD):
```
SET:U011101
```

**Response**:
```
OK:SET:U01#
```

---

## State Machine Comunicazione

### Diagramma Stati

```
                    ┌─────────────┐
                    │    IDLE     │
                    └──────┬──────┘
                           │ startCommunication()
                           ▼
                    ┌─────────────┐
         ┌─────────│  PREPARING  │─────────┐
         │         └──────┬──────┘         │
         │                │ ready          │ error
         │                ▼                │
         │         ┌─────────────┐         │
         │         │   SENDING   │         │
         │         └──────┬──────┘         │
         │                │                │
         │    ┌───────────┼───────────┐    │
         │    │ sent      │ timeout   │    │
         │    ▼           │           ▼    │
         │ ┌─────────┐    │    ┌──────────┐│
         │ │ WAITING │    │    │  ERROR   ││
         │ │ RESPONSE│    │    └──────────┘│
         │ └────┬────┘    │           ▲    │
         │      │         │           │    │
         │      │ received│           │    │
         │      ▼         │           │    │
         │ ┌─────────┐    │           │    │
         │ │ PARSING │────┴───────────┘    │
         │ └────┬────┘                     │
         │      │ success                  │
         │      ▼                          │
         │ ┌─────────────┐                 │
         │ │  COMPLETE   │                 │
         │ └─────────────┘                 │
         │      │                          │
         │      │ hasNext?                 │
         │      ▼                          │
         └──────┴──────────────────────────┘
```

### Stati

| Stato | Descrizione | Timeout |
|-------|-------------|---------|
| `IDLE` | In attesa | - |
| `PREPARING` | Preparazione comando | 5s |
| `SENDING` | Invio SMS in corso | 10s |
| `WAITING_RESPONSE` | Attesa risposta | 60s |
| `PARSING` | Elaborazione risposta | 5s |
| `COMPLETE` | Operazione completata | - |
| `ERROR` | Errore | - |

---

## Sequenza Configurazione Completa

### Flusso CONF1-5

```
┌─────────┐                              ┌─────────┐
│   App   │                              │ Allarme │
└────┬────┘                              └────┬────┘
     │                                        │
     │  ──────── CONF1? ─────────────────►   │
     │                                        │
     │  ◄─────── CONF1:...& ─────────────    │
     │                                        │
     │  ──────── CONF2? ─────────────────►   │
     │                                        │
     │  ◄─────── CONF2:...& ─────────────    │
     │                                        │
     │  ──────── CONF3? ─────────────────►   │
     │                                        │
     │  ◄─────── CONF3:...& ─────────────    │
     │                                        │
     │  ──────── CONF4? ─────────────────►   │
     │                                        │
     │  ◄─────── CONF4:...& ─────────────    │
     │                                        │
     │  ──────── CONF5? ─────────────────►   │
     │                                        │
     │  ◄─────── CONF5:...# ─────────────    │
     │                                        │
     ▼                                        ▼
```

### Gestione Terminatori

| Terminatore | Significato | Azione |
|-------------|-------------|--------|
| `&` | Continua | Avanza a prossimo CONF |
| `#` | Fine | Termina sequenza |

---

## Parsing Risposte

### Parser CONF1

```java
public class SmsParser {

    public static ConfigData parseConf1(String response) {
        ConfigData data = new ConfigData();

        // Rimuovi prefisso "CONF1:"
        String content = response.substring(6);

        // Split per '&'
        String[] fields = content.split("&");

        for (String field : fields) {
            if (field.contains(".")) {
                // FLAGS.PPPP
                parseFlags(field, data);
            } else if (field.startsWith("Z")) {
                // Zn=nome
                parseZone(field, data);
            } else if (field.matches("\\d+\\.\\d+")) {
                // Versione
                data.version = field;
            }
        }

        return data;
    }

    private static void parseFlags(String field, ConfigData data) {
        // MAIN.1111 o OTHER.1111
        String[] parts = field.split("\\.");
        data.isMain = parts[0].equals("MAIN");

        if (parts.length > 1 && parts[1].length() == 4) {
            data.rx1 = parts[1].charAt(0) == '1';
            data.rx2 = parts[1].charAt(1) == '1';
            data.verify = parts[1].charAt(2) == '1';
            data.cmdOnOff = parts[1].charAt(3) == '1';
        }
    }

    private static void parseZone(String field, ConfigData data) {
        // Z1=Ingresso
        if (field.length() > 3 && field.charAt(1) >= '1' && field.charAt(1) <= '8') {
            int zoneIndex = field.charAt(1) - '1';
            String zoneName = field.substring(3);
            data.zones[zoneIndex] = zoneName;
        }
    }
}
```

### Parser CONF2/3 (Scenari)

```java
public static List<Scenario> parseConf2or3(String response, int startIndex) {
    List<Scenario> scenarios = new ArrayList<>();

    // Rimuovi prefisso "CONFx:"
    String content = response.substring(6);
    String[] fields = content.split("&");

    for (String field : fields) {
        if (field.startsWith("S") && field.contains("=")) {
            // Snn=nome
            int scenarioNum = Integer.parseInt(field.substring(1, 3));
            String scenarioName = field.substring(4);

            if (!scenarioName.equals("NE")) {
                Scenario s = new Scenario();
                s.setSlot(scenarioNum);
                s.setName(scenarioName);
                s.setEnabled(true);
                scenarios.add(s);
            }
        }
    }

    return scenarios;
}
```

### Parser CONF4/5 (Utenti)

```java
public static List<User> parseConf4or5(String response) {
    List<User> users = new ArrayList<>();

    String content = response.substring(6);
    String[] fields = content.split("&");

    for (String field : fields) {
        if (field.startsWith("R") && field.contains("=")) {
            String prefix = field.substring(0, 3);
            String name = field.substring(4);

            if (prefix.equals("RJO")) {
                // Joker user
                User u = new User();
                u.setSlot(0); // Special slot for joker
                u.setName(name);
                u.setJoker(true);
                users.add(u);
            } else if (prefix.matches("R\\d{2}")) {
                // Regular user Rnn
                int userNum = Integer.parseInt(prefix.substring(1));
                if (!name.equals("NE")) {
                    User u = new User();
                    u.setSlot(userNum);
                    u.setName(name);
                    u.setEnabled(true);
                    users.add(u);
                }
            }
        }
    }

    return users;
}
```

---

## Gestione Errori

### Codici Errore

| Codice | Descrizione | Azione Suggerita |
|--------|-------------|------------------|
| `E01` | Comando non riconosciuto | Verifica formato |
| `E02` | Parametro invalido | Verifica parametri |
| `E03` | Non autorizzato | Verifica permessi |
| `E04` | Sistema occupato | Riprova dopo |
| `E05` | Errore interno | Contatta supporto |
| `TIMEOUT` | Nessuna risposta | Verifica connettività |

### Timeout

| Fase | Timeout | Retry |
|------|---------|-------|
| Invio SMS | 10 sec | 2 volte |
| Attesa risposta | 60 sec | 1 volta |
| Parsing | 5 sec | No retry |

### Retry Logic

```java
public class RetryPolicy {
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 5000;

    public void executeWithRetry(Runnable action, Callback callback) {
        int attempts = 0;
        Exception lastError = null;

        while (attempts < MAX_RETRIES) {
            try {
                action.run();
                callback.onSuccess();
                return;
            } catch (Exception e) {
                lastError = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        callback.onError(lastError);
    }
}
```

---

## Normalizzazione Numeri Telefonici

### Problema

Il numero mittente può arrivare in diversi formati:
- `+393331234567`
- `3331234567`
- `0039 333 123 4567`

### Soluzione

```java
public class PhoneNumberUtils {

    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) return "";

        // Rimuovi spazi, trattini, parentesi
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Rimuovi prefisso internazionale italiano
        if (cleaned.startsWith("+39")) {
            cleaned = cleaned.substring(3);
        } else if (cleaned.startsWith("0039")) {
            cleaned = cleaned.substring(4);
        }

        return cleaned;
    }

    public static boolean matches(String number1, String number2) {
        String n1 = normalize(number1);
        String n2 = normalize(number2);

        // Confronta le ultime 9 cifre (per gestire variazioni)
        if (n1.length() >= 9 && n2.length() >= 9) {
            return n1.substring(n1.length() - 9)
                     .equals(n2.substring(n2.length() - 9));
        }

        return n1.equals(n2);
    }
}
```

---

## Test Mode

### Simulazione SMS

Per testing senza SIM/allarme reale:

```java
public class TestSmsService {

    private static final Map<String, String> MOCK_RESPONSES = new HashMap<>();

    static {
        MOCK_RESPONSES.put("CONF1?",
            "CONF1:08.99&MAIN.1111&Z1=Ingresso&Z2=Soggiorno&Z3=Cucina&Z4=Camera&Z5=NE&Z6=NE&Z7=NE&Z8=NE&");
        MOCK_RESPONSES.put("CONF2?",
            "CONF2:S01=Casa&S02=Notte&S03=Fuori&S04=NE&S05=NE&S06=NE&S07=NE&S08=NE&");
        MOCK_RESPONSES.put("CONF3?",
            "CONF3:S09=NE&S10=NE&S11=NE&S12=NE&S13=NE&S14=NE&S15=NE&S16=NE&");
        MOCK_RESPONSES.put("CONF4?",
            "CONF4:R01=Mario&R02=Anna&R03=NE&R04=NE&R05=NE&R06=NE&R07=NE&R08=NE&");
        MOCK_RESPONSES.put("CONF5?",
            "CONF5:R09=NE&R10=NE&R11=NE&R12=NE&R13=NE&R14=NE&R15=NE&R16=NE&RJO=Admin#");
        MOCK_RESPONSES.put("SYS OFF", "OK:DISARMED#");
        MOCK_RESPONSES.put("SYS?", "STATUS:DISARMED#");
    }

    public void simulateSend(String message, SmsCallback callback) {
        // Simula delay di rete
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String response = MOCK_RESPONSES.get(message);
            if (response != null) {
                callback.onReceived(response);
            } else if (message.startsWith("SCE:")) {
                callback.onReceived("OK:ARMED:" + message.substring(4) + "#");
            } else if (message.startsWith("CUST:")) {
                callback.onReceived("OK:ARMED:CUSTOM#");
            } else {
                callback.onError("E01");
            }
        }, 2000); // 2 secondi di delay simulato
    }
}
```

---

## Sicurezza

### Validazione Input

```java
public class SmsValidator {

    public static boolean isValidCommand(String command) {
        // Whitelist di comandi validi
        String[] validCommands = {
            "CONF1?", "CONF2?", "CONF3?", "CONF4?", "CONF5?",
            "SYS OFF", "SYS?"
        };

        for (String valid : validCommands) {
            if (command.equals(valid)) return true;
        }

        // Pattern per comandi parametrici
        if (command.matches("SCE:\\d{2}")) return true;
        if (command.matches("CUST:[01]{8}")) return true;
        if (command.matches("SET:U\\d{2}[01]{4}")) return true;

        return false;
    }

    public static boolean isValidResponse(String response) {
        // Verifica formato base
        if (response == null || response.isEmpty()) return false;
        if (response.length() > 500) return false; // Max length

        // Verifica caratteri permessi
        return response.matches("[A-Za-z0-9:&=#\\.\\s\\-]+");
    }
}
```

### No Logging Sensibile

```java
public class SecureLogger {

    public static void logSms(String direction, String content) {
        // Maschia numeri telefono nei log
        String masked = maskPhoneNumbers(content);
        Log.d("SMS", direction + ": " + masked);
    }

    private static String maskPhoneNumbers(String text) {
        // Sostituisci numeri con ***
        return text.replaceAll("\\+?\\d{10,}", "***PHONE***");
    }
}
```
