import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Acorde físico (raiz + qualidade). Dois acordes são iguais se tiverem a mesma raiz
 * (classe de altura) e o mesmo tipo, independentemente da grafia (C# ou Db).
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 30/09/2026 | Pedro | Criação da classe Acorde do modelo de domínio.
 */
public class Acorde {
    private final int raiz;
    private final TipoAcorde tipo;
    private final boolean grafiaBemol;

    public Acorde(int raiz, TipoAcorde tipo, boolean grafiaBemol) {
        this.raiz = Nota.normalizar(raiz);
        this.tipo = tipo;
        this.grafiaBemol = grafiaBemol;
    }

    public Acorde(int raiz, TipoAcorde tipo) {
        this(raiz, tipo, false);
    }

    public int getRaiz() {
        return raiz;
    }

    public TipoAcorde getTipo() {
        return tipo;
    }

    public boolean isGrafiaBemol() {
        return grafiaBemol;
    }

    public Acorde comGrafia(boolean bemol) {
        return new Acorde(raiz, tipo, bemol);
    }

    /** Tríade equivalente (G7 -> G, Am7 -> Am), usada para localizar o acorde no grafo. */
    public Acorde triade() {
        return tipo.isTriade() ? this : new Acorde(raiz, tipo.triadeBase(), grafiaBemol);
    }

    public List<Integer> notas() {
        List<Integer> notas = new ArrayList<>();
        for (int i : tipo.getIntervalos()) notas.add(Nota.normalizar(raiz + i));
        return notas;
    }

    /** Cifra: C, Ebm, Bdim, G7... */
    public String getCifra() {
        return Nota.nome(raiz, grafiaBemol) + tipo.getSufixo();
    }

    /** Nome por extenso: "Sol maior", "Mi menor", "Si diminuto". */
    public String getNomeExtenso() {
        return Nota.nomePt(raiz, grafiaBemol) + " " + tipo.getDescricao();
    }

    /** Nome curto como no wireframe: "Sol", "Mi menor", "Si dim". */
    public String getNomeCurto() {
        String nota = Nota.nomePt(raiz, grafiaBemol);
        switch (tipo) {
            case MAIOR: return nota;
            case MENOR: return nota + " menor";
            case DIMINUTO: return nota + " dim";
            default: return nota + " " + tipo.getDescricao();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Acorde)) return false;
        Acorde a = (Acorde) o;
        return raiz == a.raiz && tipo == a.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raiz, tipo);
    }

    @Override
    public String toString() {
        return getCifra();
    }
}
