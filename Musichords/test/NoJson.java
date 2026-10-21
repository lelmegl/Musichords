import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Leitor de JSON mínimo usado só pelos testes, para conferir as respostas da API
 * campo a campo (o projeto não usa bibliotecas externas, então o parser fica aqui).
 *   NoJson r = NoJson.ler("{\"vertices\":84}");  r.get("vertices").inteiro() == 84
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação da classe.
 */
final class NoJson {

    private final Object valor;
    private final String caminho;

    private NoJson(Object valor, String caminho) {
        this.valor = valor;
        this.caminho = caminho;
    }

    static NoJson ler(String texto) {
        Leitor l = new Leitor(texto);
        Object v = l.valor();
        l.espacos();
        if (l.i != texto.length()) throw new IllegalArgumentException("Conteúdo extra após o JSON na posição " + l.i);
        return new NoJson(v, "$");
    }

    // ------------------------------------------------------------------ navegação

    NoJson get(String chave) {
        if (!(valor instanceof Map)) throw new AssertionError(caminho + " não é um objeto JSON: " + this);
        Map<?, ?> m = (Map<?, ?>) valor;
        if (!m.containsKey(chave)) throw new AssertionError("Campo \"" + chave + "\" ausente em " + caminho + ": " + this);
        return new NoJson(m.get(chave), caminho + "." + chave);
    }

    NoJson get(int indice) {
        List<?> l = lista();
        if (indice < 0 || indice >= l.size()) throw new AssertionError(caminho + "[" + indice + "] fora da lista de " + l.size());
        return new NoJson(l.get(indice), caminho + "[" + indice + "]");
    }

    boolean tem(String chave) {
        return valor instanceof Map && ((Map<?, ?>) valor).containsKey(chave);
    }

    List<NoJson> itens() {
        List<NoJson> r = new ArrayList<>();
        List<?> l = lista();
        for (int i = 0; i < l.size(); i++) r.add(new NoJson(l.get(i), caminho + "[" + i + "]"));
        return r;
    }

    int tamanho() {
        if (valor instanceof Map) return ((Map<?, ?>) valor).size();
        return lista().size();
    }

    // ------------------------------------------------------------------ valores

    boolean nulo() {
        return valor == null;
    }

    int inteiro() {
        if (!(valor instanceof Number)) throw new AssertionError(caminho + " não é número: " + this);
        return ((Number) valor).intValue();
    }

    String texto() {
        if (!(valor instanceof String)) throw new AssertionError(caminho + " não é texto: " + this);
        return (String) valor;
    }

    boolean logico() {
        if (!(valor instanceof Boolean)) throw new AssertionError(caminho + " não é booleano: " + this);
        return (Boolean) valor;
    }

    List<Integer> inteiros() {
        List<Integer> r = new ArrayList<>();
        for (NoJson n : itens()) r.add(n.inteiro());
        return r;
    }

    private List<?> lista() {
        if (!(valor instanceof List)) throw new AssertionError(caminho + " não é uma lista JSON: " + this);
        return (List<?>) valor;
    }

    @Override
    public String toString() {
        String s = String.valueOf(valor);
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    // ------------------------------------------------------------------ parser

    private static final class Leitor {
        private final String s;
        private int i;

        Leitor(String s) {
            this.s = s;
        }

        Object valor() {
            espacos();
            if (i >= s.length()) throw erro("fim inesperado");
            char c = s.charAt(i);
            switch (c) {
                case '{': return objeto();
                case '[': return lista();
                case '"': return texto();
                case 't': literal("true"); return Boolean.TRUE;
                case 'f': literal("false"); return Boolean.FALSE;
                case 'n': literal("null"); return null;
                default: return numero();
            }
        }

        private Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            esperar('{');
            espacos();
            if (proximo() == '}') {
                i++;
                return m;
            }
            while (true) {
                espacos();
                String chave = texto();
                espacos();
                esperar(':');
                m.put(chave, valor());
                espacos();
                if (proximo() == ',') {
                    i++;
                    continue;
                }
                esperar('}');
                return m;
            }
        }

        private List<Object> lista() {
            List<Object> l = new ArrayList<>();
            esperar('[');
            espacos();
            if (proximo() == ']') {
                i++;
                return l;
            }
            while (true) {
                l.add(valor());
                espacos();
                if (proximo() == ',') {
                    i++;
                    continue;
                }
                esperar(']');
                return l;
            }
        }

        private String texto() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw erro("texto sem aspas de fechamento");
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: throw erro("escape inválido \\" + e);
                }
            }
        }

        private Number numero() {
            int inicio = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String n = s.substring(inicio, i);
            if (n.isEmpty()) throw erro("valor inválido");
            if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
            return Long.parseLong(n);
        }

        private void literal(String palavra) {
            if (!s.startsWith(palavra, i)) throw erro("esperava " + palavra);
            i += palavra.length();
        }

        private void esperar(char c) {
            if (proximo() != c) throw erro("esperava '" + c + "'");
            i++;
        }

        private char proximo() {
            if (i >= s.length()) throw erro("fim inesperado");
            return s.charAt(i);
        }

        void espacos() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private IllegalArgumentException erro(String msg) {
            return new IllegalArgumentException("JSON inválido (posição " + i + "): " + msg);
        }
    }
}
