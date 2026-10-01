/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Classe que representa o custo de transição física (aresta) entre dois acordes.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 22/09/2026 | Pedro | Criação da estrutura base da classe Aresta e métodos Getters.
 */
public class Aresta {
    private int destinoId;
    private int peso;

    public Aresta(int destinoId, int peso) {
        this.destinoId = destinoId;
        this.peso = peso;
    }

    public int getDestinoId() {
        return destinoId;
    }

    public int getPeso() {
        return peso;
    }
}