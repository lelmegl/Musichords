import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Integrantes:
 * Gustavo Kiyoshi Ikeda - 10439179
 * Pedro Montarroyos de Pinho - 10440213
 * Felipe Marques Leite Martha - 10437877
 *
 * Síntese: Testes de unidade do domínio musical (notas, identificação de acordes e campo
 * harmônico), que é a base das respostas dos endpoints /api/identificar e /api/progressoes.
 * Histórico de Alterações:
 * Data       | Autor | Descrição
 * 20/10/2026 | Pedro | Criação dos testes.
 */
public class DominioTest {

    private static Acorde identificar(int... casas) {
        return IdentificadorAcordes.identificar(casas).getAcorde();
    }

    // ================================================================== Nota

    @Test
    public void normalizarMantemClasseDeAlturaEntreZeroEOnze() {
        assertEquals(0, Nota.normalizar(12));
        assertEquals(11, Nota.normalizar(-1));
        assertEquals(7, Nota.normalizar(31));
    }

    @Test
    public void notaNaCordaSegueAfinacaoPadrao() {
        assertEquals(4, Nota.notaNaCorda(0, 0));  // Mi grave solta = E
        assertEquals(7, Nota.notaNaCorda(0, 3));  // casa 3 = G
        assertEquals(9, Nota.notaNaCorda(1, 0));  // Lá solta = A
        assertEquals(4, Nota.notaNaCorda(5, 12)); // Mi agudo, casa 12 = E
    }

    @Test
    public void parseConverteNomesComSustenidoEBemol() {
        assertEquals(1, Nota.parse("C#"));
        assertEquals(1, Nota.parse("Db"));
        assertEquals(10, Nota.parse("Bb"));
        assertEquals(-1, Nota.parse("H"));
        assertEquals(-1, Nota.parse(""));
    }

    // ================================================================== IdentificadorAcordes

    @Test
    public void identificaTriadesMaioresEMenores() {
        assertEquals("C", identificar(-1, 3, 2, 0, 1, 0).getCifra());
        assertEquals("G", identificar(3, 2, 0, 0, 0, 3).getCifra());
        assertEquals("Am", identificar(-1, 0, 2, 2, 1, 0).getCifra());
        assertEquals("Em", identificar(0, 2, 2, 0, 0, 0).getCifra());
        assertEquals("F", identificar(1, 3, 3, 2, 1, 1).getCifra());
    }

    @Test
    public void identificaTetradesEDiminuto() {
        assertEquals("G7", identificar(3, 2, 0, 0, 0, 1).getCifra());
        assertEquals("Cmaj7 = C7M", "C7M", identificar(-1, 3, 2, 0, 0, 0).getCifra());
        assertEquals(TipoAcorde.DIMINUTO, identificar(-1, 2, 3, 4, 3, -1).getTipo()); // B D F
    }

    @Test
    public void inversaoEhReconhecidaPeloBaixo() {
        IdentificadorAcordes.Resultado r = IdentificadorAcordes.identificar(new int[]{0, 3, 2, 0, 1, 0});
        assertEquals("C", r.getAcorde().getCifra());
        assertTrue(r.isInvertido());
        assertEquals(4, r.getBaixo()); // E no baixo
    }

    @Test
    public void menosDeTresNotasDiferentesNaoFormamAcorde() {
        assertNull(identificar(-1, -1, -1, -1, -1, -1));
        assertNull(identificar(0, -1, -1, -1, -1, 0));    // só E
        assertNull(identificar(3, -1, 0, -1, -1, 3));     // G e D (power chord)
    }

    @Test
    public void grafiaPadraoUsaBemolNosAcordesUsuais() {
        assertTrue(IdentificadorAcordes.grafiaPadrao(10, TipoAcorde.MAIOR));   // Bb
        assertTrue(IdentificadorAcordes.grafiaPadrao(3, TipoAcorde.MAIOR));    // Eb
        assertFalse(IdentificadorAcordes.grafiaPadrao(6, TipoAcorde.MAIOR));   // F#
        assertFalse(IdentificadorAcordes.grafiaPadrao(1, TipoAcorde.MENOR));   // C#m
    }

    // ================================================================== CampoHarmonico

    @Test
    public void campoHarmonicoDeDoMaior() {
        CampoHarmonico c = new CampoHarmonico("C");
        List<String> cifras = new ArrayList<>();
        for (Acorde a : c.acordes()) cifras.add(a.getCifra());
        assertEquals(Arrays.asList("C", "Dm", "Em", "F", "G", "Am", "Bdim"), cifras);
    }

    @Test
    public void campoHarmonicoDeSolTemFSustenidoDiminuto() {
        CampoHarmonico g = new CampoHarmonico("G");
        assertEquals("D", g.acorde(4).getCifra());
        assertEquals("F#dim", g.acorde(6).getCifra());
    }

    @Test
    public void funcoesTonaisDosGraus() {
        assertEquals(CampoHarmonico.FuncaoTonal.TONICA, CampoHarmonico.funcao(0));      // I
        assertEquals(CampoHarmonico.FuncaoTonal.SUBDOMINANTE, CampoHarmonico.funcao(3)); // IV
        assertEquals(CampoHarmonico.FuncaoTonal.DOMINANTE, CampoHarmonico.funcao(4));    // V
        assertEquals(CampoHarmonico.FuncaoTonal.TONICA, CampoHarmonico.funcao(5));      // vi
    }

    @Test
    public void escalaMaiorEPentatonicaRelativa() {
        CampoHarmonico c = new CampoHarmonico("C");
        assertEquals(Arrays.asList(0, 2, 4, 5, 7, 9, 11), c.escalaMaior());
        // pentatônica de Lá menor: A C D E G
        assertEquals(5, c.pentatonicaRelativa().size());
        assertTrue(c.pentatonicaRelativa().containsAll(Arrays.asList(9, 0, 2, 4, 7)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void tonalidadeInvalidaEhRecusada() {
        new CampoHarmonico("X");
    }
}
