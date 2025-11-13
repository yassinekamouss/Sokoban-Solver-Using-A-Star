package com.fstt.devoir;

import java.util.*;

/**
 * Représente un "Nœud" A* (un état unique du jeu).
 * * Un état est défini par :
 * 1. La position du Joueur.
 * 2. L'ensemble des positions de TOUTES les caisses.
 */
class Etat implements Comparable<Etat> {

    // --- Variables Statiques (partagées par tous les états) ---
    // Grille statique contenant Murs (■) et Cibles (T). Ne change jamais.
    private static char[][] STATIC_BOARD;
    // Ensemble des positions des Cibles. Ne change jamais.
    private static Set<Position> TARGETS = new HashSet<>();
    private static int ROWS;
    private static int COLS;

    // --- Variables d'Instance (uniques à cet état) ---
    public char[][] board; // Grille visuelle de cet état (avec joueur et caisses)
    public int playerR, playerC; // Position du joueur
    public Set<BoxPosition> boxCoords; // Ensemble des positions ET NOMS des caisses

    // --- Coûts A* ---
    public int g_cost; // Coût (Nombre de POUSSÉES depuis le début)
    public int h_cost; // Heuristique (Distance estimée restante)
    public int f_cost; // Coût total (f = g + h)

    // --- Traçabilité ---
    public Etat parent; // L'état précédent (pour reconstruire le chemin)
    public String action; // L'action qui a mené à CET état (ex: "PUSH 'a' UP")

    /**
     * Constructeur Initial (pour le premier état).
     * Analyse la grille, sépare les éléments statiques (murs, cibles)
     * des éléments mobiles (joueur, caisses).
     */
    public Etat(String[] level) {
        this.ROWS = level.length;
        this.COLS = level[0].length();
        this.board = new char[ROWS][COLS];
        this.boxCoords = new HashSet<>();

        // Initialise les champs statiques (STATIC_BOARD, TARGETS)
        // si c'est la première fois qu'on crée un état.
        if (STATIC_BOARD == null) {
            STATIC_BOARD = new char[ROWS][COLS];

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    char cell = level[r].charAt(c);

                    if (cell == SokobanSolver.WALL) {
                        STATIC_BOARD[r][c] = SokobanSolver.WALL;
                    } else if (cell == SokobanSolver.TARGET || cell == SokobanSolver.PLAYER_ON_TARGET || (cell >= 'A' && cell <= 'D')) {
                        // Si c'est une cible, ou un objet SUR une cible
                        STATIC_BOARD[r][c] = SokobanSolver.TARGET;
                        TARGETS.add(new Position(r, c));
                    } else {
                        // C'est un sol vide (□, @, ou $)
                        STATIC_BOARD[r][c] = SokobanSolver.FLOOR;
                    }
                }
            }
        }

        // Analyse les éléments MOBILES (Joueur, Caisses)
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                char cell = level[r].charAt(c);

                if (cell == SokobanSolver.PLAYER || cell == SokobanSolver.PLAYER_ON_TARGET) {
                    this.playerR = r;
                    this.playerC = c;
                    this.board[r][c] = STATIC_BOARD[r][c] == SokobanSolver.TARGET ? SokobanSolver.PLAYER_ON_TARGET : SokobanSolver.PLAYER;
                } else if (SokobanSolver.isBoxSymbol(cell)) {
                    // Ajoute la caisse à l'ensemble des caisses
                    this.boxCoords.add(new BoxPosition(r, c, SokobanSolver.getBoxName(cell)));
                    // Met le bon symbole sur la grille (ex: 'A' si sur cible)
                    this.board[r][c] = STATIC_BOARD[r][c] == SokobanSolver.TARGET ? SokobanSolver.BOX_TO_TARGET_MAP.get(SokobanSolver.getBoxName(cell)) : SokobanSolver.getBoxName(cell);
                } else {
                    // C'est un mur ou un sol
                    this.board[r][c] = STATIC_BOARD[r][c];
                }
            }
        }

        // Initialise les coûts A* pour l'état de départ
        this.g_cost = 0; // 0 poussée au début
        this.h_cost = calculateHeuristic();
        this.f_cost = this.g_cost + this.h_cost;
        this.parent = null;
        this.action = null;
    }

    /**
     * Constructeur de copie (pour créer un état voisin).
     * Crée une copie profonde de l'état 'other'.
     */
    public Etat(Etat other) {
        this.parent = other; // Le parent est l'état 'other'
        this.g_cost = other.g_cost; // g_cost sera incrémenté si PUSH

        // Copie simple
        this.playerR = other.playerR;
        this.playerC = other.playerC;

        // Copie profonde de la grille (board)
        this.board = new char[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, COLS);
        }

        // Copie profonde de l'ensemble des caisses (boxCoords)
        this.boxCoords = new HashSet<>();
        for (BoxPosition box : other.boxCoords) {
            // BoxPosition est immuable, on peut le copier directement
            this.boxCoords.add(box);
        }
    }

    /**
     * Génère une clé unique (String) pour cet état.
     * Ex: "P(5,4)_B(a,3,2)_B(b,3,6)..."
     * Permet au HashSet (closedList) de fonctionner.
     */
    public String getUniqueKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("P").append(playerR).append(",").append(playerC);

        // Trier les caisses par nom est crucial pour que la clé soit
        // la même quel que soit l'ordre d'insertion.
        List<BoxPosition> sortedBoxes = new ArrayList<>(boxCoords);
        Collections.sort(sortedBoxes); // Utilise 'compareTo' de BoxPosition

        for (BoxPosition c : sortedBoxes) {
            sb.append("_B").append(c.name).append("(").append(c.r).append(",").append(c.c).append(")");
        }
        return sb.toString();
    }

    /**
     * Vérifie si c'est un état de victoire (toutes les caisses sur des cibles).
     */
    public boolean isGoal() {
        // Le but est atteint si TOUTES les positions des caisses
        // sont DANS l'ensemble des positions des CIBLES.
        for (BoxPosition box : boxCoords) {
            if (!TARGETS.contains(new Position(box.r, box.c))) {
                return false; // Une caisse n'est pas sur une cible
            }
        }
        return true; // Toutes les caisses sont sur des cibles
    }

    /**
     * Calcule l'heuristique h(n) (Somme des distances de Manhattan).
     * Estime le "coût restant" (nombre min de poussées) pour gagner.
     */
    private int calculateHeuristic() {
        int h = 0;

        // Pour chaque caisse...
        for (BoxPosition box : boxCoords) {
            int minManhattan = Integer.MAX_VALUE;

            // ...trouver la distance à la CIBLE la plus proche
            for (Position target : TARGETS) {
                // Si la caisse est déjà sur cette cible, la distance est 0
                if (box.r == target.r && box.c == target.c) {
                    minManhattan = 0;
                    break;
                }

                // Distance de Manhattan: |x1 - x2| + |y1 - y2|
                int manhattan = Math.abs(box.r - target.r) + Math.abs(box.c - target.c);
                if (manhattan < minManhattan) {
                    minManhattan = manhattan;
                }
            }
            h += minManhattan;
        }
        return h;
    }

    /**
     * Génère tous les états successeurs (voisins) valides.
     * C'est le cœur de la logique du jeu.
     */
    public List<Etat> generateSuccessors() {
        List<Etat> successors = new ArrayList<>();

        // Itération sur les 4 directions (Haut, Bas, Gauche, Droite)
        for (int i = 0; i < 4; i++) {
            int dr = SokobanSolver.DIRS[i][0];
            int dc = SokobanSolver.DIRS[i][1];
            String actionDirection = SokobanSolver.DIR_NAMES[i];

            // Position adjacente (là où le joueur veut aller)
            int nextPR = playerR + dr;
            int nextPC = playerC + dc;

            // Si c'est un mur, mouvement impossible
            if (STATIC_BOARD[nextPR][nextPC] == SokobanSolver.WALL) {
                continue;
            }

            char cellAtNextPos = this.board[nextPR][nextPC];

            // --- Cas 1 : Poussée de Caisse (PUSH) ---
            // Si la case adjacente contient une caisse...
            if (SokobanSolver.isBoxSymbol(cellAtNextPos)) {

                // Position derrière la caisse (cible de la poussée)
                int targetR = nextPR + dr;
                int targetC = nextPC + dc;

                // Vérification de la cible de la poussée:
                // 1. Est-ce un mur ?
                // 2. Y a-t-il une AUTRE caisse ?
                if (STATIC_BOARD[targetR][targetC] != SokobanSolver.WALL &&
                        !SokobanSolver.isBoxSymbol(this.board[targetR][targetC]))
                {
                    // --- Poussée valide ---
                    Etat newState = new Etat(this); // Crée une copie

                    // ******** CORRECTION COÛT A* ********
                    // On incrémente g(n) SEULEMENT pour une poussée.
                    newState.g_cost += 1;
                    // **********************************

                    // Trouver la caisse qui est poussée
                    BoxPosition boxToPush = null;
                    for(BoxPosition b : newState.boxCoords) {
                        if(b.r == nextPR && b.c == nextPC) {
                            boxToPush = b;
                            break;
                        }
                    }

                    newState.action = "PUSH '" + boxToPush.name + "' " + actionDirection;

                    // 1. Mettre à jour l'ensemble des caisses
                    newState.boxCoords.remove(boxToPush);
                    newState.boxCoords.add(new BoxPosition(targetR, targetC, boxToPush.name));

                    // 2. Mettre à jour la position du joueur
                    newState.playerR = nextPR;
                    newState.playerC = nextPC;

                    // 3. Mettre à jour la GRILLE VISUELLE (board)
                    // A. Ancienne position du Joueur
                    newState.board[playerR][playerC] = STATIC_BOARD[playerR][playerC]; // (devient T ou □)

                    // B. Ancienne position de la Caisse (devient le Joueur)
                    newState.board[nextPR][nextPC] = (STATIC_BOARD[nextPR][nextPC] == SokobanSolver.TARGET) ? SokobanSolver.PLAYER_ON_TARGET : SokobanSolver.PLAYER;

                    // C. Nouvelle position de la Caisse
                    newState.board[targetR][targetC] = (STATIC_BOARD[targetR][targetC] == SokobanSolver.TARGET)
                            ? SokobanSolver.BOX_TO_TARGET_MAP.get(boxToPush.name) // (devient 'A', 'B'...)
                            : boxToPush.name; // (devient 'a', 'b'...)

                    // 4. Recalculer les coûts h(n) et f(n)
                    newState.h_cost = newState.calculateHeuristic();
                    newState.f_cost = newState.g_cost + newState.h_cost;

                    successors.add(newState);
                }
            }
            // --- Cas 2 : Mouvement Simple du Joueur (MOVE) ---
            else {
                Etat newState = new Etat(this); // Crée une copie
                newState.action = "MOVE " + actionDirection; // Action (pas de coût)

                // ******** CORRECTION COÛT A* ********
                // g_cost n'est PAS incrémenté. Le mouvement est "gratuit".
                // h_cost ne change pas, car les caisses n'ont pas bougé.
                // **********************************

                // Mettre à jour la position du joueur
                newState.playerR = nextPR;
                newState.playerC = nextPC;

                // Mettre à jour la GRILLE VISUELLE (board)
                // A. Ancienne position du Joueur
                newState.board[playerR][playerC] = STATIC_BOARD[playerR][playerC]; // (devient T ou □)
                // B. Nouvelle position du Joueur
                newState.board[nextPR][nextPC] = (STATIC_BOARD[nextPR][nextPC] == SokobanSolver.TARGET) ? SokobanSolver.PLAYER_ON_TARGET : SokobanSolver.PLAYER;

                // 4. Recalculer les coûts h(n) et f(n)
                newState.h_cost = newState.calculateHeuristic(); // (h ne change pas, mais on le garde)
                newState.f_cost = newState.g_cost + newState.h_cost;

                successors.add(newState);
            }
        }
        return successors;
    }

    /**
     * Comparateur pour la PriorityQueue.
     * Compare les états par 'f_cost' (le plus bas en premier).
     */
    @Override
    public int compareTo(Etat other) {
        return Integer.compare(this.f_cost, other.f_cost);
    }
}