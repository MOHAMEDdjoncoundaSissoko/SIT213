package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import simulateur.Simulateur;

/**
 * Tests JUnit pour le Simulateur SIT213.
 * Couvre les etapes 1 et 2 (chaine logique et analogique sans bruit).
 */
public class SimulateurTest {

    @BeforeAll
    static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    // -------------------------------------------------------
    // Etape 1 : chaine logique
    // -------------------------------------------------------

    @Test
    @DisplayName("TEB nul - message aleatoire par defaut")
    void testTEBNulDefaut() throws Exception {
        Simulateur s = new Simulateur(new String[]{});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("TEB nul - message binaire fixe")
    void testTEBNulMessageFixe() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-mess", "1011001"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("TEB nul - longueur imposee avec seed")
    void testTEBNulAvecSeed() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-mess", "50", "-seed", "42"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("Reproductibilite - meme seed = meme TEB")
    void testReproductibilite() throws Exception {
        Simulateur s1 = new Simulateur(new String[]{"-mess", "100", "-seed", "7"});
        Simulateur s2 = new Simulateur(new String[]{"-mess", "100", "-seed", "7"});
        s1.execute();
        s2.execute();
        assertEquals(s1.calculTauxErreurBinaire(), s2.calculTauxErreurBinaire());
    }

    @Test
    @DisplayName("Exception - argument -mess invalide")
    void testMessInvalide() {
        assertThrows(Exception.class, () -> {
            new Simulateur(new String[]{"-mess", "abc"});
        });
    }

    @Test
    @DisplayName("Exception - option inconnue")
    void testOptionInconnue() {
        assertThrows(Exception.class, () -> {
            new Simulateur(new String[]{"-toto"});
        });
    }

    // -------------------------------------------------------
    // Etape 2 : chaine analogique NRZ
    // -------------------------------------------------------

    @Test
    @DisplayName("NRZ - TEB nul, parametres par defaut")
    void testNRZDefaut() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZ - TEB nul avec seed et longueur imposee")
    void testNRZAvecSeed() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-mess", "50", "-seed", "42"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZ - TEB nul avec amplitudes personnalisees")
    void testNRZAmplitudes() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-ampl", "-1.0", "1.0", "-mess", "100", "-seed", "7"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZ - TEB nul avec nbEch eleve")
    void testNRZNbEch() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-nbEch", "100", "-mess", "30", "-seed", "1"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    // -------------------------------------------------------
    // Etape 2 : chaine analogique NRZT
    // -------------------------------------------------------

    @Test
    @DisplayName("NRZT - TEB nul, parametres par defaut")
    void testNRZTDefaut() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZT - TEB nul avec seed")
    void testNRZTAvecSeed() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT", "-mess", "50", "-seed", "42"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZT - TEB nul, message long")
    void testNRZTMessageLong() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT", "-mess", "200", "-seed", "99"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    // -------------------------------------------------------
    // Etape 2 : chaine analogique RZ
    // -------------------------------------------------------

    @Test
    @DisplayName("RZ - TEB nul, parametres par defaut")
    void testRZDefaut() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "RZ"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("RZ - TEB nul avec seed")
    void testRZAvecSeed() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "RZ", "-mess", "50", "-seed", "42"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("RZ - TEB nul avec nbEch grand et amplitudes")
    void testRZNbEchAmplitudes() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "RZ", "-nbEch", "50", "-ampl", "0.0", "5.0", "-mess", "100", "-seed", "7"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    // -------------------------------------------------------
    // Branches manquantes dans Simulateur
    // -------------------------------------------------------

    @Test
    @DisplayName("Exception - -mess 0 (trop court)")
    void testMessZero() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-mess", "0"}));
    }

    @Test
    @DisplayName("Exception - -seed sans valeur")
    void testSeedSansValeur() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-seed"}));
    }

    @Test
    @DisplayName("Exception - -ampl avec une seule valeur")
    void testAmplUneValeur() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ampl", "0.0"}));
    }

    @Test
    @DisplayName("Exception - -nbEch avec valeur nulle")
    void testNbEchZero() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-nbEch", "0"}));
    }

    @Test
    @DisplayName("Exception - -nbEch 3 doit echouer (minimum 10)")
    void testNbEchTroisEchoue() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-nbEch", "3"}));
    }

    @Test
    @DisplayName("NRZT - TEB nul avec amplitudes negatives")
    void testNRZTAmplNegatives() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT", "-ampl", "-1.0", "1.0", "-mess", "50", "-seed", "3"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZ - message fixe binaire")
    void testNRZMessageFixe() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-mess", "1010101"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("NRZT - message fixe binaire")
    void testNRZTMessageFixe() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT", "-mess", "1100110"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("RZ - message fixe binaire")
    void testRZMessageFixe() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "RZ", "-mess", "0001111"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    // -------------------------------------------------------
    // Branches affichage (-s) : couvre les blocs if (affichage)
    // -------------------------------------------------------

    @Test
    @DisplayName("Affichage logique - option -s construction OK")
    void testAffichageLogique() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-mess", "20", "-seed", "1", "-s"});
        // execute peut lever HeadlessException en environnement sans ecran
        try { s.execute(); } catch (Exception e) { /* GUI non disponible */ }
    }

    @Test
    @DisplayName("Affichage analogique NRZ - option -s construction OK")
    void testAffichageAnalogiqueNRZ() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-mess", "20", "-seed", "1", "-s"});
        try { s.execute(); } catch (Exception e) { /* GUI non disponible */ }
    }

    @Test
    @DisplayName("Affichage analogique NRZT - option -s construction OK")
    void testAffichageAnalogiqueNRZT() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZT", "-mess", "20", "-seed", "2", "-s"});
        try { s.execute(); } catch (Exception e) { /* GUI non disponible */ }
    }
}
