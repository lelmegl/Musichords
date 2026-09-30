package br.mackenzie.musichords.model;

import java.util.*;

public class ChordIdentifier {

    // Maps a set of notes to a chord name
    public static String identifyChord(Set<String> notes) {
        if (notes.isEmpty()) return "Desconhecido";
        
        // Remove duplicates and sort for easier matching (simplified)
        List<String> uniqueNotes = new ArrayList<>(notes);
        Collections.sort(uniqueNotes);
        
        // Simple mock mapping for the prototype based on common triads
        // Note: For a real app, this would use intervals to detect roots and qualities.
        if (containsAll(notes, "C", "E", "G")) return "C"; // Dó Maior
        if (containsAll(notes, "G", "B", "D")) return "G"; // Sol Maior
        if (containsAll(notes, "D", "F#", "A")) return "D"; // Ré Maior
        if (containsAll(notes, "A", "C#", "E")) return "A"; // Lá Maior
        if (containsAll(notes, "E", "G#", "B")) return "E"; // Mi Maior
        if (containsAll(notes, "F", "A", "C")) return "F"; // Fá Maior
        if (containsAll(notes, "B", "D#", "F#")) return "B"; // Si Maior
        
        // Minors
        if (containsAll(notes, "A", "C", "E")) return "Am"; // Lá Menor
        if (containsAll(notes, "E", "G", "B")) return "Em"; // Mi Menor
        if (containsAll(notes, "D", "F", "A")) return "Dm"; // Ré Menor
        if (containsAll(notes, "B", "D", "F#")) return "Bm"; // Si Menor
        
        return "Desconhecido";
    }
    
    private static boolean containsAll(Set<String> notes, String... required) {
        for (String req : required) {
            if (!notes.contains(req)) return false;
        }
        return true;
    }

    // Maps a graph vertex label (Tonalidade_Grau) to the physical chord it represents
    public static String getChordFromVertexLabel(String label) {
        // Label format: "C_I", "G_IV", etc.
        String[] parts = label.split("_");
        if (parts.length != 2) return label;
        
        String key = parts[0];
        String degree = parts[1];
        
        // We can determine the physical chord based on the major scale intervals
        // For simplicity in this prototype, we'll implement a static lookup table or basic logic
        // The scales order of notes:
        String[] notes = {"C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};
        
        // Find index of key
        int keyIndex = -1;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].equals(key)) {
                keyIndex = i;
                break;
            }
        }
        
        if (keyIndex == -1) return label;
        
        // Major scale intervals: W, W, H, W, W, W, H (2, 2, 1, 2, 2, 2, 1)
        int[] intervals = {0, 2, 4, 5, 7, 9, 11};
        
        int degreeIndex = -1;
        boolean isMinor = false;
        boolean isDiminished = false;
        
        switch (degree) {
            case "I": degreeIndex = 0; break;
            case "ii": degreeIndex = 1; isMinor = true; break;
            case "iii": degreeIndex = 2; isMinor = true; break;
            case "IV": degreeIndex = 3; break;
            case "V": degreeIndex = 4; break;
            case "vi": degreeIndex = 5; isMinor = true; break;
            case "vii": degreeIndex = 6; isDiminished = true; break;
            default: return label;
        }
        
        int chordRootIndex = (keyIndex + intervals[degreeIndex]) % 12;
        String chordRoot = notes[chordRootIndex];
        
        if (isDiminished) return chordRoot + "dim";
        if (isMinor) return chordRoot + "m";
        return chordRoot;
    }
}
