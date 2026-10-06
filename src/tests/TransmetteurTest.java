package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.CodeurCanal;
import transmetteurs.DecodeurCanal;
import transmetteurs.TransmetteurParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurAnalogiqueLogique;
import transmetteurs.TransmetteurAnalogiqueBruite;
import transmetteurs.TransmetteurAnalogiqueTrajetsMultiples;
import destinations.DestinationFinale;

/**
 * Tests JUnit pour les classes Transmetteur.
 */
public class TransmetteurTest {

    // Transmetteur (classe abstraite via TransmetteurParfait)

    @Test
    @DisplayName("getInformationRecue - retourne l information recue")
    void testGetInformationRecue() throws Exception {
        TransmetteurParfait t = new TransmetteurParfait();
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false);
        t.recevoir(info);
        assertEquals(info, t.getInformationRecue());
    }

    @Test
    @DisplayName("getInformationEmise - retourne l information emise")
    void testGetInformationEmise() throws Exception {
        TransmetteurParfait t = new TransmetteurParfait();
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false);
        t.recevoir(info);
        assertEquals(info, t.getInformationEmise());
    }

    @Test
    @DisplayName("getInformationRecue - null avant reception")
    void testGetInformationRecueNull() {
        TransmetteurParfait t = new TransmetteurParfait();
        assertNull(t.getInformationRecue());
    }

    @Test
    @DisplayName("getInformationEmise - null avant emission")
    void testGetInformationEmiseNull() {
        TransmetteurParfait t = new TransmetteurParfait();
        assertNull(t.getInformationEmise());
    }

    @Test
    @DisplayName("connecter et deconnecter une destination")
    void testConnecterDeconnecter() throws Exception {
        TransmetteurParfait t = new TransmetteurParfait();
        DestinationFinale d = new DestinationFinale();

        t.connecter(d);
        Information<Boolean> info = new Information<>();
        info.add(true);
        t.recevoir(info);
        assertEquals(info, d.getInformationRecue());

        // deconnexion : plus de transmission
        t.deconnecter(d);
        Information<Boolean> info2 = new Information<>();
        info2.add(false);
        t.recevoir(info2);
        // d n a pas recu info2 (toujours info)
        assertEquals(info, d.getInformationRecue());
    }

    @Test
    @DisplayName("connecter plusieurs destinations")
    void testConnecterPlusieursDestinations() throws Exception {
        TransmetteurParfait t = new TransmetteurParfait();
        DestinationFinale d1 = new DestinationFinale();
        DestinationFinale d2 = new DestinationFinale();
        t.connecter(d1);
        t.connecter(d2);

        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false);
        t.recevoir(info);

        assertEquals(info, d1.getInformationRecue());
        assertEquals(info, d2.getInformationRecue());
    }

    // TransmetteurLogiqueAnalogique

    @Test
    @DisplayName("TLA NRZ - taille signal = nbBits * nbEch")
    void testTLANRZTaille() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", 10, 0f, 1f);
        Information<Boolean> info = new Information<>();
        for (int i = 0; i < 5; i++) info.add(i % 2 == 0);
        tla.recevoir(info);
        assertEquals(50, tla.getInformationEmise().nbElements());
    }

    @Test
    @DisplayName("TLA NRZ - valeurs correctes bit 1 et bit 0")
    void testTLANRZValeurs() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", 10, 0f, 1f);
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false);
        tla.recevoir(info);
        // echantillons du bit 1 : valeur = aMax = 1.0
        assertEquals(1.0f, tla.getInformationEmise().iemeElement(5), 0.001f);
        // echantillons du bit 0 : valeur = aMin = 0.0
        assertEquals(0.0f, tla.getInformationEmise().iemeElement(15), 0.001f);
    }

    @Test
    @DisplayName("TLA NRZT - taille signal correcte")
    void testTLANRZTTaille() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZT", 30, 0f, 1f);
        Information<Boolean> info = new Information<>();
        for (int i = 0; i < 4; i++) info.add(true);
        tla.recevoir(info);
        assertEquals(120, tla.getInformationEmise().nbElements());
    }

    @Test
    @DisplayName("TLA NRZT - plateau au milieu du bit")
    void testTLANRZTPlateauMilieu() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZT", 30, 0f, 1f);
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(true); info.add(true);
        tla.recevoir(info);
        // milieu du deuxieme bit (index 30+15=45) doit valoir aMax
        assertEquals(1.0f, tla.getInformationEmise().iemeElement(45), 0.001f);
    }

    @Test
    @DisplayName("TLA NRZT - rampes montante et descendante entre les bits")
    void testTLANRZTRampesEntreBits() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZT", 10, -2f, 2f);
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false); info.add(true);

        tla.recevoir(info);

        Information<Float> emis = tla.getInformationEmise();
        assertEquals(-2.0f, emis.iemeElement(0), 0.001f);
        assertEquals(-2.0f / 3.0f, emis.iemeElement(1), 0.001f);
        assertEquals(2.0f / 3.0f, emis.iemeElement(2), 0.001f);
        assertEquals(2.0f, emis.iemeElement(10), 0.001f);
        assertEquals(2.0f / 3.0f, emis.iemeElement(11), 0.001f);
        assertEquals(-2.0f / 3.0f, emis.iemeElement(12), 0.001f);
        assertEquals(-2.0f, emis.iemeElement(13), 0.001f);
        assertEquals(-2.0f, emis.iemeElement(20), 0.001f);
        assertEquals(2.0f, emis.iemeElement(23), 0.001f);
    }

    @Test
    @DisplayName("TLA RZ - impulsion au tiers central seulement")
    void testTLARZImpulsion() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("RZ", 30, 0f, 1f);
        Information<Boolean> info = new Information<>();
        info.add(true);
        tla.recevoir(info);
        // premier tiers : aMin
        assertEquals(0.0f, tla.getInformationEmise().iemeElement(0), 0.001f);
        // tiers central : aMax
        assertEquals(1.0f, tla.getInformationEmise().iemeElement(15), 0.001f);
        // dernier tiers : aMin
        assertEquals(0.0f, tla.getInformationEmise().iemeElement(25), 0.001f);
    }

    @Test
    @DisplayName("TLA RZ bit 0 - reste a aMin partout")
    void testTLARZBitZero() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("RZ", 30, 0f, 1f);
        Information<Boolean> info = new Information<>();
        info.add(false);
        tla.recevoir(info);
        for (int i = 0; i < 30; i++) {
            assertEquals(0.0f, tla.getInformationEmise().iemeElement(i), 0.001f);
        }
    }

    @Test
    @DisplayName("TLA - nbEch trop petit (3) doit echouer a la construction")
    void testTLANbEchTropPetitEchoue() {
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurLogiqueAnalogique("RZ", 3, 0f, 1f));
    }

    @Test
    @DisplayName("TLA - forme inconnue utilise le niveau du bit")
    void testTLAFormeInconnue() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("AUTRE", 10, -1f, 2f);
        Information<Boolean> info = new Information<>();
        info.add(false); info.add(true);

        tla.recevoir(info);

        assertEquals(-1.0f, tla.getInformationEmise().iemeElement(0), 0.001f);
        assertEquals(2.0f, tla.getInformationEmise().iemeElement(10), 0.001f);
    }

    @Test
    @DisplayName("TLA - signal vide transmis a une destination")
    void testTLASignalVide() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", 10, 0f, 1f);
        TransmetteurAnalogiqueLogique destination = new TransmetteurAnalogiqueLogique("NRZ", 10, 0f, 1f);
        tla.connecter(destination);

        tla.recevoir(new Information<>());

        assertEquals(0, tla.getInformationEmise().nbElements());
        assertSame(tla.getInformationEmise(), destination.getInformationRecue());
    }

    // TransmetteurAnalogiqueParfait

    @Test
    @DisplayName("TAP - signal transmis sans modification")
    void testTAPSansModification() throws Exception {
        TransmetteurAnalogiqueParfait tap = new TransmetteurAnalogiqueParfait(10, 0f, 1f);
        Information<Float> signal = new Information<>();
        signal.add(0.5f); signal.add(1.0f); signal.add(0.0f);
        tap.recevoir(signal);
        assertEquals(signal, tap.getInformationEmise());
    }

    // TransmetteurAnalogiqueLogique

    @Test
    @DisplayName("TAL - decision correcte bit 1")
    void testTALDecisionBit1() throws Exception {
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, 0f, 1f);
        Information<Float> signal = new Information<>();
        // 10 echantillons a 1.0 (bit 1)
        for (int i = 0; i < 10; i++) signal.add(1.0f);
        tal.recevoir(signal);
        assertTrue(tal.getInformationEmise().iemeElement(0));
    }

    @Test
    @DisplayName("TAL - decision correcte bit 0")
    void testTALDecisionBit0() throws Exception {
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, 0f, 1f);
        Information<Float> signal = new Information<>();
        for (int i = 0; i < 10; i++) signal.add(0.0f);
        tal.recevoir(signal);
        assertFalse(tal.getInformationEmise().iemeElement(0));
    }

    @Test
    @DisplayName("TAL - sequence complete correcte")
    void testTALSequence() throws Exception {
        int nbEch = 10;
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", nbEch, 0f, 1f);
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", nbEch, 0f, 1f);
        tla.connecter(tal);

        Information<Boolean> entree = new Information<>();
        entree.add(true); entree.add(false); entree.add(true); entree.add(true); entree.add(false);
        tla.recevoir(entree);

        assertEquals(entree, tal.getInformationEmise());
    }

    @Test
    @DisplayName("TAL - filtre adapte RZ : ignore les tiers hors impulsion")
    void testTALFiltreAdapteRZ() throws Exception {
        // bit 0 en RZ, mais tres perturbe hors du tiers central : sans effet sur la decision
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("RZ", 30, 0f, 1f);
        Information<Float> signal = new Information<>();
        for (int k = 0; k < 30; k++) signal.add((k >= 10 && k < 20) ? 0.0f : 5.0f);

        tal.recevoir(signal);

        assertFalse(tal.getInformationEmise().iemeElement(0));
    }

    @Test
    @DisplayName("TAL - filtre adapte : decision sur tout le bit, pas sur un echantillon")
    void testTALFiltreAdapteMoyenne() throws Exception {
        // bit 1 en NRZ dont l'echantillon du milieu est tres bas : la corrélation reste positive
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f);
        Information<Float> signal = new Information<>();
        for (int k = 0; k < 10; k++) signal.add(k == 5 ? -3.0f : 1.0f);

        tal.recevoir(signal);

        assertTrue(tal.getInformationEmise().iemeElement(0));
    }

    @Test
    @DisplayName("TAL - NRZT : sequence correcte quelle que soit la rampe du bit precedent")
    void testTALSequenceNRZT() throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZT", 30, -1f, 1f);
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZT", 30, -1f, 1f);
        tla.connecter(tal);
        Information<Boolean> entree = new Information<>();
        for (boolean b : new boolean[] {true, true, false, false, true, false, true, true}) entree.add(b);

        tla.recevoir(entree);

        assertEquals(entree, tal.getInformationEmise());
    }

    // TransmetteurAnalogiqueLogique desynchronise (fenetre de decision decalee)

    /** Bits decides par un recepteur NRZ (-1, 1) a 10 echantillons par bit, decale de d. */
    private static Information<Boolean> decideAvecDecalage(String message, int d) throws Exception {
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", 10, -1f, 1f);
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f, d);
        tla.connecter(tal);
        tla.recevoir(bits(message));
        return tal.getInformationEmise();
    }

    @Test
    @DisplayName("TAL decale - moins d'un demi-bit : decisions inchangees")
    void testTALDecalageFaible() throws Exception {
        // fenetre a cheval : 6 echantillons du bit courant, 4 du voisin -> le bit courant l'emporte
        assertEquals(bits("1011001"), decideAvecDecalage("1011001", 4));
        assertEquals(bits("1011001"), decideAvecDecalage("1011001", -4));
    }

    @Test
    @DisplayName("TAL decale - retard de plus d'un demi-bit : le bit suivant est lu")
    void testTALDecalagePositifFort() throws Exception {
        // d = 6 : 4 echantillons du bit i, 6 du bit i+1 -> on decide b(i+1) ;
        // le dernier bit (4 echantillons recus, 6 hors du signal) reste juste
        assertEquals(bits("0110011"), decideAvecDecalage("1011001", 6));
    }

    @Test
    @DisplayName("TAL decale - avance de plus d'un demi-bit : le bit precedent est lu")
    void testTALDecalageNegatifFort() throws Exception {
        // d = -6 : 6 echantillons du bit i-1, 4 du bit i -> on decide b(i-1) ;
        // le premier bit (6 echantillons hors du signal, 4 recus) reste juste
        assertEquals(bits("1101100"), decideAvecDecalage("1011001", -6));
    }

    @Test
    @DisplayName("TAL decale - decalage d'au moins un bit refuse, accesseur")
    void testTALDecalageLimites() {
        assertEquals(0, new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f).getDecalage());
        assertEquals(-9, new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f, -9).getDecalage());
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f, 10));
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f, -10));
    }

    // TransmetteurAnalogiqueBruite

    @Test
    @DisplayName("TAB - bruit reproductible avec une seed et signal transmis")
    void testTABruitReproductible() throws Exception {
        TransmetteurAnalogiqueBruite premier = new TransmetteurAnalogiqueBruite(10, 0f, 42);
        TransmetteurAnalogiqueBruite second = new TransmetteurAnalogiqueBruite(10, 0f, 42);
        TransmetteurAnalogiqueLogique destination = new TransmetteurAnalogiqueLogique("NRZ", 1, 0f, 1f);
        premier.connecter(destination);

        Information<Float> signal = new Information<>();
        signal.add(1.0f); signal.add(-1.0f); signal.add(0.5f); signal.add(0.0f);
        premier.recevoir(signal);
        second.recevoir(signal);

        assertSame(signal, premier.getInformationRecue());
        assertEquals(signal.nbElements(), premier.getInformationEmise().nbElements());
        assertNotEquals(signal, premier.getInformationEmise());
        assertEquals(premier.getInformationEmise(), second.getInformationEmise());
        assertSame(premier.getInformationEmise(), destination.getInformationRecue());
    }

    @Test
    @DisplayName("TAB - signal nul transmis sans bruit")
    void testTABruitSignalNul() throws Exception {
        TransmetteurAnalogiqueBruite transmetteur = new TransmetteurAnalogiqueBruite(10, 0f, 42);
        Information<Float> signal = new Information<>();
        signal.add(0.0f); signal.add(0.0f);

        transmetteur.recevoir(signal);

        assertEquals(signal, transmetteur.getInformationEmise());
    }

    @Test
    @DisplayName("TAB - signal vide transmis sans erreur")
    void testTABruitSignalVide() throws Exception {
        TransmetteurAnalogiqueBruite transmetteur = new TransmetteurAnalogiqueBruite(10, 0f, 42);
        TransmetteurAnalogiqueLogique destination = new TransmetteurAnalogiqueLogique("NRZ", 1, 0f, 1f);
        transmetteur.connecter(destination);
        Information<Float> signal = new Information<>();

        transmetteur.recevoir(signal);

        assertEquals(0, transmetteur.getInformationEmise().nbElements());
        assertSame(transmetteur.getInformationEmise(), destination.getInformationRecue());
    }

    @Test
    @DisplayName("TAB - construction sans seed et signal de taille conservee")
    void testTABruitSansSeed() throws Exception {
        TransmetteurAnalogiqueBruite transmetteur = new TransmetteurAnalogiqueBruite(10, 0f, null);
        Information<Float> signal = new Information<>();
        signal.add(1.0f); signal.add(0.0f);

        transmetteur.recevoir(signal);

        assertEquals(signal.nbElements(), transmetteur.getInformationEmise().nbElements());
        for (Float echantillon : transmetteur.getInformationEmise()) {
            assertTrue(Float.isFinite(echantillon));
        }
    }

    // TransmetteurAnalogiqueTrajetsMultiples

    @Test
    @DisplayName("TATM - echo retarde applique apres le debut du signal")
    void testTATMEchoRetarde() throws Exception {
        TransmetteurAnalogiqueTrajetsMultiples transmetteur =
            new TransmetteurAnalogiqueTrajetsMultiples(10, Float.POSITIVE_INFINITY,
                new int[] {2}, new float[] {0.5f}, 42);
        TransmetteurAnalogiqueLogique destination = new TransmetteurAnalogiqueLogique("NRZ", 1, 0f, 1f);
        transmetteur.connecter(destination);
        Information<Float> signal = new Information<>();
        signal.add(1.0f); signal.add(2.0f); signal.add(3.0f); signal.add(4.0f);

        transmetteur.recevoir(signal);

        Information<Float> attendu = new Information<>();
        attendu.add(1.0f); attendu.add(2.0f); attendu.add(3.5f); attendu.add(5.0f);
        assertEquals(attendu, transmetteur.getInformationEmise());
        assertSame(transmetteur.getInformationEmise(), destination.getInformationRecue());
    }

    @Test
    @DisplayName("TATM - bruit reproductible avec une seed")
    void testTATMBruitReproductible() throws Exception {
        TransmetteurAnalogiqueTrajetsMultiples premier =
            new TransmetteurAnalogiqueTrajetsMultiples(10, 0f, new int[0], new float[0], 42);
        TransmetteurAnalogiqueTrajetsMultiples second =
            new TransmetteurAnalogiqueTrajetsMultiples(10, 0f, new int[0], new float[0], 42);
        Information<Float> signal = new Information<>();
        signal.add(1.0f); signal.add(0.0f); signal.add(0.5f);

        premier.recevoir(signal);
        second.recevoir(signal);

        assertEquals(premier.getInformationEmise(), second.getInformationEmise());
        assertEquals(signal.nbElements(), premier.getInformationEmise().nbElements());
        assertNotEquals(signal, premier.getInformationEmise());
    }

    @Test
    @DisplayName("TATM - seed absente et signal vide transmis")
    void testTATMSansSeedSignalVide() throws Exception {
        TransmetteurAnalogiqueTrajetsMultiples transmetteur =
            new TransmetteurAnalogiqueTrajetsMultiples(10, 0f, new int[0], new float[0], null);
        TransmetteurAnalogiqueLogique destination = new TransmetteurAnalogiqueLogique("NRZ", 1, 0f, 1f);
        transmetteur.connecter(destination);
        Information<Float> signal = new Information<>();

        transmetteur.recevoir(signal);

        assertEquals(0, transmetteur.getInformationEmise().nbElements());
        assertSame(transmetteur.getInformationEmise(), destination.getInformationRecue());
    }

    @Test
    @DisplayName("TATM - bruit identique au canal gaussien sans trajet indirect")
    void testTATMMemeBruitQueCanalGaussien() throws Exception {
        TransmetteurAnalogiqueBruite bruite = new TransmetteurAnalogiqueBruite(10, 3f, 42);
        TransmetteurAnalogiqueTrajetsMultiples trajets =
            new TransmetteurAnalogiqueTrajetsMultiples(10, 3f, new int[0], new float[0], 42);
        Information<Float> signal = new Information<>();
        signal.add(1.0f); signal.add(-1.0f); signal.add(0.5f); signal.add(1.0f);

        bruite.recevoir(signal);
        trajets.recevoir(signal);

        assertEquals(bruite.getInformationEmise(), trajets.getInformationEmise());
    }

    @Test
    @DisplayName("TATM - rejette des tableaux de trajets invalides")
    void testTATMTrajetsInvalides() {
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueTrajetsMultiples(10, 0f,
                new int[0], new float[] {0.5f}, 42));
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueTrajetsMultiples(10, 0f,
                new int[TransmetteurAnalogiqueTrajetsMultiples.NB_TRAJETS_MAX + 1],
                new float[TransmetteurAnalogiqueTrajetsMultiples.NB_TRAJETS_MAX + 1], 42));
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueTrajetsMultiples(10, 0f,
                new int[] {-1}, new float[] {0.5f}, 42));
    }

    // CodeurCanal et DecodeurCanal

    /** Construit une Information logique à partir d'une suite de 0 et de 1. */
    private static Information<Boolean> bits(String suite) {
        Information<Boolean> info = new Information<>();
        for (char c : suite.toCharArray()) info.add(c == '1');
        return info;
    }

    @Test
    @DisplayName("Codeur - 0 donne 010 et 1 donne 101")
    void testCodeurMotsDeCode() throws Exception {
        CodeurCanal codeur = new CodeurCanal();

        codeur.recevoir(bits("01"));

        assertEquals(bits("010101"), codeur.getInformationEmise());
    }

    @Test
    @DisplayName("Codeur - jamais plus de deux bits consecutifs identiques")
    void testCodeurPasDeLonguesSuites() throws Exception {
        CodeurCanal codeur = new CodeurCanal();

        // toutes les transitions possibles entre bits, et de longues suites de 0 et de 1
        codeur.recevoir(bits("0000011111001101"));

        Information<Boolean> code = codeur.getInformationEmise();
        assertEquals(3 * 16, code.nbElements());
        for (int i = 2; i < code.nbElements(); i++) {
            boolean troisIdentiques = code.iemeElement(i).equals(code.iemeElement(i - 1))
                && code.iemeElement(i).equals(code.iemeElement(i - 2));
            assertFalse(troisIdentiques, "trois bits identiques a partir de l'indice " + (i - 2));
        }
    }

    @Test
    @DisplayName("Decodeur - table de decodage des 8 paquets du TP5")
    void testDecodeurTable() throws Exception {
        DecodeurCanal decodeur = new DecodeurCanal();

        decodeur.recevoir(bits("000" + "001" + "010" + "011" + "100" + "101" + "110" + "111"));

        assertEquals(bits("01001101"), decodeur.getInformationEmise());
    }

    @Test
    @DisplayName("Codeur + decodeur - une erreur par paquet est corrigee")
    void testCodageCorrigeUneErreur() throws Exception {
        Information<Boolean> message = bits("0110");
        CodeurCanal codeur = new CodeurCanal();
        codeur.recevoir(message);

        // une erreur sur le 1er, le 2e, le 3e bit des trois premiers paquets ; le dernier est intact
        Information<Boolean> recu = new Information<>();
        for (int i = 0; i < codeur.getInformationEmise().nbElements(); i++) {
            boolean bit = codeur.getInformationEmise().iemeElement(i);
            recu.add((i == 0 || i == 4 || i == 8) ? !bit : bit);
        }
        DecodeurCanal decodeur = new DecodeurCanal();
        decodeur.recevoir(recu);

        assertEquals(message, decodeur.getInformationEmise());
    }

    @Test
    @DisplayName("Codeur + decodeur - deux erreurs dans un paquet ne sont pas corrigees")
    void testCodageDeuxErreurs() throws Exception {
        DecodeurCanal decodeur = new DecodeurCanal();

        // 010 (bit 0) avec deux erreurs devient 100 : le mot le plus proche est 101
        decodeur.recevoir(bits("100"));

        assertEquals(bits("1"), decodeur.getInformationEmise());
    }

    @Test
    @DisplayName("Codeur + decodeur - chaines, message vide et paquet incomplet")
    void testCodageChaineEtCasLimites() throws Exception {
        CodeurCanal codeur = new CodeurCanal();
        DecodeurCanal decodeur = new DecodeurCanal();
        DestinationFinale destination = new DestinationFinale();
        codeur.connecter(decodeur);
        decodeur.connecter(destination);

        codeur.recevoir(bits("1011001"));
        assertEquals(bits("1011001"), destination.getInformationRecue());

        codeur.recevoir(new Information<>());
        assertEquals(0, destination.getInformationRecue().nbElements());

        // les deux bits en trop d'un paquet incomplet sont ignores
        decodeur.recevoir(bits("10101"));
        assertEquals(bits("1"), decodeur.getInformationEmise());
    }
}
