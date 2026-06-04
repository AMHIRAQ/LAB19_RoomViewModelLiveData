

https://github.com/user-attachments/assets/d17785f9-a433-4152-85be-71972be10ed9



# LAB 19 : Room, MVVM, Repository, ViewModel, LiveData et RecyclerView

> Application Android développée dans le cadre du **LAB 19** du cours *Programmation Mobile : Android avec Java*.
> Elle illustre l'architecture **MVVM** combinée à **Room**, **LiveData**, **ViewModel** et **RecyclerView**.

---

## Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Architecture](#architecture)
- [Structure des packages](#structure-des-packages)
- [Fonctionnalités](#fonctionnalités)
- [Technologies utilisées](#technologies-utilisées)
- [Dépendances](#dépendances)
- [Installation et lancement](#installation-et-lancement)
- [Guide des fichiers](#guide-des-fichiers)
- [Flux de données](#flux-de-données)
- [Tests à réaliser](#tests-à-réaliser)
- [Compétences acquises](#compétences-acquises)
- [Auteur](#auteur)

---

## Aperçu du projet

RoomMVVMDemo est une application de gestion de notes locales. L'utilisateur peut ajouter, consulter, supprimer individuellement ou supprimer toutes les notes. Les données sont persistées localement grâce à **Room (SQLite)** et l'interface est automatiquement mise à jour via **LiveData** sans rechargement manuel.

---

## Architecture

Le projet suit le patron architectural **MVVM (Model — View — ViewModel)** recommandé par Google pour les applications Android modernes.

```
┌─────────────────────────────────────────┐
│           Interface Utilisateur          │
│   MainActivity  ◄──  RecyclerView        │
└──────────────┬──────────────────────────┘
               │  observe / appelle
┌──────────────▼──────────────────────────┐
│            NoteViewModel                 │
│     Logique de présentation              │
│     Survie aux changements config        │
└──────────────┬──────────────────────────┘
               │  délègue
┌──────────────▼──────────────────────────┐
│           NoteRepository                 │
│     Couche d'accès centralisée           │
│     Gère le thread secondaire            │
└──────────────┬──────────────────────────┘
               │  appelle
┌──────────────▼──────────────────────────┐
│             NoteDao                      │
│     Interface SQL annotée                │
│     Retourne LiveData<List<Note>>        │
└──────────────┬──────────────────────────┘
               │  lit/écrit
┌──────────────▼──────────────────────────┐
│          Room / SQLite                   │
│     Base de données locale               │
└─────────────────────────────────────────┘
```

**Sens descendant (actions utilisateur) :**
MainActivity → NoteViewModel → NoteRepository → NoteDao → SQLite

**Sens remontant (mises à jour automatiques) :**
SQLite → Room → LiveData → ViewModel → Activity → RecyclerView

---

## Structure des packages

```
com.example.roommvvmdemo
│
├── data
│   ├── local
│   │   ├── Note.java           ← Entité Room (table SQLite)
│   │   ├── NoteDao.java        ← Interface d'accès aux données
│   │   └── NoteDatabase.java   ← Singleton de la base Room
│   └── NoteRepository.java     ← Couche intermédiaire données
│
├── ui
│   ├── MainActivity.java       ← Vue principale (Activity)
│   └── NoteAdapter.java        ← Adapter du RecyclerView
│
└── viewmodel
    └── NoteViewModel.java      ← Logique de présentation
```

---

## Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| ➕ Ajouter une note | Saisir un titre + une description, puis cliquer sur le bouton |
| 🗑️ Supprimer une note | Clic long sur la note souhaitée |
| 🧹 Tout supprimer | Bouton "SUPPRIMER TOUTES LES NOTES" |
| 💾 Persistance | Les notes survivent à la fermeture de l'application |
| 🔄 Rotation d'écran | Les données restent présentes sans rechargement |
| 📋 Liste dynamique | Mise à jour automatique via LiveData + RecyclerView |

---

## Technologies utilisées

- **Java** — Langage de développement
- **Android SDK** — Min API 24 (Android 7.0)
- **Room** — ORM officiel Android au-dessus de SQLite
- **LiveData** — Observation des données respectant le cycle de vie
- **ViewModel / AndroidViewModel** — Survie aux changements de configuration
- **RecyclerView** — Affichage performant avec recyclage des vues
- **CardView** — Rendu visuel moderne de chaque note
- **ExecutorService** — Exécution des opérations Room hors du thread principal

---

## Dépendances

Dans `build.gradle (Module: app)` :

```groovy
def room_version = "2.6.1"
def lifecycle_version = "2.8.7"

// Room
implementation "androidx.room:room-runtime:$room_version"
annotationProcessor "androidx.room:room-compiler:$room_version"
implementation "androidx.room:room-ktx:$room_version"

// Lifecycle (ViewModel + LiveData)
implementation "androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-livedata:$lifecycle_version"

// UI
implementation "androidx.recyclerview:recyclerview:1.3.2"
implementation "androidx.cardview:cardview:1.0.0"
```

---

## Installation et lancement

1. Cloner ou télécharger le projet
2. Ouvrir Android Studio → **Open an existing project**
3. Sélectionner le dossier `RoomMVVMDemo`
4. Attendre la synchronisation Gradle
5. Connecter un appareil physique ou lancer un émulateur (API ≥ 24)
6. Cliquer sur **▶ Run** (`Shift + F10`)

---

## Guide des fichiers

### `Note.java` — Entité
Représente une ligne dans la table SQLite `notes_table`.
Annotée `@Entity`, avec un identifiant auto-généré (`@PrimaryKey(autoGenerate = true)`).

### `NoteDao.java` — DAO
Interface annotée `@Dao` contenant les opérations SQL :
- `insert(Note)` — insertion d'une note
- `delete(Note)` — suppression d'une note précise
- `deleteAllNotes()` — suppression complète
- `getAllNotes()` — retourne un `LiveData<List<Note>>` trié par id décroissant

### `NoteDatabase.java` — Base de données
Singleton thread-safe (`volatile` + `synchronized`) représentant la base Room.
Utilise `fallbackToDestructiveMigration()` pour les environnements de développement.

### `NoteRepository.java` — Repository
Couche intermédiaire qui :
- récupère l'instance de la base
- expose les méthodes d'insertion, suppression et lecture
- utilise un `ExecutorService` pour exécuter les opérations hors du thread principal

### `NoteViewModel.java` — ViewModel
Étend `AndroidViewModel` pour accéder à l'objet `Application`.
Expose les données observables à l'Activity et délègue toutes les opérations au Repository.
Survit aux rotations d'écran et aux changements de configuration.

### `NoteAdapter.java` — Adapter RecyclerView
Gère l'affichage de la liste des notes.
Supporte un `OnItemClickListener` (clic simple → affiche le titre) et un `OnItemLongClickListener` (clic long → suppression).

### `MainActivity.java` — Vue principale
Initialise le RecyclerView, l'Adapter et le ViewModel.
Observe `getAllNotes()` via LiveData pour mettre à jour la liste automatiquement.

---

## Flux de données

### Ajout d'une note

```
Utilisateur saisit titre + description
         ↓
  btnAdd.setOnClickListener
         ↓
  saveNote() → new Note(title, description)
         ↓
  noteViewModel.insert(note)
         ↓
  repository.insert(note)
         ↓
  ExecutorService → thread secondaire
         ↓
  noteDao.insert(note) → SQLite
         ↓
  Room réémet LiveData automatiquement
         ↓
  Observer dans MainActivity notifié
         ↓
  adapter.setNotes(notes) → RecyclerView mis à jour
```

### Suppression individuelle

```
Utilisateur effectue un clic long
         ↓
  OnItemLongClickListener déclenché
         ↓
  noteViewModel.delete(note)
         ↓
  repository.delete(note) → thread secondaire
         ↓
  Room → SQLite → LiveData réémet
         ↓
  RecyclerView mis à jour automatiquement
```

---

## Tests à réaliser

| # | Test | Résultat attendu |
|---|---|---|
| 1 | Ajouter 3 notes | Les 3 notes apparaissent immédiatement dans la liste |
| 2 | Clic long sur une note | La note disparaît de la liste |
| 3 | Fermer et rouvrir l'app | Les notes sont toujours présentes (persistance Room) |
| 4 | Rotation d'écran | La liste reste cohérente, aucune perte de données |
| 5 | Supprimer toutes les notes | La liste devient vide |

---

## Compétences acquises

- Comprendre et implémenter l'architecture **MVVM** sur Android
- Utiliser **Room** comme abstraction propre au-dessus de SQLite
- Observer des données avec **LiveData** en respectant le cycle de vie
- Protéger le thread principal avec **ExecutorService**
- Afficher une liste dynamique performante avec **RecyclerView**
- Comprendre pourquoi le **ViewModel** survit aux rotations d'écran
- Séparer les responsabilités entre les couches : UI, ViewModel, Repository, DAO, Base

---

## Auteur

Projet réalisé dans le cadre du cours **Programmation Mobile : Android avec Java** — LAB 19.
