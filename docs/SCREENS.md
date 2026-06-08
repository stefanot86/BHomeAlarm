# Schermate UI - BHomeAlarm

## Design System

### Material Design 3

L'app utilizza Material Design 3 (Material You) con:

- **Dynamic Colors**: Colori estratti dal wallpaper (Android 12+)
- **Color Scheme**: Primary, Secondary, Tertiary, Surface, Error
- **Typography**: Roboto con scale MD3
- **Elevation**: Surface tint invece di ombre
- **Shape**: Rounded corners (small: 8dp, medium: 12dp, large: 16dp)

### Tema Colori Base

```xml
<!-- Light Theme -->
<color name="md_theme_light_primary">#006C4C</color>
<color name="md_theme_light_onPrimary">#FFFFFF</color>
<color name="md_theme_light_primaryContainer">#89F8C7</color>
<color name="md_theme_light_secondary">#4D6357</color>
<color name="md_theme_light_error">#BA1A1A</color>
<color name="md_theme_light_surface">#FBFDF8</color>

<!-- Dark Theme -->
<color name="md_theme_dark_primary">#6CDBAB</color>
<color name="md_theme_dark_onPrimary">#003826</color>
<color name="md_theme_dark_primaryContainer">#005138</color>
<color name="md_theme_dark_surface">#191C1A</color>
```

---

## Navigazione

### Flow Diagram

```
                    ┌─────────────────┐
                    │     SPLASH      │
                    └────────┬────────┘
                             │
              First run?     │
         ┌───────────────────┴───────────────────┐
         │ Yes                                   │ No
         ▼                                       │
┌─────────────────┐                              │
│   DISCLAIMER    │                              │
└────────┬────────┘                              │
         │ Accept                                │
         ▼                                       │
┌─────────────────┐                              │
│  SETUP_PHONE    │                              │
└────────┬────────┘                              │
         │                                       │
         └───────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │      MAIN       │◄─────────────────┐
                    └────────┬────────┘                  │
                             │                           │
         ┌───────────────────┼───────────────────┐      │
         │                   │                   │      │
         ▼                   ▼                   ▼      │
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  ARM (select    │ │    DISARM       │ │  CHECK STATUS   │
│   scenario)     │ │   (confirm)     │ │                 │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         └───────────────────┼───────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ CONFIG_PROGRESS │──────────────────┘
                    └─────────────────┘


                    ┌─────────────────┐
                    │    SETTINGS     │◄──── from MAIN
                    └────────┬────────┘
                             │
    ┌────────────────────────┼────────────────────────┐
    │                        │                        │
    ▼                        ▼                        ▼
┌──────────┐          ┌──────────┐            ┌──────────┐
│  USERS   │          │SCENARIOS │            │ CONFIG   │
└────┬─────┘          └────┬─────┘            └────┬─────┘
     │                     │                       │
     ▼                     ▼                       ▼
┌────────────┐        ┌──────────┐         ┌─────────────┐
│   USER     │        │  ZONES   │         │CONFIG_PROG  │
│PERMISSIONS │        │(custom)  │         │  (CONF1-5)  │
└────────────┘        └──────────┘         └─────────────┘
```

---

## Schermate Dettagliate

### 1. SplashFragment

**Scopo**: Schermata di avvio con branding

**Layout**:
```
┌────────────────────────────┐
│                            │
│                            │
│                            │
│         [  LOGO  ]         │
│                            │
│        BHomeAlarm          │
│                            │
│         v1.0.0             │
│                            │
│     ═══════════════        │ ← ProgressBar (indeterminate)
│                            │
│                            │
└────────────────────────────┘
```

**Componenti MD3**:
- `ImageView` per logo
- `MaterialTextView` per nome e versione
- `LinearProgressIndicator` (indeterminate)

**Comportamento**:
- Durata: 1.5 secondi
- Check permessi in background
- Navigazione automatica

---

### 2. DisclaimerFragment

**Scopo**: Accettazione termini d'uso

**Layout**:
```
┌────────────────────────────┐
│ ← Disclaimer               │ ← TopAppBar
├────────────────────────────┤
│                            │
│  ┌──────────────────────┐  │
│  │                      │  │
│  │  Terms of Use        │  │
│  │                      │  │
│  │  Lorem ipsum dolor   │  │
│  │  sit amet...         │  │ ← NestedScrollView
│  │                      │  │
│  │  ...                 │  │
│  │                      │  │
│  └──────────────────────┘  │
│                            │
├────────────────────────────┤
│  [ RIFIUTA ]  [ ACCETTA ]  │ ← ButtonBar
└────────────────────────────┘
```

**Componenti MD3**:
- `MediumTopAppBar`
- `NestedScrollView` con `MaterialTextView`
- `MaterialButton` (text per Rifiuta, filled per Accetta)

---

### 3. SetupPhoneFragment

**Scopo**: Inserimento numero telefonico allarme

**Layout**:
```
┌────────────────────────────┐
│ ← Numero Allarme           │
├────────────────────────────┤
│                            │
│    [  📱 icona phone  ]    │
│                            │
│  Inserisci il numero       │
│  del tuo sistema allarme   │
│                            │
│  ┌──────────────────────┐  │
│  │ Numero telefonico    │  │
│  │ +39 333 1234567    ▼ │  │ ← TextInputLayout (outlined)
│  └──────────────────────┘  │
│                            │
│  [ 📒 SELEZIONA DA RUBRICA ]│ ← OutlinedButton
│                            │
│                            │
├────────────────────────────┤
│         [ CONTINUA ]       │ ← FilledButton
└────────────────────────────┘
```

**Componenti MD3**:
- `TextInputLayout` (outlined style) con `TextInputEditText`
- `MaterialButton` outlined per rubrica
- `MaterialButton` filled per conferma
- Icona leading nel TextInputLayout

**Validazione**:
- Minimo 5 caratteri
- Solo numeri e simboli telefono (+, -, spazi)

---

### 4. MainFragment

**Scopo**: Menu principale con azioni rapide

**Layout**:
```
┌────────────────────────────┐
│  BHomeAlarm            ⚙️  │ ← TopAppBar con settings
├────────────────────────────┤
│                            │
│  ┌──────────────────────┐  │
│  │ 🔴  Sistema          │  │
│  │     DISATTIVO        │  │ ← Status Card
│  │     Ultimo check: 10:30│ │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ 🔒  ATTIVA ALLARME   │  │
│  │     Seleziona scenario │ │ ← Action Card
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ 🔓  DISATTIVA        │  │
│  │     Spegni sistema    │ │ ← Action Card
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ ❓  VERIFICA STATO   │  │
│  │     Controlla sistema │ │ ← Action Card
│  └──────────────────────┘  │
│                            │
└────────────────────────────┘
```

**Componenti MD3**:
- `LargeTopAppBar` con menu action
- `MaterialCardView` per status (con tint color dinamico)
- `MaterialCardView` clickable per azioni
- Leading icon in ogni card
- Ripple effect sui click

**Status Card Colors**:
- Verde: Sistema attivo
- Rosso: Sistema disattivo
- Giallo: Stato sconosciuto
- Grigio: Non configurato

---

### 7. SettingsFragment

**Scopo**: Menu impostazioni

**Layout**:
```
┌────────────────────────────┐
│ ← Impostazioni             │
├────────────────────────────┤
│                            │
│  CONFIGURAZIONE            │ ← Section header
│  ┌──────────────────────┐  │
│  │ 📱 Numero allarme    │  │
│  │    +39 333 1234567  >│  │
│  ├──────────────────────┤  │
│  │ 🔄 Configura sistema │  │
│  │    Scarica CONF1-5  >│  │
│  └──────────────────────┘  │
│                            │
│  GESTIONE                  │
│  ┌──────────────────────┐  │
│  │ 👥 Utenti            │  │
│  │    Gestisci rubrica >│  │
│  ├──────────────────────┤  │
│  │ 📋 Scenari           │  │
│  │    16 scenari       >│  │
│  └──────────────────────┘  │
│                            │
│  INFO                      │
│  ┌──────────────────────┐  │
│  │ ℹ️ Informazioni       │  │
│  │    Versione 1.0.1   >│  │
│  └──────────────────────┘  │
│                            │
└────────────────────────────┘
```

**Componenti MD3**:
- `RecyclerView` con section headers
- `MaterialCardView` raggruppate
- Stile preference-like
- Dividers tra items

---

### 8. ConfigProgressFragment

**Scopo**: Mostra progresso comunicazione SMS

**Layout**:
```
┌────────────────────────────┐
│ ← Configurazione           │
├────────────────────────────┤
│                            │
│  ┌──────────────────────┐  │
│  │                      │  │
│  │   ████████░░░░░░░░   │  │ ← LinearProgressIndicator
│  │        60%           │  │
│  │                      │  │
│  │   Ricevendo CONF3... │  │
│  │                      │  │
│  └──────────────────────┘  │
│                            │
│  Dettaglio operazioni:     │
│  ┌──────────────────────┐  │
│  │ ✅ CONF1 - Completato│  │
│  │ ✅ CONF2 - Completato│  │
│  │ 🔄 CONF3 - In corso  │  │ ← RecyclerView
│  │ ⏳ CONF4 - In attesa │  │
│  │ ⏳ CONF5 - In attesa │  │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ TX: CONF3?           │  │
│  │ RX: CONF3:S09=...    │  │ ← Debug panel (collapsible)
│  └──────────────────────┘  │
│                            │
├────────────────────────────┤
│        [ ANNULLA ]         │
└────────────────────────────┘
```

**Componenti MD3**:
- `LinearProgressIndicator` (determinate)
- `RecyclerView` per lista step
- `MaterialCardView` collapsible per debug
- Icone per stato step (check, loading, waiting, error)

**Stati Step**:
- ⏳ Pending (grigio)
- 🔄 In Progress (blu, animated)
- ✅ Complete (verde)
- ❌ Error (rosso)

---

### 9. UsersFragment

**Scopo**: Lista utenti rubrica allarme

**Layout**:
```
┌────────────────────────────┐
│ ← Utenti                   │
├────────────────────────────┤
│                            │
│  ┌──────────────────────┐  │
│  │ 👤 Mario Rossi       │  │
│  │    RX1 RX2 CMD      >│  │ ← Chips per permessi
│  ├──────────────────────┤  │
│  │ 👤 Anna Verdi        │  │
│  │    RX1 VERIFY       >│  │
│  ├──────────────────────┤  │
│  │ 👤 Luca Bianchi      │  │
│  │    RX1 RX2 VERIFY CMD│ │
│  ├──────────────────────┤  │
│  │ 👤 (Vuoto)           │  │ ← Slot vuoto
│  │                     >│  │
│  └──────────────────────┘  │
│                            │
│        ...                 │
│                            │
└────────────────────────────┘
```

**Componenti MD3**:
- `RecyclerView` con `ListItem`
- `Chip` piccoli per mostrare permessi attivi
- Leading avatar/icon
- Trailing chevron

---

### 10. UserPermissionsFragment

**Scopo**: Modifica permessi singolo utente

**Layout**:
```
┌────────────────────────────┐
│ ← Mario Rossi              │
├────────────────────────────┤
│                            │
│  PERMESSI                  │
│  ┌──────────────────────┐  │
│  │ [✓] Ricevi notifiche │  │
│  │     tipo 1 (RX1)     │  │
│  ├──────────────────────┤  │
│  │ [✓] Ricevi notifiche │  │
│  │     tipo 2 (RX2)     │  │
│  ├──────────────────────┤  │
│  │ [ ] Verifica stato   │  │
│  │     (VERIFY)         │  │
│  ├──────────────────────┤  │
│  │ [✓] Comandi ON/OFF   │  │
│  │     (CMD)            │  │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ [ ] Applica a tutti  │  │ ← Checkbox speciale
│  │     gli utenti       │  │
│  └──────────────────────┘  │
│                            │
├────────────────────────────┤
│  [ ANNULLA ]   [ SALVA ]   │
└────────────────────────────┘
```

**Componenti MD3**:
- `CheckBox` per ogni permesso
- Descrizione sotto ogni checkbox
- Switch o Checkbox per "applica a tutti"

---

### 11. ScenariosFragment

**Scopo**: Lista scenari disponibili

**Layout**:
```
┌────────────────────────────┐
│ ← Scenari                  │
├────────────────────────────┤
│                            │
│  SCENARI PREDEFINITI       │
│  ┌──────────────────────┐  │
│  │ 🏠 Casa              │  │
│  │    Zone: 1,2,3,4    >│  │
│  ├──────────────────────┤  │
│  │ 🌙 Notte             │  │
│  │    Zone: 1,2,5,6    >│  │
│  ├──────────────────────┤  │
│  │ 🚗 Fuori Casa        │  │
│  │    Zone: tutte      >│  │
│  └──────────────────────┘  │
│                            │
│  PERSONALIZZATO            │
│  ┌──────────────────────┐  │
│  │ ⚙️ Crea scenario     │  │
│  │    Seleziona zone   >│  │
│  └──────────────────────┘  │
│                            │
└────────────────────────────┘
```

**Componenti MD3**:
- `RecyclerView` con sezioni
- Card speciale per scenario custom
- Icona per tipo scenario

---

### 12. ZonesFragment (Crea Scenario)

**Scopo**: Creazione scenario personalizzato con nome e selezione zone

**Layout**:
```
┌────────────────────────────┐
│ ← Crea Scenario            │
├────────────────────────────┤
│                            │
│  ┌──────────────────────┐  │
│  │ Nome scenario        │  │
│  │ [__________________ ]│  │ ← TextInputLayout (outlined)
│  └──────────────────────┘  │
│                            │
│  Seleziona le zone da      │
│  includere nello scenario  │
│                            │
│  ┌──────────────────────┐  │
│  │ [✓] Zona 1 - Ingresso│  │
│  ├──────────────────────┤  │
│  │ [✓] Zona 2 - Soggiorno│ │
│  ├──────────────────────┤  │
│  │ [ ] Zona 3 - Cucina  │  │
│  ├──────────────────────┤  │
│  │ [✓] Zona 4 - Camera  │  │
│  ├──────────────────────┤  │
│  │ [ ] Zona 5 - Bagno   │  │
│  ├──────────────────────┤  │
│  │ ...                  │  │
│  └──────────────────────┘  │
│                            │
├────────────────────────────┤
│ 3 zone selezionate         │
│ [ ANNULLA ]   [ SALVA ]    │
└────────────────────────────┘
```

**Componenti MD3**:
- `TextInputLayout` (outlined) per nome scenario
- `NestedScrollView` con `RecyclerView` per zone
- `MaterialButton` per Annulla (text) e Salva (filled)
- Count zone selezionate nella bottom bar

**Flusso**:
1. Utente inserisce nome scenario
2. Seleziona le zone desiderate
3. Click "Salva" (abilitato solo se nome + almeno 1 zona)
4. Scenario salvato nel database locale
5. Dialog "Armare ora?" con opzioni Sì/No
6. Se Sì → invia `CUST:NNN` e torna a Home
7. Se No → torna a Home senza armare

---

## Widget Home Screen

### AlarmWidget (AlarmWidgetProvider)

**Scopo**: Attivare/disattivare l'allarme dalla home screen senza aprire l'app.

**Layout** (~4x2 celle):
```
┌────────────────────────────┐
│ BHomeAlarm                 │ ← titolo
│ DISATTIVO                  │ ← stato (colore: verde/rosso/arancione)
│ Ultimo controllo: 10:30    │ ← ora ultimo aggiornamento
│                            │
│ ┌──────────┐  ┌──────────┐ │
│ │ 🔒 Attiva│  │🔓 Disatt.│ │ ← due bottoni
│ └──────────┘  └──────────┘ │
└────────────────────────────┘
```

**Note tecniche**:
- Costruito con `RemoteViews` (no `MaterialCardView`/`ConstraintLayout`/`RecyclerView`).
- Sfondo arrotondato `@drawable/widget_background` (varianti chiaro/scuro via `values`/`values-night`).
- Bottone Attiva su `colorPrimary`, Disattiva su `colorError`; icone `ic_lock` / `ic_lock_open`.
- Ogni bottone è un `PendingIntent` verso `AlarmActionActivity` con extra `Constants.WIDGET_EXTRA_MODE`.
- Lo stato si aggiorna quando la centrale risponde (`SmsReceiver` → `AlarmWidgetProvider.updateAllWidgets`).

### AlarmActionActivity (mini-schermata translucida)

**Scopo**: Mini-finestra mostrata sopra la home quando si tocca un bottone del widget.

- **Modalità ARM**: `MaterialAlertDialog` con la lista degli scenari abilitati → al tocco invia il comando e chiude.
- **Modalità DISARM**: `MaterialAlertDialog` di conferma → su conferma invia `SYS OFF` e chiude.
- Tema `Theme.BHomeAlarm.Transparent` (finestra trasparente, dialog Material 3 sopra la home).
- Delega l'invio ad `AlarmController`; gestisce numero non configurato e permesso `SEND_SMS` mancante.

```
       (home screen sullo sfondo)
   ┌────────────────────────────┐
   │     Attiva Allarme         │
   ├────────────────────────────┤
   │  Casa                      │
   │  Notte                     │  ← lista scenari (MODE_ARM)
   │  Fuori Casa                │
   ├────────────────────────────┤
   │                  [ ANNULLA]│
   └────────────────────────────┘
```

---

## Dialogs

### SimSelectionDialog

**Scopo**: Selezione SIM su dispositivi dual-SIM

```
┌────────────────────────────┐
│     Seleziona SIM          │
├────────────────────────────┤
│                            │
│  ○ SIM 1 - Vodafone        │
│     +39 333 1234567        │
│                            │
│  ● SIM 2 - TIM             │
│     +39 347 9876543        │
│                            │
│  [✓] Ricorda scelta        │
│                            │
├────────────────────────────┤
│ [ ANNULLA ]   [ INVIA ]    │
└────────────────────────────┘
```

**Componenti**: `AlertDialog` con `RadioGroup`

### ConfirmationDialog

**Scopo**: Conferma azioni critiche (es. Disarm)

```
┌────────────────────────────┐
│     Conferma               │
├────────────────────────────┤
│                            │
│  Vuoi disattivare          │
│  l'allarme?                │
│                            │
├────────────────────────────┤
│ [ ANNULLA ]  [ DISATTIVA ] │
└────────────────────────────┘
```

### DeleteScenarioDialog

**Scopo**: Conferma eliminazione scenario (trigger: long press)

```
┌────────────────────────────┐
│     Elimina Scenario       │
├────────────────────────────┤
│                            │
│  Eliminare lo scenario     │
│  "Casa"?                   │
│                            │
├────────────────────────────┤
│ [ ANNULLA ]  [ ELIMINA ]   │
└────────────────────────────┘
```

### ArmAfterSaveDialog

**Scopo**: Chiede se armare dopo salvataggio scenario

```
┌────────────────────────────┐
│     Armare ora?            │
├────────────────────────────┤
│                            │
│  Lo scenario "Notte" è     │
│  stato salvato. Vuoi       │
│  attivare l'allarme        │
│  adesso?                   │
│                            │
├────────────────────────────┤
│ [ NO ]         [ SI ]      │
└────────────────────────────┘
```

---

## Notifiche

### Layout Notifica Standard

```
┌────────────────────────────────────────┐
│ 🔔 BHomeAlarm              10:30       │
│                                        │
│ Allarme Attivato                       │
│ Sistema armato con scenario "Casa"     │
│                                        │
│ [ VERIFICA ]  [ DISATTIVA ]            │
└────────────────────────────────────────┘
```

### Canali Notifica (Android 8+)

| Canale ID | Nome | Importance |
|-----------|------|------------|
| `alarm_status` | Stato Allarme | HIGH |
| `alarm_errors` | Errori | HIGH |
| `config_progress` | Configurazione | DEFAULT |

---

## Transizioni e Animazioni

### Transizioni Fragment

- **Enter**: Slide from right (300ms)
- **Exit**: Slide to left (300ms)
- **Pop Enter**: Slide from left
- **Pop Exit**: Slide to right

### Animazioni UI

- **Card press**: Ripple + slight scale down
- **Status change**: Color fade (500ms)
- **Progress**: Smooth animation
- **List items**: Staggered fade-in

### Motion

```xml
<!-- res/anim/slide_in_right.xml -->
<translate
    android:fromXDelta="100%"
    android:toXDelta="0%"
    android:duration="300"
    android:interpolator="@android:interpolator/decelerate_cubic"/>
```

---

## Responsive Design

### Breakpoints

| Dispositivo | Width | Layout |
|-------------|-------|--------|
| Phone portrait | < 600dp | Single column |
| Phone landscape | < 840dp | Single column |
| Tablet | >= 600dp | Two columns (optional) |

### Adaptive Layouts

Per tablet, considerare:
- Master-detail per Users/Scenarios
- Side navigation rail invece di bottom nav
- Più contenuto visibile

---

## Accessibilità

### Content Descriptions

Ogni elemento interattivo deve avere:
- `contentDescription` per screen readers
- Label visibili per input
- Contrasto colori WCAG AA

### Focus Order

Ordine logico di navigazione con tastiera/TalkBack

### Touch Targets

Minimo 48dp x 48dp per elementi clickabili
