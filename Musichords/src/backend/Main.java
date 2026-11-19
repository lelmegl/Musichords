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
 * 11/11/2026 | Pedro   | Mensagens de sucesso só aparecem quando a operação realmente acontece.
 * 13/11/2026 | Pedro   | Parte 3: título da aplicação e opções k..o (Dijkstra, graus e Euler,
 *                        Hamilton e Caixeiro Viajante, coloração e árvore geradora mínima).
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Grafo musichordsGrafo = new Grafo();
        String opcao = "";

        while (!opcao.equals("j")) {
            System.out.println("\n=====================================================================");
            System.out.println("  MUSICHORDS - Progressões Harmônicas de Menor Esforço via Grafos");
            System.out.println("=====================================================================");
            System.out.println("a) Ler dados do arquivo grafo.txt");
            System.out.println("b) Gravar dados no arquivo grafo.txt");
            System.out.println("c) Inserir vértice");
            System.out.println("d) Inserir aresta");
            System.out.println("e) Remover vértice");
            System.out.println("f) Remover aresta");
            System.out.println("g) Mostrar conteúdo do arquivo");
            System.out.println("h) Mostrar grafo (Lista de Adjacência)");
            System.out.println("i) Apresentar a conexidade (FCONEX) e o reduzido");
            System.out.println("--- Parte 3: solução do problema e características do grafo ---");
            System.out.println("k) Progressão de menor esforço entre dois acordes (Dijkstra)");
            System.out.println("l) Graus dos vértices e caminho euleriano");
            System.out.println("m) Hamilton: Teoremas 1 e 2, ciclo hamiltoniano e Caixeiro Viajante");
            System.out.println("n) Coloração por classe (Welsh e Powell) e k-partição");
            System.out.println("o) Árvore geradora de custo mínimo (Kruskal)");
            System.out.println("j) Encerrar a aplicação");
            System.out.print("Escolha uma opção: ");

            opcao = scanner.nextLine().toLowerCase();

            // as opções da Parte 3 analisam o grafo carregado: sem vértices não há o que analisar
            if (opcao.length() == 1 && opcao.charAt(0) >= 'k' && opcao.charAt(0) <= 'o'
                    && musichordsGrafo.getVertices().isEmpty()) {
                System.out.println("O grafo está vazio. Use a opção a) para ler o grafo.txt primeiro.");
                continue;
            }

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
                        if (rotulo.trim().isEmpty()) {
                            System.out.println("O rótulo não pode ser vazio.");
                        } else if (musichordsGrafo.inserirVertice(idVertice, rotulo.trim())) {
                            System.out.println("Vértice inserido com sucesso.");
                        }
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
                        if (musichordsGrafo.inserirAresta(idOrigem, idDestino, peso)) {
                            System.out.println("Aresta inserida com sucesso.");
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Entrada inválida! Digite apenas números.");
                    }
                    break;
                case "e":
                    try {
                        System.out.print("Digite o ID do vértice a remover: ");
                        int idRemover = Integer.parseInt(scanner.nextLine());
                        // removerVertice já informa se o vértice foi removido ou não existia
                        musichordsGrafo.removerVertice(idRemover);
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
                        // removerAresta já informa se a aresta foi removida ou não existia
                        musichordsGrafo.removerAresta(origemRemover, destinoRemover);
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
                case "k":
                    System.out.print("Acorde de origem (ID ou rótulo, ex.: G_I): ");
                    Integer origem = lerVertice(musichordsGrafo, scanner.nextLine());
                    System.out.print("Acorde de destino (ID ou rótulo, ex.: C_I): ");
                    Integer destino = lerVertice(musichordsGrafo, scanner.nextLine());
                    if (origem == null || destino == null) {
                        System.out.println("Vértice não encontrado. Informe um ID ou rótulo existente.");
                    } else {
                        musichordsGrafo.mostrarMenorEsforco(origem, destino);
                    }
                    break;
                case "l":
                    musichordsGrafo.analisarGrausEEuler();
                    break;
                case "m":
                    musichordsGrafo.analisarHamilton();
                    break;
                case "n":
                    musichordsGrafo.mostrarColoracao();
                    break;
                case "o":
                    musichordsGrafo.mostrarArvoreGeradoraMinima();
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

    /** Converte o que o usuário digitou (ID numérico ou rótulo como "C_I") no ID do vértice; null se não existir. */
    private static Integer lerVertice(Grafo grafo, String entrada) {
        String texto = entrada.trim();
        try {
            int id = Integer.parseInt(texto);
            return grafo.getVerticeById(id) != null ? id : null;
        } catch (NumberFormatException e) {
            Vertice v = grafo.getVerticeByRotulo(texto);
            return v != null ? v.getId() : null;
        }
    }
}
