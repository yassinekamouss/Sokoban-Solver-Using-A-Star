package com.fstt.devoir;

import java.util.*;

/**
 * Classe principale du Solveur.
 * * Rôle :
 * 1. Contenir les constantes du jeu (symboles, directions).
 * 2. Implémenter la boucle principale de l'algorithme A* (méthode 'solve').
 * 3. Contenir les méthodes utilitaires (affichage, vérifications).
 * * (Basé sur le fichier SokobanSolver.java de votre ami, 'main' en moins)
 */
public class SokobanSolver {

    // --- SYMBOLES DU JEU ---
    public static final char WALL = '■';
    public static final char TARGET = 'T';
    public static final char PLAYER = '@';
    public static final char FLOOR = '□';
    public static final char PLAYER_ON_TARGET = '+';

    // Les caisses dans un emplacement autre que la cible (nommées a, b, c, d)
    public static final char[] BOX_NAMES = {'a', 'b', 'c', 'd'};
    // Les caisses DANS leurs cibles (en majuscules)
    public static final char[] BOX_ON_TARGET_NAMES = {'A', 'B', 'C', 'D'};

    // Map pour convertir 'a' -> 'A' et 'A' -> 'a'
    public static final Map<Character, Character> BOX_TO_TARGET_MAP = Map.of(
            'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D',
            'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd'
    );
    // Un Set pour vérifier rapidement si un char est une caisse (maj ou min)
    public static final Set<Character> ALL_BOX_SYMBOLS = new HashSet<>(Arrays.asList('a', 'b', 'c', 'd', 'A', 'B', 'C', 'D'));

    // --- DIRECTIONS ---
    // Mouvements: {Haut, Bas, Gauche, Droite}
    public static final int[][] DIRS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
    // Noms des directions (pour l'affichage de l'action)
    public static final String[] DIR_NAMES = {
            "UP", "DOWN", "LEFT", "RIGHT"
    };

    /**
     * Méthode de résolution principale (la boucle A*).
     * Trouve la séquence de Poussées la plus courte.
     * * @param level La grille de jeu (tableau de String)
     * @return L'état final (gagnant), ou 'null' si aucune solution n'est trouvée.
     */
    public static Etat solve(String[] level) {

        // 1. Initialisation
        // Crée l'état initial en analysant la grille
        Etat etatInitial = new Etat(level);
        if (etatInitial.isGoal()) {
            System.out.println("Niveau déjà résolu!");
            return etatInitial;
        }

        // openList : 'PriorityQueue' trie les états par 'f_cost' (f = g + h)
        PriorityQueue<Etat> openList = new PriorityQueue<>(Comparator.comparingInt(s -> s.f_cost));

        // closedList : 'HashSet' stocke les clés uniques des états déjà explorés
        Set<String> closedList = new HashSet<>();

        openList.add(etatInitial);
        int exploredNodes = 0; // Métrique: Nombre de nœuds explorés

        // 2. Boucle A*
        // Tant qu'il y a des états à explorer...
        while (!openList.isEmpty()) {
            // Prendre le meilleur état (celui avec le plus petit f_cost)
            Etat current = openList.poll();
            exploredNodes++;

            // Si cet état est déjà dans la closedList, on l'ignore
            if (closedList.contains(current.getUniqueKey())) {
                continue;
            }

            // 3. Vérification de la Victoire
            if (current.isGoal()) {
                // Métrique: Nombre de nœuds explorés
                System.out.println("Nombre de nœuds explorés par A*: " + exploredNodes);
                return current; // Solution trouvée!
            }

            // 4. Exploration
            // Ajouter l'état actuel à la closedList
            closedList.add(current.getUniqueKey());

            // 5. Génération des successeurs
            // 'generateSuccessors' crée tous les états atteignables (MOVE ou PUSH)
            for (Etat nextState : current.generateSuccessors()) {
                if (!closedList.contains(nextState.getUniqueKey())) {
                    openList.add(nextState);
                }
            }
        }

        // 6. Échec
        System.out.println("Nombre de nœuds explorés par A*: " + exploredNodes);
        return null; // Solution non trouvée
    }

    /**
     * Méthode utilitaire pour afficher la grille d'un état.
     */
    public static void displayBoard(char[][] board) {
        for (char[] row : board) {
            for (char cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    /**
     * Vérifie si une coordonnée (r, c) est dans les limites de la grille.
     */
    public static boolean isValid(int r, int c, int rows, int cols) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /**
     * Vérifie si un caractère est un symbole de caisse (a,b,c,d,A,B,C,D).
     */
    public static boolean isBoxSymbol(char c) {
        return ALL_BOX_SYMBOLS.contains(c);
    }

    /**
     * Retourne le nom de base (minuscule) d'une caisse.
     * 'A' -> 'a', 'b' -> 'b'.
     */
    public static char getBoxName(char c) {
        if (c >= 'a' && c <= 'd') return c;
        if (c >= 'A' && c <= 'D') return SokobanSolver.BOX_TO_TARGET_MAP.get(c);
        return ' '; // Ne devrait pas arriver
    }
}