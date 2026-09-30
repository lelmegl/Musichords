package br.mackenzie.musichords.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Classe que representa um nó (Tonalidade_Grau) e sua lista de adjacências.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 22/09/2026 | Pedro | Criação da estrutura base, Getters e método adicionarAresta.
 */
public class Vertice {
    private int id;
    private String rotulo;
    private List<Aresta> adjacencias;

    public Vertice(int id, String rotulo) {
        this.id = id;
        this.rotulo = rotulo;
        this.adjacencias = new LinkedList<>();
    }

    public void adicionarAresta(Aresta aresta) {
        this.adjacencias.add(aresta);
    }

    public int getId() {
        return id;
    }

    public String getRotulo() {
        return rotulo;
    }

    public List<Aresta> getAdjacencias() {
        return adjacencias;
    }
}
