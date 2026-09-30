package br.mackenzie.musichords.model;

import java.util.ArrayList;
import java.util.List;

public class Progressao implements Comparable<Progressao> {
    private List<Vertice> caminho;
    private int custoTotal;

    public Progressao(List<Vertice> caminho, int custoTotal) {
        this.caminho = new ArrayList<>(caminho);
        this.custoTotal = custoTotal;
    }

    public List<Vertice> getCaminho() {
        return caminho;
    }

    public int getCustoTotal() {
        return custoTotal;
    }

    @Override
    public int compareTo(Progressao outra) {
        return Integer.compare(this.custoTotal, outra.custoTotal);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < caminho.size(); i++) {
            sb.append(ChordIdentifier.getChordFromVertexLabel(caminho.get(i).getRotulo()));
            if (i < caminho.size() - 1) sb.append(" - ");
        }
        return sb.toString();
    }
}
