# Gameplay Design Specification: Tower Attack

- **Status:** verbindliche Grundlage für den ersten spielbaren Slice
- **Ziel:** klare Spielidee, rendererfreie Implementierung und automatisierte
  Balancing-Simulation
- **Arbeitsprinzip:** Die Regeln in diesem Dokument sind fest. Zahlenwerte mit dem Hinweis
  *Startwert* werden nach Simulationen angepasst, ohne den Kernloop zu verändern.

## 1. Produktkern

### 1.1 Elevator Pitch

**Tower Attack ist ein kurzes, deterministisches Angriffs-Puzzle.** Der Spieler sieht eine
bereits aufgebaute Verteidigung, stellt aus einem knappen Budget eine kleine Angriffswelle
zusammen und bestimmt die Startabstände der Einheiten. Nach dem Start läuft der Angriff
ohne weitere Eingriffe ab. Gewonnen wird nicht durch höhere Werte, sondern dadurch, dass
eine robuste Einheit Turmfeuer bindet und schnelle Einheiten die entstehenden
Cooldown-Fenster ausnutzen.

Die zentrale Frage lautet:

> Welche Einheiten sende ich in welcher Reihenfolge und mit welchen Abständen, damit die
> gesamte Welle die Verteidigung durchbricht?

### 1.2 Spielerfantasie

Der Spieler ist kein Feldkommandant, der jede Figur einzeln steuert, sondern ein
Angriffsplaner. Er liest Reichweiten und Feuerrhythmen, formuliert einen Plan und beobachtet,
ob seine Vorhersage aufgeht. Eine Niederlage soll neue Information liefern und unmittelbar
zu einem verbesserten Versuch führen.

### 1.3 Designpfeiler

1. **Lesen vor Handeln:** Verteidigung, Reichweiten, Zielregeln und Cooldowns sind vor dem
   Start bekannt.
2. **Rollen statt Wertetreppen:** Jede Unit erfüllt eine erkennbare Aufgabe. Keine Unit ist
   lediglich schneller, robuster und dadurch immer besser.
3. **Timing ist eine Ressource:** Schon mit derselben Zusammenstellung können gute und
   schlechte Startabstände zu unterschiedlichen Ergebnissen führen.
4. **Kurze Ursache-Wirkungs-Kette:** Ein Versuch dauert im ersten Slice unter 15 Sekunden
   und kann sofort verändert und wiederholt werden.
5. **Determinismus:** Derselbe Plan im selben Szenario liefert immer dasselbe Ergebnis.
   Zufall darf später nur als ausdrücklich aktivierte Szenariovariation hinzukommen.

### 1.4 Bewusster Umfang des ersten Slice

Enthalten sind:

- eine feste Route;
- eine vorgegebene, vollständig sichtbare Verteidigung;
- zwei klar unterschiedliche Unit-Typen;
- höchstens drei Deployments;
- Budget und Startzeitpunkte als Planungsentscheidungen;
- automatische Ausführung, Auswertung und Wiederholung;
- eine rendererfreie Simulation als einzige Regelinstanz.

Nicht enthalten sind zunächst:

- Bauen oder Aufrüsten von Türmen während des Angriffs;
- zufällige Entscheidungen der Verteidigungs-KI;
- Routenwahl, aktive Fähigkeiten, Heilung, Status-Effekte oder Projektillaufzeiten;
- Meta-Progression, Deckbau oder freischaltbare Werte;
- Eingriffe des Spielers während der Ausführungsphase.

Diese Begrenzung ist eine Produktentscheidung, keine technische Einschränkung. Erst wenn
Reihenfolge und Timing mit zwei Unit-Rollen tragen, wird der Entscheidungsraum erweitert.

## 2. Kernloop

Ein Versuch besteht aus vier Phasen:

### 2.1 Verteidigung lesen

Der Spieler sieht Route, Basis, Türme, deren Reichweiten, Schaden, Cooldown und Zielregel.
Das Spiel versteckt im ersten Slice keine kampfrelevante Information.

### 2.2 Angriff planen

Der Spieler füllt bis zu drei Slots mit Units. Für jede Unit wird ein Startzeitpunkt auf
einem Zeitraster gewählt. Das UI zeigt jederzeit Gesamtkosten, verbleibendes Budget und die
sortierte Startreihenfolge. Ein Plan kann vor dem Start beliebig geändert werden.

### 2.3 Angriff ausführen

Mit **Angriff starten** wird der Plan fixiert. Units bewegen sich automatisch, Türme wählen
Ziele und feuern nach den unten definierten Regeln. Währenddessen gibt es im ersten Slice
keine spielmechanische Eingabe; Pause und Geschwindigkeitswahl sind reine
Darstellungsfunktionen.

### 2.4 Ergebnis verstehen und iterieren

Das Spiel zeigt Sieg oder Niederlage sowie mindestens:

- welche Units durchgebrochen oder gefallen sind;
- wann und auf wen jeder Turm geschossen hat;
- verbleibende Basisintegrität;
- den letzten verwendeten Plan.

Mit **Plan anpassen** kehrt der Spieler mit unverändertem Plan in die Planungsphase zurück.
Mit **Wiederholen** wird exakt derselbe Plan erneut ausgeführt und muss dasselbe Ergebnis
liefern.

```text
Verteidigung lesen -> Welle und Timing planen -> automatisch ausführen
        ^                                              |
        |-------------- Ergebnis auswerten ------------|
```

## 3. Formale Spielregeln

### 3.1 Zustände eines Versuchs

```text
PLANNING -> RUNNING -> VICTORY
                    -> DEFEAT
                    -> TIMEOUT
```

- Nur in `PLANNING` darf der Deployment-Plan verändert werden.
- `RUNNING` beginnt mit einem validierten, unveränderlichen Plan.
- `VICTORY` tritt sofort ein, wenn die gegnerische Basisintegrität 0 erreicht.
- `DEFEAT` tritt ein, wenn alle geplanten Units aufgelöst sind und die Basis noch
  Integrität besitzt.
- `TIMEOUT` tritt ein, wenn das Zeitlimit erreicht ist. Für die Spielerwertung zählt dies
  als Niederlage, bleibt für Diagnose und Simulation aber als eigene Ursache sichtbar.
- Ein Neustart erzeugt einen frischen Simulationszustand aus demselben Szenario und Plan.

### 3.2 Raum und Route

- Die Kampflogik arbeitet auf einer eindimensionalen, ganzzahligen
  `routeProgressUnits`-Achse. `1` fachliche Fortschrittseinheit entspricht verbindlich
  `6.000` Routenuntereinheiten (`ROUTE_UNITS_PER_PROGRESS = 6000`).
- Der Start liegt bei `0`, die gegnerische Basis bei `routeLengthUnits`. Auch
  Turmpositionen und Reichweiten werden als ganze Routenuntereinheiten gespeichert.
- Jede Unit besitzt eine ganzzahlige Geschwindigkeit `speedUnitsPerTick` und bewegt sich
  pro Simulationsschritt exakt um diesen Wert vorwärts. Für die Startwerte ergeben sich
  damit ohne Rundung `150` Routenuntereinheiten pro Tick für `BULWARK` und `350` für
  `RUNNER`.
- `routeProgressUnits`, `routeLengthUnits`, `routePositionUnits`, `rangeUnits` und
  `speedUnitsPerTick` verwenden einen ausreichend breiten Ganzzahltyp. In der Regellogik
  sind weder Fließkommazahlen noch ein laufendes Aufsummieren von `deltaTime` zulässig.
- Die visuelle Kachelroute ist nur eine Abbildung der `routeProgressUnits`-Position auf die
  Weltkoordinaten. Kurven verändern im ersten Slice weder Tempo noch Kampfwerte.
- Eine Unit durchbricht die Verteidigung, sobald
  `routeProgressUnits >= routeLengthUnits` gilt.

Damit bleibt das Regelmodell unabhängig von LibGDX und kann dieselbe gerade oder
gekrümmte Route ohne Sonderfälle simulieren.

### 3.3 Deployment-Plan

Ein Deployment enthält `unitTypeId`, `spawnTick` und eine stabile `deploymentId`.

Für den ersten Slice gelten:

- ein bis drei Deployments pro Plan;
- jedes Deployment verbraucht einen Slot;
- die Summe aller Unit-Kosten darf das Szenariobudget nicht überschreiten;
- Startzeiten liegen zwischen 0 und 6 Sekunden;
- Startzeiten sind in Schritten von 0,25 Sekunden wählbar;
- die früheste Unit startet immer bei 0 Sekunden; UI und Plansuche verschieben einen Plan
  vor der Validierung automatisch entsprechend nach vorne;
- mehrere Units dürfen gleichzeitig starten;
- bei gleicher Startzeit entscheidet `deploymentId` stabil über die Reihenfolge.

Ein ungültiger Plan wird nicht teilweise ausgeführt, sondern mit konkreten
Validierungsfehlern abgelehnt, beispielsweise `BUDGET_EXCEEDED`, `TOO_MANY_DEPLOYMENTS`
oder `SPAWN_OUTSIDE_WINDOW`.

### 3.4 Unit-Rollen

Die Namen beschreiben Rollen und sind die fachlichen Identitäten. Vorhandene 3D-Modelle
können zunächst als Platzhalter weiterverwendet werden.

| Unit | Fachliche ID | Aufgabe | Kosten | Leben | Tempo | Basisschaden |
|---|---|---|---:|---:|---:|---:|
| Bollwerk | `BULWARK` | Läuft voraus und absorbiert mehrere Schüsse. | 3 | 200 | 1,5 | 1 |
| Läufer | `RUNNER` | Nutzt ein Cooldown-Fenster, stirbt aber durch einen Treffer. | 1 | 50 | 3,5 | 1 |

Alle Zahlen sind **Startwerte** für das erste Referenzszenario. Fest sind dagegen die
Rollenbeziehungen:

- `BULWARK` ist langsam, teuer und überlebt mehrere Treffer;
- `RUNNER` ist schnell, günstig und durch einen einzelnen Treffer gefährdet;
- Units greifen Türme nicht an und blockieren einander nicht;
- es gibt im ersten Slice keine Rüstung, Resistenzen, Kollisionen oder aktiven Fähigkeiten.

### 3.5 Turmverhalten

Jeder Turm besitzt `routePositionUnits`, `rangeUnits`, `damage`, `cooldownTicks` und eine
Zielregel. Ein Turm deckt den ganzzahligen, beidseitig eingeschlossenen Bereich
`[routePositionUnits - rangeUnits, routePositionUnits + rangeUnits]` ab.

Wenn ein Turm feuerbereit ist:

1. Er betrachtet alle lebenden Units in Reichweite.
2. Er wählt die Unit mit dem größten `routeProgressUnits`, also die der Basis am nächsten
   ist.
3. Bei gleichem Fortschritt gewinnt die kleinere `deploymentId`.
4. Der Treffer wird sofort angewendet.
5. Danach ist der Turm für seinen vollen Cooldown nicht feuerbereit.

Ein Turm reserviert kein Ziel zwischen zwei Schüssen. Stirbt oder verlässt eine Unit die
Reichweite, wird beim nächsten Schuss normal neu gewählt. Visuelles Drehen,
Mündungsfeuer und Geschosse dürfen den Treffer darstellen, aber nicht verzögern oder die
Regelentscheidung verändern.

Für Reichweiten- und Gleichzeitigkeit-Grenzfälle gelten zusätzlich diese verbindlichen
Regeln:

- Eine Unit exakt auf der unteren oder oberen Reichweitengrenze ist in Reichweite; es gibt
  keine Epsilon-Toleranz.
- Reichweite wird einmal pro Tick nach der Bewegung anhand der neuen ganzzahligen Position
  geprüft. Eine Unit, die den gesamten Bereich zwischen zwei Tickpositionen überspringt,
  war in keinem Tick in Reichweite und kann in diesem Intervall nicht beschossen werden.
- `UNIT_ENTERED_TOWER_RANGE` entsteht beim Zustandswechsel von außerhalb zu innerhalb,
  einschließlich eines erstmaligen Eintritts direkt auf einer Grenze. Eine neu erzeugte
  Unit gilt vor ihrer ersten Bewegung als außerhalb; maßgeblich ist ihre Position nach der
  Bewegung des Spawn-Ticks.
- Feuert ein Turm in Tick `t`, ist er erstmals in Tick `t + cooldownTicks` wieder
  feuerbereit, nicht bereits einen Tick früher.
- Mehrere feuerbereite Türme handeln nacheinander in stabiler `towerId`-Reihenfolge. Eine
  durch einen früheren Turm auf `health <= 0` gebrachte Unit ist für alle weiteren Türme
  desselben Ticks sofort kein gültiges Ziel mehr, auch wenn sie erst in Schritt 5 entfernt
  wird.

### 3.6 Basis, Sieg und Niederlage

- Jede durchgebrochene Unit reduziert die Basisintegrität um ihren `baseDamage`.
- Überschüssiger Schaden ist erlaubt; die Integrität wird für die Anzeige bei 0 begrenzt.
- Eine tote Unit verursacht keinen Basisschaden.
- Für das Referenzszenario besitzt die Basis 3 Integrität. Bei maximal drei Units müssen
  deshalb alle drei durchbrechen.
- Das Ergebnis wird nur einmal gesetzt und ist danach unveränderlich.

### 3.7 Zeit und deterministische Ereignisreihenfolge

Die Simulation verwendet feste Ticks von `1/60 s`. Zeitkritische Werte werden als ganze
Ticks gespeichert; beispielsweise sind 0,25 Sekunden 15 Ticks und 1,6 Sekunden 96 Ticks.
Ein Headless-Lauf darf Ticks schneller als Echtzeit berechnen, aber nicht überspringen.
Räumliche Werte folgen ergänzend dem Ganzzahlmodell aus Abschnitt 3.2. Dezimale
Autorenwerte werden genau einmal beim Erzeugen der Specs in Routenuntereinheiten pro Tick
umgerechnet: `speedUnitsPerTick = speedProgressPerSecond * 6000 / 60`. Dabei wird der
Dezimalwert als exakte Dezimalzahl beziehungsweise als Bruch verarbeitet, nicht zunächst
als Float, und das Ergebnis muss ohne Rest ganzzahlig sein; andernfalls wird die Spec
abgelehnt. Während eines Laufs findet keine Float-Konvertierung oder Rundung statt.

Innerhalb jedes Ticks gilt exakt diese Reihenfolge:

1. fällige Units in stabiler `deploymentId`-Reihenfolge erzeugen;
2. alle lebenden Units in stabiler `deploymentId`-Reihenfolge bewegen; betritt eine Unit
   dabei mehrere Turmreichweiten, werden die Eintrittsereignisse in stabiler
   `towerId`-Reihenfolge protokolliert;
3. Durchbrüche in stabiler `deploymentId`-Reihenfolge markieren, Basisschaden anwenden und
   diese Units entfernen; erreicht die Basis dabei 0, endet der Tick sofort mit Sieg;
4. feuerbereite Türme in stabiler `towerId`-Reihenfolge Ziele wählen und Schaden anwenden;
5. Units mit `health <= 0` in stabiler `deploymentId`-Reihenfolge als gefallen markieren
   und entfernen;
6. Niederlage, Timeout oder Fortsetzung bestimmen;
7. alle während des Ticks bereits in Regelreihenfolge protokollierten Domain-Events für
   die Ausgabe freigeben.

Diese Reihenfolge ist Teil der Spielregel. Sie verhindert, dass Framerate, Listenreihenfolge
oder Renderer unterschiedliche Ergebnisse erzeugen.

Damit sind gleichzeitige Grenzfälle ebenfalls eindeutig: Eine in Tick `t` erzeugte Unit
bewegt sich bereits in Tick `t`. Erreicht eine Unit in einem Tick zugleich die Basis und
eine mögliche Schussposition, wird sie in Schritt 3 zuerst als Durchbruch verarbeitet und
kann in diesem Tick nicht mehr beschossen werden. Mehrere gleichzeitige Durchbrüche werden
in `deploymentId`-Reihenfolge angewendet; sobald die Basisintegrität 0 erreicht, endet der
Tick, und später einsortierte Durchbrüche dieses Ticks werden nicht mehr verarbeitet.
Gleichzeitig erzeugte Units und Fortschrittsgleichstände werden wie in Abschnitt 3.3 und
3.5 über `deploymentId` aufgelöst. `VICTORY` aus Schritt 3 hat auch am Zeitlimit Vorrang;
falls in Schritt 6 gleichzeitig die Bedingungen für `DEFEAT` und `TIMEOUT` erfüllt sind,
hat `DEFEAT` gemäß der dort angegebenen Prüfungsreihenfolge Vorrang.

## 4. Erstes Referenzszenario: „Das Zeitfenster“

Dieses Szenario ist Tutorial, automatischer Akzeptanztest und erste Balancing-Sandbox.

### 4.1 Szenariowerte

| Parameter | Startwert |
|---|---:|
| Routenlänge | 10 Fortschrittseinheiten |
| Spielerbudget | 5 Command Points |
| Deployment-Slots | 3 |
| Planungsfenster | 0 bis 6 Sekunden |
| Zeitlimit | 15 Sekunden |
| Basisintegrität | 3 |

Die Verteidigung besteht aus einem Turm:

| Turm | Position | Reichweite | Schaden | Cooldown | Anfangszustand |
|---|---:|---:|---:|---:|---|
| Wächter `GUARDIAN_1` | 5 | 2 | 50 | 1,6 s | feuerbereit |

Im Simulationsmodell entsprechen diese dargestellten Fachwerte exakt
`routeLengthUnits = 60000`, `routePositionUnits = 30000` und `rangeUnits = 12000`.

### 4.2 Beabsichtigte Lernerfahrung

Der Wächter tötet einen Läufer mit einem Schuss. Das Bollwerk kann dagegen in die
Reichweite laufen, einen Schuss auslösen und während des Cooldowns ein Fenster für einen
Läufer öffnen. Der Spieler muss beide Läufer in passende Fenster schicken, ohne dass sie
das Bollwerk zu früh überholen und selbst zum vordersten Ziel werden.

Ein plausibler Gewinnplan mit den Startwerten lautet:

| Zeit | Deployment/Ereignis |
|---:|---|
| 0,00 s | Bollwerk startet. |
| 1,50 s | Erster Läufer startet. |
| ca. 2,00 s | Wächter schießt auf das Bollwerk. |
| 3,00 s | Zweiter Läufer startet. |
| ca. 3,60 s | Wächter schießt erneut auf das Bollwerk. |
| ca. 4,35 s | Erster Läufer erreicht die Basis. |
| ca. 5,85 s | Zweiter Läufer erreicht die Basis. |
| ca. 6,65 s | Bollwerk erreicht die Basis; der Angriff gewinnt. |

Die Zeiten nach dem Start sind erwartete Simulationswerte und dürfen bei der technischen
Umsetzung nur um höchstens einen Tick abweichen.

### 4.3 Akzeptanzfälle

Die Startwerte gelten erst dann als bestätigt, wenn eine automatisierte Simulation diese
Verhaltensziele erfüllt:

| Plan | Erwartung | Begründung |
|---|---|---|
| `BULWARK@0`, `RUNNER@1,5`, `RUNNER@3,0` | Sieg | Beide Läufer nutzen vom Bollwerk erzeugte Fenster. |
| `BULWARK@0`, `RUNNER@1,0`, `RUNNER@1,5` | Niederlage | Läufer überholen zu früh und werden getroffen. |
| `RUNNER@0`, `RUNNER@1,5`, `RUNNER@3,0` | Niederlage | Es fehlt eine Unit, die mehrere Schüsse absorbiert. |
| nur `BULWARK` + `RUNNER` | kein Gesamtsieg | Zwei Durchbrüche reichen nicht gegen 3 Basisintegrität. |
| zwei `BULWARK` | ungültig | Kosten 6 übersteigen Budget 5. |
| identischer Plan, 100 Wiederholungen | identische Ergebnisse | Der Kern enthält keinen Zufall. |

Die Tabelle definiert Verhalten, nicht die endgültigen Zahlen. Falls die Startwerte einen
Akzeptanzfall verfehlen, werden Werte zentral in den Specs angepasst und nicht durch
Sonderregeln im Controller korrigiert.

### 4.4 Bestätigte Batchprüfung der Startwerte

**Status: durch `PlanEnumerator` und `CombatSimulation` bestätigt.** Die ursprüngliche
Fixed-Tick-Plausibilitätsrechnung war mangels Skript nicht reproduzierbar. Ihre Zahlen
werden deshalb nicht als Quelle weitergeführt, stimmen aber mit der inzwischen im Projekt
enthaltenen Prüfung der echten Regelinstanz überein.

Die Referenzfamilie setzt `BULWARK` fest auf Tick 0. Die beiden gleichen Läufer erhalten
je einen Starttick aus den 25 Rasterpunkten von Tick 0 bis 360 einschließlich und werden
vertauschungsfrei kanonisiert. Damit entstehen exakt
`25 * 26 / 2 = 325` verschiedene Timing-Pläne. Die vollständige Suche erzeugt zusätzlich
alle kanonischen, budgetgültigen Pläne aus ein bis drei Units, bei denen mindestens eine
Unit in Tick 0 startet.

Die Prüfung ist reproduzierbar mit:

```shell
sh gradlew core:test --tests 'de.codecrunch.gameplay.simulation.*'
```

Bestätigte Ergebnisse:

| Kanonische Zusammenstellung | Pläne | Siege | Niederlagen | Timeouts |
|---|---:|---:|---:|---:|
| nur `BULWARK` | 1 | 0 | 1 | 0 |
| nur `RUNNER` | 1 | 0 | 1 | 0 |
| `BULWARK + RUNNER` | 49 | 0 | 49 | 0 |
| `RUNNER + RUNNER` | 25 | 0 | 25 | 0 |
| `BULWARK + RUNNER + RUNNER` | 925 | 21 | 904 | 0 |
| `RUNNER + RUNNER + RUNNER` | 325 | 0 | 325 | 0 |
| **Gesamt** | **1.326** | **21** | **1.305** | **0** |

Innerhalb der engeren Referenzfamilie mit `BULWARK@0` gewinnen genau **21 von 325**
Timing-Varianten; 304 verlieren. Damit sind auch die ursprünglich genannten Varianten
`BULWARK@0, RUNNER@1,25, RUNNER@3,0` und
`BULWARK@0, RUNNER@1,5, RUNNER@2,75` bestätigt. Ein automatisch gefundener robuster
Nachbarschaftsfall ist `BULWARK@0, RUNNER@2,25, RUNNER@2,25`: Er gewinnt weiterhin, wenn
einer der Läufer auf 2,50 Sekunden verschoben wird.

Für den Referenzplan bestätigt die Simulation außerdem die Turmschüsse in Tick 119 und
215 sowie die Durchbrüche in Tick 261, 351 und 399. Alle 1.326 Pläne lieferten in jeweils
100 Wiederholungen denselben Event-Log-Hash; schrittweise und vollständige Ausführung sowie
umgekehrte Eingabereihenfolgen der Spec-Collections lieferten ebenfalls identische Hashes.
Alle Balancing-Gates aus Abschnitt 6.6 sind damit erfüllt. Eine Anpassung der zentralen
Startwerte war nicht erforderlich.

## 5. Lesbarkeit und Bedienung

### 5.1 Planungsansicht

Vor dem Start müssen ohne versteckte Untermenüs sichtbar sein:

- Budget, verwendete Kosten und freie Slots;
- Unit-Karten mit Rolle, Kosten, Leben und Tempo;
- eine drei Zeilen große Deployment-Timeline;
- Startzeit jeder Unit und Abstände zur vorherigen Unit;
- Reichweitenfläche des ausgewählten Turms;
- Schaden, Cooldown und Zielpriorität des Turms;
- Basisintegrität und benötigte Zahl an Durchbrüchen.

Die kleinste brauchbare Bedienung besteht aus Unit-Auswahl, drei Slot-Zeilen,
`-0,25 s`/`+0,25 s` pro Startzeit und **Angriff starten**. Drag-and-drop ist optional.

### 5.2 Ausführungsansicht

- Units sind nach Rolle und Gesundheitszustand unterscheidbar.
- Ein Turmschuss zeigt klar Ziel, Treffer und Beginn des Cooldowns.
- Ein Cooldown-Ring oder Balken macht das nächste Schussfenster sichtbar.
- Durchbruch und Tod erzeugen unterschiedliche, kurze Rückmeldungen.
- Die Timeline besitzt einen Abspielkopf, damit Planung und beobachtetes Timing verbunden
  bleiben.

### 5.3 Ergebnisansicht

Die Ergebnisansicht nennt nicht nur „verloren“, sondern zeigt eine knappe Diagnose, etwa:

- „Läufer 1 wurde bei 3,45 s getroffen.“
- „Das Bollwerk hatte beim zweiten Schuss das Feuer nicht mehr gebunden.“
- „2 von 3 benötigten Units sind durchgebrochen.“

Diagnosen werden aus Domain-Events abgeleitet. Sie verändern keine Regeln und müssen für
die erste technische Version noch nicht alle denkbaren Fälle erklären.

## 6. Rendererfreie Simulationsdomäne

### 6.1 Eine Regelinstanz

Es darf keine getrennte „vereinfachte Simulation“ neben dem eigentlichen Spiel geben.
Headless-Sandbox, automatisierte Tests und visuelle Combat-Demo verwenden dieselbe
`CombatSimulation`. LibGDX liest Zustände und Events, entscheidet aber weder Ziele noch
Schaden, Bewegung oder Ergebnis.

```text
ScenarioSpec + DeploymentPlan
              |
              v
      CombatSimulation  <---- einzige Regelinstanz
          |        |
          |        +---- runToCompletion() -> SimulationResult
          |
          +------------- advanceTicks(n) -> State + DomainEvents -> Renderer/HUD
```

### 6.2 Minimales Datenmodell

Empfohlene unveränderliche Eingaben:

```text
ScenarioSpec
  scenarioId
  routeLengthUnits
  baseIntegrity
  budget
  maxDeployments
  maxSpawnTick
  timeLimitTicks
  unitSpecs[]
  towerSpecs[]

UnitSpec
  unitTypeId
  cost
  maxHealth
  speedUnitsPerTick
  baseDamage

TowerSpec
  towerId
  routePositionUnits
  rangeUnits
  damage
  cooldownTicks
  targetRule

DeploymentPlan
  deployments[]

Deployment
  deploymentId
  unitTypeId
  spawnTick
```

Veränderlicher Laufzeitzustand:

```text
SimulationState
  currentTick
  baseIntegrity
  status
  activeUnits[]
  towerStates[]
  eventLog[]
```

Das Ergebnis enthält mindestens:

```text
SimulationResult
  scenarioId
  normalizedPlan
  result                 // VICTORY, DEFEAT, TIMEOUT, INVALID_PLAN
  endTick
  remainingBaseIntegrity
  spentBudget
  unitOutcomes[]
  towerStatistics[]
  events[]
```

Für Meilenstein 1 sind davon das fachliche Ergebnis, End-Tick, verbleibende
Basisintegrität und der vollständige Event-Log verbindlich. Komfort- und Statistikfelder
wie `towerStatistics` dürfen erst in Meilenstein 2 aus den Events abgeleitet werden.

### 6.3 Domain-Events

Mindestens diese Ereignisse werden mit `tick` und stabiler Objekt-ID protokolliert:

- `UNIT_SPAWNED`
- `UNIT_ENTERED_TOWER_RANGE`
- `TOWER_FIRED`
- `UNIT_DAMAGED`
- `UNIT_DEFEATED`
- `UNIT_BREACHED`
- `BASE_DAMAGED`
- `SIMULATION_FINISHED`

`TOWER_FIRED` enthält Turm, Ziel, Schaden und Zielposition. Damit lassen sich Ergebnisse
erklären, visuell abspielen und später statistisch auswerten, ohne die Simulation erneut zu
interpretieren.

### 6.4 Automatische Plansuche

Für ein Szenario mit zwei Unit-Typen und drei Slots wird der kleine Suchraum vollständig
durchlaufen:

1. alle Sequenzen aus ein bis drei Units erzeugen;
2. Pläne über Budget verwerfen;
3. ersten Spawn auf Tick 0 normalisieren;
4. weitere Startzeiten im 0,25-Sekunden-Raster bis 6 Sekunden kombinieren;
5. jeden kanonischen Plan einmal simulieren;
6. Resultate nach Zusammenstellung und Timing gruppieren.

Für genau drei Units ergeben sich bei zwei Typen und 25 möglichen Folgezeitpunkten
höchstens `2^3 * 25^2 = 5.000` Rohkombinationen. Das ist klein genug für jeden lokalen
Testlauf. Doppelte Pläne mit gleichzeitig identischen Units werden vor der Ausführung
kanonisiert. Der implementierte Enumerator sortiert dazu nach `spawnTick` und `unitTypeId`,
vergibt anschließend stabile Deployment-IDs und verwirft budgetungültige Pläne über den
`PlanValidator`. Für das Referenzszenario verbleiben reproduzierbar 1.326 gültige
kanonische Pläne; die genaue Verteilung steht in Abschnitt 4.4.

### 6.5 Kennzahlen

Die folgenden Kennzahlen sind für eine spätere, umfangreiche Balancing-Sandbox nützlich,
aber nachrangig. Die schlanke Batch-Auswertung aus Meilenstein 2 hält pro Plan nur Ergebnis,
End-Tick, verbleibende Basisintegrität und Event-Log-Hash sowie aggregierte
Zusammenstellungszähler im Speicher. Exporte und die weiterführenden Kennzahlen bleiben
bewusst vertagt.

Pro Plan werden gespeichert:

- Sieg/Niederlage und verbleibende Basisintegrität;
- Endzeit und Zeitpunkt jedes Durchbruchs;
- Lebenspunkte und Ausgang jeder Unit;
- Schüsse und verursachter Schaden je Turm;
- Ziel jedes Schusses;
- eingesetztes und ungenutztes Budget;
- Zeit jeder Unit in Turmreichweite;
- Abstand des Plans zum nächsten Gewinnplan, gemessen in geänderten 0,25-s-Schritten.

Über alle Pläne eines Szenarios werden berechnet:

- Gewinnquote insgesamt und pro Unit-Zusammenstellung;
- Zahl verschiedener gewinnender Sequenzen;
- Größe zusammenhängender Gewinnfenster;
- häufigste Todesursache und gefährlichster Turm;
- Anteil der Pläne, deren Ergebnis sich durch eine Änderung von nur 0,25 Sekunden ändert.

### 6.6 Balancing-Gates für ein gutes Puzzle

Das Referenzszenario wird nur für den visuellen Slice freigegeben, wenn:

1. mindestens ein Plan gewinnt und mindestens ein gültiger Plan verliert;
2. dieselbe Zusammenstellung `BULWARK + RUNNER + RUNNER` abhängig vom Timing sowohl
   gewinnen als auch verlieren kann;
3. `RUNNER + RUNNER + RUNNER` nicht gewinnt;
4. es mehr als einen Gewinnplan gibt, damit keine einzelne Tick-Kombination erraten werden
   muss;
5. mindestens ein Gewinnplan noch gewinnt, wenn ein Läufer um `-0,25 s` oder `+0,25 s`
   verschoben wird;
6. 100 Wiederholungen jedes Plans exakt denselben Event-Log-Hash erzeugen;
7. kein Ergebnis von Render-Framerate oder Reihenfolge einer ungeordneten Collection
   abhängt.

Die Sandbox misst damit Lösbarkeit, Robustheit und Dominanz. Ob das Puzzle verständlich
und befriedigend ist, muss anschließend ein kurzer menschlicher Playtest klären.
Die automatisierte Prüfung aus Abschnitt 4.4 erfüllt alle sieben Gates mit den
unveränderten Startwerten.

## 7. Implementierungsleitlinie für das bestehende Projekt

### 7.1 Zielarchitektur

Die neue Domäne kann unter `de.codecrunch.gameplay.simulation` entstehen und darf keine
Abhängigkeit auf `com.badlogic.gdx.*`, Views oder Controller besitzen.

Empfohlene erste Klassen:

```text
simulation/spec/ScenarioSpec.java
simulation/spec/UnitSpec.java
simulation/spec/TowerSpec.java
simulation/plan/Deployment.java
simulation/plan/DeploymentPlan.java
simulation/plan/PlanValidator.java
simulation/CombatSimulation.java
simulation/SimulationState.java
simulation/SimulationResult.java
simulation/SimulationEvent.java
simulation/PlanEnumerator.java
```

`CombatSimulation` bietet zwei Zugänge zur identischen Logik:

- `advanceOneTick()` für die schrittweise visuelle Ausführung;
- `runToCompletion()` für Tests und Batch-Simulationen.

### 7.2 Umgang mit vorhandenem Code

Im aktuellen Stand existieren bereits nützliche Vorarbeiten: eine validierte Route,
Bewegung entlang der Route, Unit-Leben, Turmreichweite, stabile Zielauswahl, Budget,
Deployment-Slots und Wave-Ergebnisse. Sie liefern Adapter und Tests, sollen aber nicht zu
einer zweiten Regelquelle werden.

Für die Umstellung gilt:

- `M_Map`/`UnitRoute` liefert die Geometrie und eine Abbildung zwischen
  `routeProgressUnits` und Weltposition.
- `M_SmallUnit` und `M_FastUnit` sind bisher technische Prototypen. Die fachlichen Specs
  `BULWARK` und `RUNNER` werden zentral definiert; Klassenname oder altes Modell bestimmen
  keine Kampfwerte mehr.
- `TowerCombat`, `PathUnit`, Cooldowns und Ergebnislogik gehen schrittweise in
  `CombatSimulation` auf.
- `C_Game` nimmt validierte Pläne entgegen, treibt die Simulation über einen festen
  Zeit-Akkumulator voran und reicht Events an `V_Game`/HUD weiter.
- Die zufällige Turmplatzierung aus `C_Computer` bleibt für das Referenzszenario
  deaktiviert. Später darf sie Szenarien erzeugen, aber nicht während eines laufenden
  deterministischen Angriffs Regeln verändern.
- Turmdrehung und Mündungsfeuer bleiben visuelle Reaktionen auf `TOWER_FIRED` und dürfen
  den Trefferzeitpunkt nicht festlegen.

### 7.3 Technische Invarianten

- Specs und Deployment-Pläne sind nach Erstellung unveränderlich.
- Jede Unit und jeder Turm besitzt innerhalb einer Simulation eine stabile ID.
- Regellogik iteriert nur über stabil sortierte Collections.
- Geld kann nicht negativ werden; tote Units können weder getroffen werden noch
  durchbrechen.
- Eine Unit kann genau einen finalen Ausgang besitzen: `BREACHED` oder `DEFEATED`.
- Die Basis erhält pro Unit höchstens einmal Schaden.
- Die Simulation erzeugt genau ein Abschlussereignis.
- Ein kompletter Lauf benötigt keine Grafik-, Audio- oder Plattforminitialisierung.

### 7.4 Testpyramide

1. **Regeltests:** Bewegung, Reichweitengrenzen, Ziel-Tiebreaker, Cooldown, Schaden,
   Durchbruch und Event-Reihenfolge jeweils isoliert.
2. **Szenariotests:** die sechs Akzeptanzfälle aus Abschnitt 4.3.
3. **Eigenschaftstests:** kein negatives Leben/Budget, nur ein Unit-Ausgang, identische
   Eingabe erzeugt identischen Event-Log-Hash.
4. **Batch-Test:** alle gültigen Pläne des Referenzszenarios laufen unter einem kurzen
   Laufzeitlimit und erfüllen die Balancing-Gates.
5. **Adaptertest:** eine Headless- und eine schrittweise Ausführung desselben Plans liefern
   dasselbe Ergebnis.

Die Regeltests decken mindestens folgende Grenzfälle mit exakten Tick- und
Routenuntereinheiten ab:

- Position exakt auf unterer beziehungsweise oberer Reichweitengrenze ist in Reichweite;
  eine Routenuntereinheit davor beziehungsweise dahinter ist außerhalb.
- Überspringen des vollständigen Reichweitenintervalls zwischen zwei Ticks löst weder
  Zielwahl noch `UNIT_ENTERED_TOWER_RANGE` aus.
- Ein Schuss in Tick `t` erlaubt den nächsten Schuss exakt in `t + cooldownTicks`, nicht in
  `t + cooldownTicks - 1`.
- Gleichzeitig erzeugte und gleich weit fortgeschrittene Units werden unabhängig von der
  Eingabelistenreihenfolge nach `deploymentId` erzeugt und als Ziel priorisiert.
- Ein gleichzeitiger Durchbruch und möglicher Turmschuss endet für diese Unit mit
  `BREACHED`; es gibt danach im Tick keinen Treffer auf sie.
- Bei mehreren Durchbrüchen im selben Tick bestimmt `deploymentId` die Schadensreihenfolge
  und damit, welches Event unmittelbar vor `VICTORY` liegt.
- Bei mehreren feuerbereiten Türmen bestimmt `towerId` die Schussreihenfolge; ein bereits
  tödlich getroffenes Ziel wird vom nächsten Turm nicht erneut gewählt.
- Ein Durchbruch am Zeitlimit endet als `VICTORY`; treffen ohne Durchbruch im selben Tick
  sowohl `DEFEAT` als auch `TIMEOUT` zu, endet der Lauf als `DEFEAT`.
- Derselbe Plan liefert über `runToCompletion()` und schrittweises `advanceOneTick()` einen
  bytegleich kanonisierten Event-Log.

### 7.5 Offene Adapterpunkte nach Meilenstein 3

Die Planungsansicht der Combat Demo verwendet bereits `ScenarioSpec`, `UnitSpec` und
`PlanValidator` aus der deterministischen Domäne. Nach `Angriff starten` hält `C_Game` den
normalisierten, unveränderlichen `DeploymentPlan`; eine `CombatSimulation` wird in
Meilenstein 3 bewusst noch nicht erzeugt oder visuell abgespielt. Der normale Spielmodus
und die bisherige automatische Demo bleiben davon unberührt.

Für Meilenstein 4 sind damit folgende Adapterarbeiten klar abgegrenzt:

- Aus dem akzeptierten Plan und demselben `ScenarioSpec` wird genau eine neue
  `CombatSimulation` erzeugt. Die bisherige `PathUnit`-/`TowerCombat`-Regellogik darf im
  Referenzlauf nicht parallel weiterlaufen.
- `C_Game` ruft über einen festen Zeit-Akkumulator ausschließlich `advanceOneTick()` auf;
  Render-Delta und Float-Weltkoordinaten verändern weder Fortschritt noch Trefferlogik.
- Ein View-Adapter bildet `routeProgressUnits` über `UnitRoute` auf Weltpositionen ab und
  verwaltet visuelle Units anhand der stabilen `deploymentId`.
- Spawn, Reichweitenwechsel, Schüsse, Schaden, Niederlage, Durchbruch, Basisschaden und
  Abschluss werden ausschließlich aus den geordneten Domain-Events dargestellt. Auch
  Turmdrehung und Mündungsfeuer bleiben reine Reaktionen auf diese Events.
- Ergebnisansicht, Wiederholen und `Plan anpassen` schließen den Übergang zwischen
  Planungs-, Ausführungs- und Ergebniszustand; die Bestätigungsansicht aus Meilenstein 3
  ist bis dahin der definierte Endpunkt.

## 8. Umsetzung in klaren Schritten

### Meilenstein 1: Deterministische Domäne

- ausschließlich die für `CombatSimulation` notwendigen Specs und den Simulationszustand
  mit festem Tick- und Ganzzahlmodell implementieren;
- `PlanValidator` mit konkreten Validierungsfehlern implementieren;
- den vollständigen, stabil geordneten Domain-Event-Log erzeugen;
- das Referenzszenario als Datenobjekt anlegen und die Akzeptanzfälle aus Abschnitt 4.3
  sowie die unter Abschnitt 7.4 gesondert aufgeführten Grenzfall-Regeltests grün bekommen.

Nicht Teil dieses Meilensteins sind `PlanEnumerator`, Batch-Auswertung, CSV-/JSON-Export,
Gewinnfenster und die umfangreichen Kennzahlen aus Abschnitt 6.5. Sie bleiben bewusst für
Meilenstein 2 nachgeordnet.

**Fertig, wenn:** Das Szenario ohne LibGDX vollständig in Tests läuft und ein verständliches
und bei identischer Eingabe bytegleich kanonisiertes Event-Log erzeugt. Der dokumentierte
Testaufruf reproduziert die Referenzfälle und ersetzt die unbestätigte Vorprüfung aus
Abschnitt 4.4 durch belegte Resultate.

### Meilenstein 2: Simulations-Sandbox

- `PlanEnumerator` und eine schlanke In-Memory-Batch-Auswertung implementieren;
- die Referenzfamilie und alle gültigen kanonischen Pläne vollständig bewerten;
- alle Balancing-Gates automatisiert prüfen;
- Startwerte nur anpassen, falls Akzeptanzfälle oder Gates dies erfordern.

CSV-/JSON-Export, Visualisierung und die umfangreichen Kennzahlen aus Abschnitt 6.5 sind
nicht Teil dieses Meilensteins und bleiben vertagt.

**Fertig, wenn:** Alle gültigen Pläne automatisch bewertet werden und mehrere robuste,
erklärbare Gewinnpläne existieren. Dieser Zustand ist mit den bestätigten Resultaten aus
Abschnitt 4.4 erreicht.

### Meilenstein 3: Planungs-UI

- in der Combat Demo drei Deployment-Slots mit Auswahl zwischen `BULWARK`, `RUNNER` und
  leerem Slot bereitstellen;
- Kosten, Restbudget, Leben, Geschwindigkeit sowie Startzeiten im 0,25-s-Raster aus den
  zentralen Specs anzeigen;
- den Plan mit demselben `PlanValidator` wie die Simulation sichtbar validieren und bei
  `Angriff starten` normalisiert und unveränderlich an `C_Game` übergeben;
- Reichweite und bekannte Turmwerte anzeigen und nach erfolgreicher Übergabe eine klare,
  gesperrte Bestätigungsansicht zeigen.

Die visuelle Kampfausführung bleibt ausdrücklich Meilenstein 4. Meilenstein 3 erzeugt
keine zweite Regelinstanz und startet weder die neue Simulation noch die alte automatische
Unit-Ausführung im Hintergrund.

**Fertig, wenn:** Ein Spieler den Referenzplan ohne Debug-Eingaben erstellen, seine
Gültigkeit erkennen und ihn eindeutig zur späteren Ausführung übergeben kann. Dieser Stand
ist für die Combat Demo erreicht.

### Meilenstein 4: Visuelle Ausführung und Auswertung

- Simulation mit festem Zeit-Akkumulator abspielen;
- Unit-Positionen und Domain-Events rendern;
- Ergebnisansicht, Wiederholen und Plan anpassen ergänzen.

**Fertig, wenn:** visueller und Headless-Lauf für denselben Plan denselben Event-Log-Hash
und dasselbe Ergebnis besitzen.

### Meilenstein 5: Menschlicher Playtest

Zu beobachten sind nicht nur Siegquoten, sondern:

- Erkennt der Spieler ohne Erklärung, dass der Cooldown das Zeitfenster ist?
- Kann er nach einer Niederlage benennen, welche Unit zu früh oder zu spät war?
- Verbessert der zweite oder dritte Versuch den Plan gezielt?
- Wirkt ein Sieg wie die Folge des Plans und nicht wie Zufall?

Erst nach einem positiven Ergebnis wird ein weiterer Unit-Typ oder ein zweiter Turm
eingeführt.

## 9. Erweiterungsreihenfolge nach dem ersten Slice

1. **Zweiter Turm mit anderem Rhythmus:** erzeugt überlappende Timing-Fenster, ohne eine
   neue Unit-Regel einzuführen.
2. **Dritte Unit-Rolle:** beispielsweise Schwarm gegen langsamen Flächenschaden oder
   Störer gegen Zielerfassung. Sie benötigt einen eigenen Konter und darf keine bestehende
   Rolle ersetzen.
3. **Routenwahl:** zwei bekannte Routen mit unterschiedlichen Verteidigungsprofilen.
4. **Begrenzte Aufklärung:** ein Scout deckt genau eine relevante Eigenschaft auf.
5. **Szenario-Generator oder Verteidigungs-KI:** erzeugt vor der Planung reproduzierbare
   Szenarien aus einem Seed; kein unkontrollierter Ausbau während der Ausführung.

Aktive Fähigkeiten, Echtzeit-Mikro und Meta-Progression bleiben danach eigenständige
Produktentscheidungen. Sie werden nicht automatisch aus der technischen Möglichkeit
abgeleitet.

## 10. Verworfene beziehungsweise vertagte Richtungen

### Reines Breach Puzzle ohne Timing

Nur die Unit-Zusammenstellung zu wählen wäre hervorragend simulierbar, aber nach dem Finden
der korrekten Kombination weitgehend gelöst. Deshalb verbindet der Slice die knappe
Zusammenstellung mit Startabständen.

### Vollständiges „Recon and Counter“

Verborgene Turmeigenschaften und Scouts können später Lernen erzeugen, erhöhen aber vor
dem Nachweis des Kernloops die Zahl der Systeme und erschweren die Ursachenanalyse.

### Laufend bauende Verteidigungs-KI

Sie widerspricht zunächst der Lesbarkeit und dem deterministischen Puzzle. Eine KI darf
später vor Rundenbeginn ein Szenario erzeugen oder eine klar angekündigte Regel befolgen.

## Anhang A: Referenzanalyse

| Referenz | Entscheidungsraum | Übertragbare Lehre | Nicht direkt übernehmen |
|---|---|---|---|
| Anomaly: Warzone Earth / Anomaly 2 | Route planen, Squad zusammenstellen, zeitlich begrenzte Commander-Fähigkeiten | Eine feste Verteidigung wird interessant, wenn Spieler Timing oder Route aktiv beeinflussen können. | Action-Commander und Echtzeit-Mikro wären für den ersten Slice zu groß. |
| Countless Army | Truppentyp, Reihenfolge und Route jeder Welle; unterstützende Zauber | Unit-Reihenfolge plus Routenwahl kann einen Reverse-TD-Loop tragen. | Große Meta-Progression, zwölf Unit-Typen und Skilltree kommen viel später. |
| Legion TD 2 | Verteidigen, Ökonomie und offensive Mercenary-Sends; Angriff als Antwort auf beobachtete Schwächen | Kosten müssen nicht nur Stärke abbilden, sondern Fenster und Gegenmaßnahmen erzeugen. | PvP-Economy und gleichzeitiger Aufbau beider Seiten sind nicht Teil des Solo-Slices. |
| Minion Masters | Ressource, Hand/Optionen, Timing und Synergien in kurzen Echtzeitpartien | Begrenzte Ressourcen plus unterschiedliche Rollen erzeugen Entscheidungen auch mit wenigen Unit-Typen. | Deckbau und große Kartenpools würden die Lesbarkeit des Prototyps verschlechtern. |
| Einfaches „Tower Offense“-Muster | Spieler sendet Units, Computer baut oder steuert Türme. | Die Rollenverteilung des Projekts ist verständlich und tragfähig. | Zufälliger KI-Ausbau darf das erste Testlevel nicht unlesbar machen. |

Quellen:

- [Anomaly: Warzone Earth – offizielle Serienseite](https://anomalythegame.com/)
- [Anomaly 2 auf Steam](https://store.steampowered.com/app/236730/Anomaly_2/)
- [Countless Army auf Steam](https://store.steampowered.com/app/2413640/Countless_Army/)
- [Legion TD 2 – offizielle Beschreibung](https://www.legiontd2.com/press/)
- [Legion TD 2 – Manual](https://beta.legiontd2.com/manual/)
- [Minion Masters auf Steam](https://store.steampowered.com/app/489520/Minion_Masters/)

## Anhang B: Entscheidungsprotokoll

| Frage | Entscheidung für Slice 1 | Später überprüfbar? |
|---|---|---|
| Live senden oder vorplanen? | Vorplanen, danach automatische Ausführung. | Ja, nach Playtests. |
| Zufall? | Nein. | Seed-basierte Szenarien später. |
| Verteidigung baut während des Angriffs? | Nein. | Erst nach stabilem Kernloop. |
| Projektillaufzeit Teil der Regel? | Nein, Treffer sind sofort. | Für neue Turmrolle möglich. |
| Zielpriorität? | Größter Routenfortschritt, stabiler ID-Tiebreaker. | Als sichtbare Tower-Eigenschaft erweiterbar. |
| Siegbedingung? | Basisintegrität auf 0 reduzieren. | Ja, szenariospezifisch. |
| Erste strategische Beziehung? | Bollwerk bindet Schüsse, Läufer nutzt Cooldown. | Bleibt Kern des Referenzszenarios. |
