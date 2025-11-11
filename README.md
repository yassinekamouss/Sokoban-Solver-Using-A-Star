Absolument ! Voici une proposition de fichier `README.md` en Markdown, basée sur les informations que vous avez fournies. Ce code est prêt à être copié-collé dans votre fichier.

---

# Solveur de Sokoban en Java avec l'Algorithme A*

## Description

Ce projet est une implémentation en Java d'un "solveur" pour le jeu de puzzle classique **Sokoban**. L'objectif n'est pas de créer un jeu jouable, mais de développer un agent d'intelligence artificielle capable de trouver la **solution optimale** (le moins de poussées) pour une grille donnée.

Ce programme a été développé en adaptant l'algorithme de recherche A* (A-star) à la logique spécifique du Sokoban.

## Fonctionnalités

-   **Algorithme A\***: Utilise une recherche heuristique pour trouver le chemin le plus court vers la solution.
-   **Optimisation des Poussées**: La solution est optimisée pour le **nombre minimum de poussées** de caisses, et non pour le nombre total de déplacements du joueur.
-   **Détection d'Impasse**: Capable de déterminer si une grille n'a pas de solution.
-   **Visualisation de la Solution**: Affiche la séquence complète des poussées, de l'état initial à l'état final, pour une meilleure compréhension.

## Démarche Algorithmique

L'adaptation de l'algorithme A* ($f(n) = g(n) + h(n)$) a nécessité plusieurs choix de conception clés pour être efficace dans le contexte de Sokoban.

### 1. Définition d'un État (`Etat.java`)

Un "état" (ou nœud de recherche) n'est pas seulement défini par la position du joueur. Un état unique est défini par la combinaison de :
-   La position du joueur (`playerR`, `playerC`).
-   L'ensemble (`Set`) des positions de **toutes les caisses** (`BoxPosition`).

Pour que la `closedList` (un `HashSet`) puisse fonctionner correctement et éviter les doublons, une clé `String` unique est générée pour chaque état en triant les caisses par nom et en concaténant leurs positions.

### 2. Fonction de Coût `g(n)` : L'optimisation Clé

C'est le point le plus important du projet. Pour optimiser le nombre de poussées (et non le nombre de pas), la fonction de coût `g(n)` est définie comme suit :

-   Un mouvement simple du joueur (**MOVE**) a un coût de **0**.
-   Une poussée de caisse (**PUSH**) a un coût de **1**.

Cela permet à l'algorithme A\* d'explorer "gratuitement" toutes les zones accessibles au joueur avant de "payer" le coût d'une poussée, ce qui garantit une optimisation centrée sur le déplacement des caisses.

### 3. Fonction Heuristique `h(n)`

L'heuristique `h(n)` (le coût estimé restant) est calculée comme la **somme des distances de Manhattan** de chaque caisse à sa cible la plus proche.

```
h(n) = Σ |caisse.r - cible_proche.r| + |caisse.c - cible_proche.c|
```

Cette heuristique est **admissible** (elle ne surestime jamais le coût réel), car une caisse doit au minimum parcourir cette distance pour atteindre sa cible. Cela garantit que A\* trouve la solution optimale.

## Structure du Code

Le projet est divisé en 5 classes Java principales :

-   `Main.java`: Point d'entrée de l'application. Contient les grilles de test et appelle le solveur.
-   `SokobanSolver.java`: Classe utilitaire contenant la boucle principale de l'algorithme A\* (`solve()`), les constantes du jeu et les méthodes d'affichage.
-   `Etat.java`: La classe principale du modèle. Représente un nœud A\* et contient toute la logique du jeu (génération des successeurs, calcul de l'heuristique, vérification de la victoire).
-   `Position.java`: Classe simple pour stocker les coordonnées des cibles (Targets).
-   `BoxPosition.java`: Classe spécialisée pour les caisses, stockant les coordonnées (`r`, `c`) et le nom de la caisse (`char name`).

## Comment l'exécuter

Le projet est une application console Java standard.

1.  **Prérequis**
    -   Assurez-vous d'avoir un JDK (Java Development Kit) installé sur votre système.

2.  **Cloner le dépôt**
    ```bash
    git clone <URL_DU_DEPOT>
    cd <NOM_DU_DOSSIER>
    ```

3.  **Compilation**
    Naviguez jusqu'au dossier racine (`src`) et compilez tous les fichiers `.java` :
    ```bash
    javac com/fstt/devoir/*.java
    ```

4.  **Exécution**
    Exécutez la classe `Main` depuis le dossier `src` :
    ```bash
    java com.fstt.devoir.Main
    ```

> **Note** : Les chemins peuvent varier selon votre structure (`src`/`target`). Le plus simple est d'ouvrir le projet dans un IDE comme **IntelliJ IDEA** ou **Eclipse** et d'exécuter directement la méthode `main` de la classe `Main.java`.

## Exemple de Résultat (Grille 1)

L'exécution du programme sur la première grille produit la sortie suivante, montrant la solution optimale de 15 poussées.

```
--- Résolution Sokoban: 1er test ---
Nombre de nœuds explorés par A*: 827764

Test 1 - Solution trouvée!
Temps de résolution: 2871 ms
Longueur de la solution optimale: 15 poussées
Chemin des poussées avec visualisation:

--- ÉTAT INITIAL ---
■■■■■■■■■■
■□□□□□□□□■
■□■■□■■□□■
■□a□T□b□□■
■□■□@□■□□■
■□c□T□d□□■
■□■■□■■□□■
■□□T□□T□□■
■□□□□□□□□■
■■■■■■■■■■

1. PUSH 'c' RIGHT
   ... (étapes 1 à 14) ...

15. PUSH 'd' LEFT
    ■■■■■■■■■■
    ■□□□□□□□□■
    ■□■■□■■□□■
    ■□□□A□□□□■
    ■□■□□□■□□■
    ■□□□D@□□□■
    ■□■■□■■□□■
    ■□□C□□B□□■
    ■□□□□□□□□■
    ■■■■■■■■■■

--- FIN DE LA SOLUTION ---
```