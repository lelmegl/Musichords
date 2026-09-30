# Musichords - Modelagem de Acordes

Projeto universitário focado na modelagem de progressões harmônicas para guitarra utilizando grafos.
Este repositório contém duas partes integradas: 
1. **O motor de grafos (Backend):** Toda a lógica matemática para manipulação de vértices (papéis tonais), arestas funcionais, arestas de pivô, análise de conexidade e buscas de caminho mínimo (Dijkstra/DFS).
2. **A interface visual (Frontend):** Uma aplicação Desktop em JavaFX simulando o braço do instrumento (fretboard) para transformar de forma prática e amigável a teoria abstrata musical em interações físicas.

O projeto utiliza **Java 17+** e foi estruturado com o **Maven** para gerenciar de forma automatizada o JavaFX e o fluxo de compilação.

---

## 🛠️ Como Executar o Projeto

Como as partes lógicas de grafos (Console) e a interface gráfica (JavaFX) estão juntas no mesmo pacote e bem separadas, **sim, é perfeitamente possível executar cada uma de forma independente!** 

Escolha abaixo qual experiência você quer rodar:

### Opção 1: Rodar a Interface Gráfica Completa (Frontend JavaFX)
Se você quer visualizar o protótipo real com as telas, as cores e a guitarra interativa comunicando-se com a lógica dos grafos.

**Pelo Terminal (Com o Maven):**
1. Abra o terminal na raiz do projeto (`C:\Users\T-Gamer\Downloads\Musichords`).
2. Execute o comando:
```bash
mvn clean javafx:run
```
*(Nota: O JavaFX precisa de um ambiente com monitor/vídeo configurado, logo esse comando não funciona em terminais puramente virtuais (headless) da nuvem, como o próprio terminal do GitHub Codespaces sem servidor X11).*

---

### Opção 2: Rodar Somente a Lógica Pura de Grafos (Backend no Console)
Se você deseja testar as manipulações do arquivo `grafo.txt`, inserir vértices, remover arestas, checar a Conexidade (FCONEX) ou ver a modelagem via terminal textual, sem abrir interface gráfica.

**Pelo Terminal (Com Maven):**
1. Abra o terminal na raiz do projeto.
2. Você pode executar o console interativo através do comando `exec:java`:
```bash
mvn compile exec:java -Dexec.mainClass="br.mackenzie.musichords.model.Main"
```
3. O Menu de terminal clássico (`a) Ler dados...`, `b) Gravar dados...`) vai aparecer diretamente no seu terminal.

---

### Estrutura do Projeto Atualizada
* `src/main/java/br/mackenzie/musichords/model`: Contém todo o motor de manipulação de grafos `Grafo`, `Vertice`, `Aresta`, as buscas matemáticas (`buscarProgressoes`), o identificador de teoria musical (`ChordIdentifier`) e o terminal antigo (`Main`).
* `src/main/java/br/mackenzie/musichords/ui`: Contém componentes visuais reutilizáveis (`FretboardView`).
* `src/main/java/br/mackenzie/musichords/controller`: Contém o cérebro que liga a interface aos Grafos (`MainController`).
* `src/main/resources`: Arquivos estáticos como base de dados inicial do grafo (`grafo.txt`) e estilização visual (`styles.css`).
