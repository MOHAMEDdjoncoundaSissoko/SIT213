package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import simulateur.Simulateur;

/**
 * Tests JUnit pour le Simulateur SIT213.
 * Couvre les etapes 1 a 3 (chaine logique, analogique, canal bruite).
 */
public class SimulateurTest {

    @BeforeAll
    static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    // Etape 1 : chaine logique

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

    // Etape 2 : chaine analogique NRZ

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

    // Etape 2 : chaine analogique NRZT

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

    // Etape 2 : chaine analogique RZ

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

    // Cas d'erreur des arguments

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

    // Affichage (-s)

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

    // Etape 3 : conformite commande unique

    @Test
    @DisplayName("Exception - -ampl avec min > max")
    void testAmplMinSupMax() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ampl", "1.0", "0.0"}));
    }

    @Test
    @DisplayName("Exception - -ampl avec min = max")
    void testAmplMinEgalMax() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ampl", "1.0", "1.0"}));
    }

    @Test
    @DisplayName("-snrpb accepte et TEB non nul a faible Eb/N0")
    void testSnrpbFaible() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-ampl", "-1", "1", "-mess", "10000", "-snrpb", "0", "-seed", "1"});
        s.execute();
        assertTrue(s.calculTauxErreurBinaire() > 0.0f);
    }

    @Test
    @DisplayName("-snrpb : TEB nul a tres fort Eb/N0")
    void testSnrpbFort() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-ampl", "-1", "1", "-mess", "1000", "-snrpb", "40", "-seed", "1"});
        s.execute();
        assertEquals(0.0f, s.calculTauxErreurBinaire(), 0.0f);
    }

    @Test
    @DisplayName("-snrpb : TEB conforme a la theorie grace au filtre adapte (NRZ antipodal)")
    void testSnrpbTheorie() throws Exception {
        // theorie : TEB = Q(sqrt(2 Eb/N0)) = 1.25e-2 a 4 dB ; ~1250 erreurs attendues sur 1e5 bits
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-ampl", "-1", "1",
            "-mess", "100000", "-snrpb", "4", "-seed", "1"});
        s.execute();
        assertEquals(1.25e-2, s.calculTauxErreurBinaire(), 0.15e-2);
    }

    @Test
    @DisplayName("Exception - -snrpb non flottant")
    void testSnrpbInvalide() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-snrpb", "abc"}));
    }

    @Test
    @DisplayName("-ti active le canal avec echos")
    void testTrajetsActifs() throws Exception {
        // deux echos forts (0.9) retardes d'un et deux bits font franchir le seuil
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-nbEch", "10",
            "-ampl", "-1", "1", "-mess", "10000", "-ti", "10", "0.9", "20", "0.9", "-seed", "42"});

        s.execute();

        assertTrue(s.calculTauxErreurBinaire() > 0.0f);
    }

    @Test
    @DisplayName("-ti sans -snrpb n'ajoute pas de bruit")
    void testTrajetsSansBruit() throws Exception {
        // un echo de 0.5 retarde d'un bit ne fait jamais franchir le seuil : TEB nul sans bruit
        Simulateur s = new Simulateur(new String[]{"-form", "NRZ", "-nbEch", "10",
            "-ampl", "-1", "1", "-mess", "10000", "-ti", "10", "0.5", "-seed", "42"});

        s.execute();

        assertEquals(0.0f, s.calculTauxErreurBinaire());
    }

    @Test
    @DisplayName("-ti avec un echo nul donne le meme TEB que -snrpb seul")
    void testTrajetsEchoNulMemeBruit() throws Exception {
        // meme puissance de bruit que le canal gaussien : la formule Eb/N0 est partagee
        Simulateur bruite = new Simulateur(new String[]{"-form", "NRZ", "-mess", "10000",
            "-snrpb", "0", "-seed", "1"});
        Simulateur trajets = new Simulateur(new String[]{"-form", "NRZ", "-mess", "10000",
            "-snrpb", "0", "-ti", "5", "0", "-seed", "1"});

        bruite.execute();
        trajets.execute();

        assertEquals(bruite.calculTauxErreurBinaire(), trajets.calculTauxErreurBinaire());
    }

    @Test
    @DisplayName("-ti suivi d'autres options et sans seed")
    void testTrajetsSansSeed() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-ti", "30", "0.5", "10", "0.2",
            "-form", "NRZ", "-mess", "100"});

        s.execute();

        assertTrue(Float.isFinite(s.calculTauxErreurBinaire()));
    }

    @Test
    @DisplayName("Exception - nombre de couples -ti hors limites")
    void testNombreTrajetsInvalide() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ti"}));
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ti", "-mess", "20"}));
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-ti",
            "1", "0.1", "2", "0.1", "3", "0.1", "4", "0.1", "5", "0.1", "6", "0.1"}));
    }

    @Test
    @DisplayName("Exception - valeurs -ti mal formees ou manquantes")
    void testValeursTrajetsInvalides() {
        assertThrows(Exception.class,
            () -> new Simulateur(new String[]{"-ti", "10", "alpha"}));
        assertThrows(Exception.class,
            () -> new Simulateur(new String[]{"-ti", "10"}));
        assertThrows(Exception.class,
            () -> new Simulateur(new String[]{"-ti", "-3", "0.5"}));
        assertThrows(Exception.class,
            () -> new Simulateur(new String[]{"-ti", "99999999999", "0.5"}));
    }

    // Codage de canal (-codeur)

    @Test
    @DisplayName("-codeur : TEB nul en logique et sans bruit pour les trois formes d'onde")
    void testCodeurSansBruit() throws Exception {
        String[][] cas = {
            {"-codeur", "-mess", "1011001"},
            {"-codeur", "-mess", "500", "-form", "NRZ"},
            {"-codeur", "-mess", "500", "-form", "NRZT", "-ampl", "-1", "1"},
            {"-codeur", "-mess", "500", "-form", "RZ"},
        };
        for (String[] args : cas) {
            Simulateur s = new Simulateur(args);
            s.execute();
            assertEquals(0.0f, s.calculTauxErreurBinaire(), String.join(" ", args));
        }
    }

    @Test
    @DisplayName("-codeur : TEB reduit et conforme a la theorie 3p^2 - 2p^3")
    void testCodeurReduitLeTEB() throws Exception {
        // NRZ antipodal a 2 dB : p = Q(sqrt(2 Eb/N0)) = 3.75e-2 par bit code,
        // soit 3p^2 - 2p^3 = 4.1e-3 apres decodage (~250 erreurs sur 60 000 bits)
        String[] base = {"-form", "NRZ", "-nbEch", "10", "-ampl", "-1", "1",
            "-mess", "60000", "-snrpb", "2", "-seed", "1"};
        String[] avecCodeur = java.util.Arrays.copyOf(base, base.length + 1);
        avecCodeur[base.length] = "-codeur";
        Simulateur sans = new Simulateur(base);
        Simulateur avec = new Simulateur(avecCodeur);

        sans.execute();
        avec.execute();

        assertEquals(3.75e-2, sans.calculTauxErreurBinaire(), 0.3e-2);
        assertEquals(4.1e-3, avec.calculTauxErreurBinaire(), 1.0e-3);
    }

    @Test
    @DisplayName("-codeur combine avec -ti et -snrpb")
    void testCodeurAvecTrajets() throws Exception {
        Simulateur s = new Simulateur(new String[]{"-codeur", "-form", "NRZ", "-mess", "300",
            "-ti", "15", "0.5", "-snrpb", "6", "-seed", "1"});

        s.execute();

        assertTrue(Float.isFinite(s.calculTauxErreurBinaire()));
    }

    @Test
    @DisplayName("Exception - -codeur ne prend pas de parametre")
    void testCodeurAvecParametre() {
        assertThrows(Exception.class, () -> new Simulateur(new String[]{"-codeur", "3"}));
    }
}
