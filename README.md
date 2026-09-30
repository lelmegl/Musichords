# Musichords - Modelagem de Acordes

Projeto universitário focado na modelagem de progressões harmônicas para guitarra utilizando grafos.
Este repositório contém duas partes integradas: 
1. **O motor de grafos (Backend):** Toda a lógica matemática para manipulação de vértices (papéis tonais), arestas funcionais, arestas de pivô, análise de conexidade e buscas de caminho mínimo (Dijkstra/DFS).
2. **A interface visual (Frontend):** Uma aplicação Desktop em JavaFX simulando o braço do instrumento (fretboard) para transformar de forma prática e amigável a teoria abstrata musical em interações físicas.

O projeto utiliza **Java 17+** de forma **portátil e independente**, sem o uso de ferramentas de build pesadas (como Maven).

---

## 🛠️ Como Executar o Projeto

Não é necessário instalar nenhuma biblioteca externa! O projeto contém scripts autônomos que preparam o ambiente para você.

### 🎸 Rodar a Interface Gráfica (Frontend + JavaFX)

**No Windows:**
Basta dar dois cliques no arquivo `rodar.bat` ou executá-lo no terminal:
```cmd
rodar.bat
```

**No Linux, Mac OS, ou GitHub Codespaces:**
Dê permissão de execução (se for a primeira vez) e rode o script bash:
```bash
chmod +x rodar.sh
./rodar.sh
```

*(O script vai identificar o seu sistema, baixar a pasta portátil do JavaFX SDK para a pasta `lib/`, compilar as classes em `bin/` e abrir o aplicativo.)*

> **Nota para Codespaces / Terminais de Nuvem:**
> Ambientes de texto puros não suportam telas gráficas nativamente. Para rodar o JavaFX lá, certifique-se de reconstruir o container com a opção Desktop Ativada (VNC) que está configurada no `.devcontainer`.

---

### 💻 Rodar Apenas a Lógica de Grafos (Backend no Console)
Se você deseja testar as manipulações do arquivo `grafo.txt` diretamente no terminal, sem abrir a interface gráfica:

**Em qualquer sistema (Linux/Windows/Mac):**
1. Abra o terminal na raiz do projeto.
2. Compile tudo para a pasta `bin`:
```bash
javac -d bin src/br/mackenzie/musichords/model/*.java
```
3. Execute a classe principal antiga do console:
```bash
java -cp bin br.mackenzie.musichords.model.Main
```
4. O Menu interativo clássico (`a) Ler dados...`, `b) Gravar dados...`) vai aparecer no terminal.

---

### Estrutura do Projeto
* `src/br/mackenzie/musichords/model`: Motor de grafos puro (Java sem dependências externas).
* `src/br/mackenzie/musichords/ui`: Componentes visuais do JavaFX.
* `src/br/mackenzie/musichords/controller`: Integração do clique do usuário com a matemática do Grafo.
* `rodar.bat` / `rodar.sh`: Scripts que ligam toda a mágica da interface.
