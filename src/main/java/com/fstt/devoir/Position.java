package com.fstt.devoir;

import java.util.Objects;

/**
 * Fichier contenant les classes pour les coordonnées.
 * * 1. Position: Une simple coordonnée (r, c). Utilisée pour les CIBLES.
 * 2. BoxPosition: Une coordonnée (r, c) + un nom (char). Utilisée pour les CAISSES.
 * * (Basé sur le fichier Coord.java de votre ami)
 */

/**
 * Classe de données pour une coordonnée statique (Ligne, Colonne).
 * Principalement utilisée pour stocker les positions des CIBLES (T).
 */
class Position {
    public final int r;
    public final int c;

    public Position(int r, int c) {
        this.r = r;
        this.c = c;
    }

    // 'equals' et 'hashCode' sont cruciaux pour le 'HashSet' des Cibles (TARGETS)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return r == position.r && c == position.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }
}