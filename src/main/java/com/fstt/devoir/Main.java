package com.fstt.devoir;

import java.util.LinkedList;
import java.util.List;

/**
 * Fichier principal (Point d'Entrée).
 * * Rôle :
 * 1. Définir les grilles de jeu.
 * 2. Lancer le solveur pour chaque grille.
 * 3. Afficher les résultats et les métriques finaux.
 */
public class Main {

    /**
     * Point d'entrée du programme.
     */
    public static void main(String[] args) {
        // les tests donnée dans le devoire
        System.out.println("--- Résolution Sokoban: 1er test ---");
        // NOTE: Les caisses '$' ont été renommées 'a', 'b', 'c', 'd'
        // pour correspondre à la logique du solveur qui suit les caisses individuellement.
        String[] test1 = {
                "■■■■■■■■■■",
                "■□□□□□□□□■",
                "■□■■□■■□□■",
                "■□a□T□b□□■",
                "■□■□@□■□□■",
                "■□c□T□d□□■",
                "■□■■□■■□□■",
                "■□□T□□T□□■",
                "■□□□□□□□□■",
                "■■■■■■■■■■"
        };
        // Appel de la méthode statique 'solve' de la classe SokobanSolver
        simuler(test1, "Test 1");

        System.out.println("\n--- Résolution Sokoban 2ème test ---");
        String[] test2 = {
                "■■■■■■■■■■",
                "■T□■□□■□T■",
                "■□■a□□b■□■",
                "■□■□□□□■□■",
                "■□□□@□□□□■",
                "■□■□□□□■□■",
                "■□■c□□d■□■",
                "■T□■□□■□T■",
                "■□□□□□□□□■",
                "■■■■■■■■■■"
        };
        // Appel de la méthode statique 'solve' de la classe SokobanSolver
        simuler(test2, "Test 2");
    }

    /**
     * Méthode utilitaire pour lancer une simulation complète
     * et afficher toutes les métriques d'évaluation requises.
     * (Inspiré de SokobanSolver.java et Main.java combinés)
     */
    private static void simuler(String[] grille, String nomTest) {

        // 1. Mesurer le temps de résolution (Métrique)
        long startTime = System.currentTimeMillis();

        // 2. Lancer la résolution
        // 'solve' retourne l'état final (le but)
        Etat etatFinal = SokobanSolver.solve(grille);

        long endTime = System.currentTimeMillis();

        // 3. Gérer et afficher les résultats
        if (etatFinal != null) {
            System.out.println("\n" + nomTest + " - Solution trouvée!");
            // Métrique: Temps de résolution
            System.out.println("Temps de résolution: " + (endTime - startTime) + " ms");
            // Métrique: Nombre de nœuds explorés (géré par solve())

            // 4. Reconstruire et afficher le chemin
            reconstruireChemin(etatFinal);

        } else {
            System.out.println("\n" + nomTest + " - Solution non trouvée!");
        }
    }

    /**
     * Remonte la chaîne des parents et affiche la solution.
     * (Adapté de la méthode printSolution de votre ami)
     */
    private static void reconstruireChemin(Etat etatFinal) {
        LinkedList<Etat> solutionEtats = new LinkedList<>();
        Etat courant = etatFinal;

        // Remonter la chaîne des parents
        while (courant != null) {
            solutionEtats.addFirst(courant);
            courant = courant.parent;
        }

        // Métrique: Longueur de la solution (coût g = nombre de poussées)
        System.out.println("Longueur de la solution optimale: " + etatFinal.g_cost + " poussées");
        System.out.println("Chemin des poussées avec visualisation:");

        int pushCount = 0;
        for (Etat etat : solutionEtats) {
            // On affiche l'état initial
            if (etat.parent == null) {
                System.out.println("\n--- ÉTAT INITIAL ---");
                SokobanSolver.displayBoard(etat.board);
            }
            // On affiche uniquement les états qui résultent d'une POUSSÉE
            else if (etat.action != null && etat.action.startsWith("PUSH")) {
                pushCount++;
                System.out.println("\n" + pushCount + ". " + etat.action);
                SokobanSolver.displayBoard(etat.board);
            }
        }
        System.out.println("\n--- FIN DE LA SOLUTION ---");
    }
}