import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Gerador de JSON mínimo (sem bibliotecas externas) usado pelo servidor para
 * enviar os dados do back-end para a interface no navegador.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe.
 */
public final class Json {

    private Json() {
    }

    /** Objeto JSON ordenado. Uso: Json.obj().put("a", 1).put("b", "x") */
    public static Obj obj() {
        return new Obj();
    }

    public static List<Object> lista() {
        return new ArrayList<>();
    }

    public static class Obj extends LinkedHashMap<String, Object> {
        public Obj put(String chave, Object valor) {
            super.put(chave, valor);
            return this;
        }

        @Override
        public String toString() {
            return Json.escrever(this);
        }
    }

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(sb, valor);
        return sb.toString();
    }

    private static void escrever(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            texto(sb, (String) v);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                texto(sb, String.valueOf(e.getKey()));
                sb.append(':');
                escrever(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Collection) {
            sb.append('[');
            boolean primeiro = true;
            for (Object o : (Collection<?>) v) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(sb, o);
            }
            sb.append(']');
        } else if (v instanceof int[]) {
            int[] a = (int[]) v;
            sb.append('[');
            for (int i = 0; i < a.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(a[i]);
            }
            sb.append(']');
        } else if (v instanceof String[]) {
            List<Object> l = new ArrayList<>();
            for (String s : (String[]) v) l.add(s);
            escrever(sb, l);
        } else {
            texto(sb, v.toString());
        }
    }

    private static void texto(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }
}
