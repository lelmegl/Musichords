package br.mackenzie.musichords.model;

import java.util.Scanner;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Classe principal que executa o menu interativo da aplicação
 * Musichords.
 * Histórico de Alterações:
 * Data       | Autor   | Descrição
 * 22/09/2026 | Pedro   | Estruturação do menu com opções de 'a' a 'j'.
 * 28/09/2026 | Gustavo | Implementação da lógica de captura de dados no menu para inserção e remoção.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Grafo musichordsGrafo = new Grafo();
        String opcao = "";

        while (!opcao.equals("j")) {
            System.out.println("\n========================================");
            System.out.println("   MUSICHORDS - MODELAGEM DE ACORDES    ");
            System.out.println("========================================");
            System.out.println("a) Ler dados do arquivo grafo.txt");
            System.out.println("b) Gravar dados no arquivo grafo.txt");
            System.out.println("c) Inserir vértice");
            System.out.println("d) Inserir aresta");
            System.out.println("e) Remover vértice");
            System.out.println("f) Remover aresta");
            System.out.println("g) Mostrar conteúdo do arquivo");
            System.out.println("h) Mostrar grafo (Lista de Adjacência)");
            System.out.println("i) Apresentar a conexidade (FCONEX) e o reduzido");
            System.out.println("j) Encerrar a aplicação");
            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextLine().toLowerCase();

            switch (opcao) {
                case "a":
                    musichordsGrafo.lerArquivo("grafo.txt");
                    break;
                case "b":
                    musichordsGrafo.gravarArquivo("grafo.txt");
                    break;
                case "c":
                    try {
                        System.out.print("Digite o ID do vértice: ");
                        int idVertice = Integer.parseInt(scanner.nextLine());
                        System.out.print("Digite o rótulo do vértice: ");
                        String rotulo = scanner.nextLine();
                        musichordsGrafo.inserirVertice(idVertice, rotulo);
                        System.out.println("Vértice inserido com sucesso.");
                    } catch (NumberFormatException ex) {
                        System.out.println("ID inválido!");
                    }
                    break;
                case "d":
                    try {
                        System.out.print("Digite o ID de origem: ");
                        int idOrigem = Integer.parseInt(scanner.nextLine());
                        System.out.print("Digite o ID de destino: ");
                        int idDestino = Integer.parseInt(scanner.nextLine());
                        System.out.print("Digite o peso: ");
                        int peso = Integer.parseInt(scanner.nextLine());
                        musichordsGrafo.inserirAresta(idOrigem, idDestino, peso);
                        System.out.println("Aresta inserida com sucesso.");
                    } catch (NumberFormatException ex) {
                        System.out.println("Entrada inválida! Digite apenas números.");
                    }
                    break;
                case "e":
                    try {
                        System.out.print("Digite o ID do vértice a remover: ");
                        int idRemover = Integer.parseInt(scanner.nextLine());
                        musichordsGrafo.removerVertice(idRemover);
                        System.out.println("Vértice removido com sucesso.");
                    } catch (NumberFormatException ex) {
                        System.out.println("ID inválido!");
                    }
                    break;
                case "f":
                    try {
                        System.out.print("Digite o ID de origem da aresta: ");
                        int origemRemover = Integer.parseInt(scanner.nextLine());
                        System.out.print("Digite o ID de destino da aresta: ");
                        int destinoRemover = Integer.parseInt(scanner.nextLine());
                        musichordsGrafo.removerAresta(origemRemover, destinoRemover);
                        System.out.println("Aresta removida com sucesso.");
                    } catch (NumberFormatException ex) {
                        System.out.println("Entrada inválida!");
                    }
                    break;
                case "g":
                    musichordsGrafo.mostrarConteudoArquivo();
                    break;
                case "h":
                    musichordsGrafo.mostrarGrafo();
                    break;
                case "i":
                    musichordsGrafo.analisarConexidade();
                    break;
                case "j":
                    System.out.println("Encerrando Musichords");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
        scanner.close();
    }
}
