package com.fstt.devoir;

import java.util.Objects;

/**
 * Classe de données spécialisée pour une Caisse (Ligne, Colonne, Nom).
 * Essentielle pour suivre chaque caisse ('a', 'b', 'c'...) individuellement.
 */

class BoxPosition implements Comparable<BoxPosition> {
    public final int r;
    public final int c;
    public final char name; // 'a', 'b', 'c', ou 'd'

    public BoxPosition(int r, int c, char name) {
        this.r = r;
        this.c = c;
        // On s'assure que le nom stocké est toujours en minuscule ('A' -> 'a')
        this.name = (name >= 'A' && name <= 'D') ? SokobanSolver.BOX_TO_TARGET_MAP.get(name) : name;
    }

    // 'equals' et 'hashCode' pour le 'HashSet' des Caisses (boxCoords)
    // Sont basés sur la position (r, c) ET le nom.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxPosition that = (BoxPosition) o;
        return r == that.r && c == that.c && name == that.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c, name);
    }

    /**
     * Comparateur pour trier les caisses.
     * Essentiel pour créer la 'getUniqueKey' de l'état.
     * Trie d'abord par nom, puis par ligne, puis par colonne.
     */
    @Override
    public int compareTo(BoxPosition other) {
        if (this.name != other.name) {
            return Character.compare(this.name, other.name);
        }
        if (this.r != other.r) {
            return Integer.compare(this.r, other.r);
        }
        return Integer.compare(this.c, other.c);
    }
}