# Inventory Management System

Sistema desktop per la gestione del magazzino sviluppato in **Java 17** con **JavaFX**, database **SQLite** ed esportazione documenti Excel tramite **Apache POI**.

Il progetto permette di:

* Gestire articoli di magazzino
* Inserire, modificare ed eliminare prodotti
* Monitorare quantità disponibili e impegnate
* Generare preventivi / fatture
* Esportare documenti in formato Excel (`.xlsx`)
* Utilizzare un database locale SQLite senza configurazioni complesse

---

# Tecnologie Utilizzate

* **Java 17**
* **JavaFX 22**
* **SQLite JDBC**
* **Apache POI**
* **Maven**

---

# Struttura del Progetto

```bash
Applicazione/
│
├── src/main/java/myapp/Applicazione/
│   ├── App.java
│   ├── Launcher.java
│   ├── GUI.java
│   ├── Articolo.java
│   ├── GestioneArticoli.java
│   ├── DBsetup.java
│   ├── Fattura.java
│   ├── RigaFattura.java
│   └── GeneratoreExcel.java
│
├── magazzino.db
├── pom.xml
└── target/
```

---

# Funzionalità

## Gestione Articoli

L'applicazione permette di:

* Inserire nuovi articoli
* Aggiornare articoli esistenti
* Eliminare articoli
* Visualizzare:

  * codice articolo
  * nome
  * descrizione
  * prezzo
  * quantità disponibile
  * quantità impegnata

---

## Gestione Magazzino

Ogni articolo contiene:

* Quantità totale
* Quantità impegnata
* Quantità disponibile calcolata automaticamente

Il sistema previene:

* Quantità negative
* Impegni superiori alla disponibilità
* Errori di gestione stock

---

## Preventivi e Fatture

Il modulo preventivi consente di:

* Selezionare articoli dal magazzino
* Definire quantità
* Generare righe fattura
* Calcolare il totale automaticamente
* Esportare documenti Excel

---

## Esportazione Excel

La classe `GeneratoreExcel` utilizza Apache POI per creare file `.xlsx` contenenti:

* Numero fattura
* Data
* Elenco articoli
* Prezzi unitari
* Quantità
* Totale finale

---

# Database

Il database utilizzato è SQLite.


# Installazione

## Requisiti

* Java 17+
* Maven
* JavaFX SDK
---

# Dipendenze Maven

Principali dipendenze utilizzate:

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.46.1.0</version>
</dependency>

<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>

<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>22.0.2</version>
</dependency>
```

---

# Architettura

## Articolo.java

Modello principale del sistema.

Gestisce:

* dati articolo
* validazioni
* quantità disponibili
* quantità impegnate

---

## GestioneArticoli.java

Layer di accesso al database.

Contiene operazioni CRUD:

* lettura articoli
* inserimento
* aggiornamento
* eliminazione

---

## GUI.java

Interfaccia grafica JavaFX.

Include:

* tabella articoli
* form gestione
* dialog preventivi
* esportazione Excel

---

## GeneratoreExcel.java

Responsabile della creazione dei file Excel.


# Possibili Miglioramenti Futuri


* PDF export
* Storico fatture
* Dashboard statistiche
* Tema dark mode
* Ricerca avanzata articoli
* Barcode scanner


---

# Licenza

Questo progetto è distribuito a scopo educativo e personale.

Puoi modificarlo e migliorarlo liberamente.

---

# Autore

Progetto sviluppato da Kris0x11.
