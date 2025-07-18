# Ferienwohnungsverwaltung – DB1 Projekt (HTWG Konstanz)

Dieses Projekt wurde im Auftrag der Hochschule Konstanz Technik, Wirtschaft und Gestaltung (HTWG) im Rahmen des Kurses **Datenbanken 1** erstellt.

## Projektbeschreibung
Die Anwendung ermöglicht die Verwaltung und Buchung von Ferienwohnungen. Nutzer können sich anmelden, Ferienwohnungen suchen und filtern, Buchungen vornehmen und ihre eigenen Buchungen einsehen. Die Daten werden in einer Oracle-Datenbank gespeichert und über ein Java-Swing-Frontend verwaltet.

## Features (Umfang Prototyp)
- Anmeldung mit E-Mail und Passwort
- Suche und Filterung von Ferienwohnungen (nach Land, Zeitraum, Ausstattung)
- Anzeige von Bildern, Ausstattung und Attraktionen
- Buchung einer Ferienwohnung für einen bestimmten Zeitraum
- Automatische Berechnung und Speicherung des Rechnungsbetrags
- Übersicht "Meine Buchungen" für eingeloggte Nutzer

## Was fehlt für Produktionstauglichkeit?
- **Sichere Passwortspeicherung:** Passwörter werden aktuell im Klartext gespeichert. In einer echten Anwendung müssen Passwörter gehasht und gesalzen werden (z.B. bcrypt).
- **Fehler- und Ausnahmebehandlung:** Fehler werden meist nur in der Konsole ausgegeben. Für den Produktivbetrieb ist ein robustes Logging und ein durchdachtes Fehlerhandling nötig.
- **Validierung und Security:** Eingaben werden nur rudimentär geprüft. Es fehlen umfassende Validierungen (z.B. IBAN, E-Mail, Datumslogik) und Schutz vor Missbrauch.
- **Rollen- und Rechteverwaltung:** Es gibt keine Unterscheidung zwischen normalen Nutzern und Admins. Für den Produktivbetrieb wäre ein Berechtigungskonzept nötig.
- **Transaktionen:** Komplexere Abläufe (z.B. Buchung + Anzahlung) sollten in Transaktionen gekapselt werden.
- **UI/UX:** Das UI ist funktional, aber nicht für Endnutzer optimiert. Es fehlen z.B. Responsive Design, Barrierefreiheit, Mehrsprachigkeit.
- **Sicherheit:** Es gibt keine Verschlüsselung der Kommunikation (z.B. SSL/TLS), keine Sessionverwaltung und keine Schutzmechanismen gegen Brute-Force-Angriffe.
- **Skalierbarkeit:** Die Anwendung ist als Prototyp für Einzelbenutzer gedacht und nicht für parallele Zugriffe oder große Datenmengen optimiert.

## Fazit
Dieses Projekt ist ein **klarer, einfacher Prototyp** und bietet einen Vorgeschmack darauf, wie eine vollständige Buchungsplattform für Ferienwohnungen aussehen könnte. Es dient als Lern- und Übungsprojekt für relationale Datenbanken, SQL und Java-Programmierung.

## Installation
Unter dbsys.sql ist die dazugehörige Datenbank zu finden. Man braucht diese Tabellenstruktur damit das Programm auch richtig funktioniert und die Abfragen über die richtige Tabellen laufen.
Unter config.sql müsst ihr die korrekte DB-URL und Anmeldedaten angeben.
In AuthManager.java und DataBaseManager.java werden dann auf den Login zugegriffen !!ACHTUNG!! Der Login erfolgt einmal über den AuthManager für den Login Bereich und über den DatabaseManager für das Menü und die eigentlichen Funktionen in der App.
Im Best-Practice wird das getrennt gehalten damit man nicht schon in der Loginabfrage zu viele Rechte auf anderen Tabellen hat sondern nur für die nötige Loginabfrage.
Einfachheitshalber haben wir das nun so gestaltet, aber vollständigkeitshalber ist es integriert und lässt sich einfach erweitern.


---
**HTWG Konstanz – Kurs Datenbanken 1 – Sommersemester 2025**
