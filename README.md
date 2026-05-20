
# 🧠 Vista ADHD Edukator

A desktop educational application built in Java (Swing) designed to support children with ADHD and dyslexia through short, engaging cognitive mini-games.

---

## Overview

Vista ADHD Edukator is a standalone Java desktop app with three mini-games targeting attention, reaction speed, and short-term memory. It includes a dyslexia-friendly display mode that enlarges fonts and adjusts background contrast to make the interface easier to read.

---

## Features

- **3 educational mini-games**, each lasting 5 rounds with a final score summary
- **Dyslexia mode** — toggleable from the main menu; increases font size and switches to a warm yellow background for improved readability
- **Final result screen** — shows score and an encouraging message after each game session
- Clean, colourful GUI built entirely with Java Swing

---

## Mini-Games

### 1. 🔍 Find the Letter (Pronađi slovo)
A letter recognition game. The player is shown a target letter and must click the correct one from a grid of four options. Tests focus and letter discrimination — skills often challenging for children with dyslexia or ADHD.

### 2. ⭐ Quick Click (Brzi klik)
A reaction speed game. A star icon appears at a random position on the screen each round, and the player must click it as quickly as possible. Trains sustained attention and motor response.

### 3. 🧠 Remember the Colours (Zapamti boje)
A sequence memory game. The app flashes a sequence of coloured buttons, and the player must reproduce the exact order by clicking. Sequence length increases across rounds. Targets working memory and concentration.

---

## Tech Stack

- **Language:** Java
- **UI Framework:** Java Swing
- **Build:** Single-file compilation, no external dependencies

---

## Running the App

### Prerequisites
- Java 8 or later

### Compile and run
```bash
javac VistadhdApp.java
java VistadhdApp
```

---

## Structure

The entire application is contained in a single file:

```
VistadhdApp.java
├── createMenuScreen()       # Main menu with game selection and dyslexia toggle
├── createGame1()            # Letter recognition game
├── createGame2()            # Reaction click game
├── createGame3()            # Colour sequence memory game
├── nextMemoryRound()        # Handles memory game round progression
├── flashButton()            # Animates colour button flash for sequence display
├── handleColorClick()       # Validates player input against the colour sequence
├── showFinalResult()        # Displays score and returns to menu
└── getAppFont()             # Returns appropriately sized font based on dyslexia mode
```

---

## Design Notes

- All games are self-contained within the same `JFrame` using a `CardLayout` for navigation
- Dyslexia mode rebuilds the entire UI on toggle to apply font and background changes globally
- Round state (`currentRound`, `correctAnswers`) is reset each time a game panel is shown via a `ComponentListener`
- The memory game increases sequence length gradually (starts at 2, grows every 2 rounds) to scale difficulty across the 5 rounds
