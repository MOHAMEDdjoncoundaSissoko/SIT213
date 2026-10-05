package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.TransmetteurParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurAnalogiqueLogique;
import transmetteurs.TransmetteurAnalogiqueBruite;
import transmetteurs.TransmetteurAnalogiqueTrajetsMultiples;
import transmetteurs.TransmetteurDecodeurCanal;
import transmetteurs.TransmetteurEncodeurCanal;
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

    // Codage de canal TP5

    @Test
    @DisplayName("Encodeur canal - applique 0 -> 010 et 1 -> 101")
    void testEncodeurCanal() throws Exception {
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();
        Information<Boolean> entree = new Information<>();
        entree.add(false); entree.add(true); entree.add(false);

        encodeur.recevoir(entree);

        Information<Boolean> attendu = new Information<>();
        attendu.add(false); attendu.add(true); attendu.add(false);
        attendu.add(true); attendu.add(false); attendu.add(true);
        attendu.add(false); attendu.add(true); attendu.add(false);
        assertEquals(attendu, encodeur.getInformationEmise());
    }

    @Test
    @DisplayName("Encodeur canal - information vide reste vide")
    void testEncodeurCanalVide() throws Exception {
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();

        encodeur.recevoir(new Information<>());

        assertEquals(0, encodeur.getInformationEmise().nbElements());
    }

    @Test
    @DisplayName("Encodeur canal - transmet l information codee a une destination")
    void testEncodeurCanalDestination() throws Exception {
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();
        DestinationFinale destination = new DestinationFinale();
        encodeur.connecter(destination);
        Information<Boolean> entree = new Information<>();
        entree.add(true);

        encodeur.recevoir(entree);

        Information<Boolean> attendu = new Information<>();
        attendu.add(true);
        attendu.add(false);
        attendu.add(true);
        assertEquals(attendu, destination.getInformationRecue());
    }

    @Test
    @DisplayName("Decodeur canal - corrige tous les triplets de la table TP5")
    void testDecodeurCanalTableComplete() throws Exception {
        TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();
        Information<Boolean> entree = new Information<>();
        boolean[][] triplets = {
            {false, false, false}, {false, false, true},
            {false, true, false}, {false, true, true},
            {true, false, false}, {true, false, true},
            {true, true, false}, {true, true, true}
        };
        for (boolean[] triplet : triplets) {
            entree.add(triplet[0]);
            entree.add(triplet[1]);
            entree.add(triplet[2]);
        }

        decodeur.recevoir(entree);

        Information<Boolean> attendu = new Information<>();
        attendu.add(false);
        attendu.add(true);
        attendu.add(false);
        attendu.add(false);
        attendu.add(true);
        attendu.add(true);
        attendu.add(false);
        attendu.add(true);
        assertEquals(attendu, decodeur.getInformationEmise());
    }

    @Test
    @DisplayName("Decodeur canal - information vide reste vide")
    void testDecodeurCanalVide() throws Exception {
        TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();

        decodeur.recevoir(new Information<>());

        assertEquals(0, decodeur.getInformationEmise().nbElements());
    }

    @Test
    @DisplayName("Decodeur canal - transmet l information decodee a une destination")
    void testDecodeurCanalDestination() throws Exception {
        TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();
        DestinationFinale destination = new DestinationFinale();
        decodeur.connecter(destination);
        Information<Boolean> entree = new Information<>();
        entree.add(false);
        entree.add(false);
        entree.add(true);

        decodeur.recevoir(entree);

        Information<Boolean> attendu = new Information<>();
        attendu.add(true);
        assertEquals(attendu, destination.getInformationRecue());
    }

    @Test
    @DisplayName("Decodeur canal - refuse un paquet incomplet")
    void testDecodeurCanalPaquetIncomplet() {
        TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();
        Information<Boolean> entree = new Information<>();
        entree.add(true);
        entree.add(false);

        assertThrows(Exception.class, () -> decodeur.recevoir(entree));
    }

    @Test
    @DisplayName("Codage canal - encodeur puis decodeur redonne le message initial")
    void testEncodeurPuisDecodeur() throws Exception {
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();
        TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();
        encodeur.connecter(decodeur);

        Information<Boolean> entree = new Information<>();
        entree.add(true); entree.add(false); entree.add(true); entree.add(true); entree.add(false);

        encodeur.recevoir(entree);

        assertEquals(entree, decodeur.getInformationEmise());
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
    @DisplayName("TAL - decalage nul par defaut et modification acceptee")
    void testTALDecalageFenetreAccesseur() {
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f);

        assertEquals(0, tal.getDecalageFenetre());
        tal.setDecalageFenetre(4);
        assertEquals(4, tal.getDecalageFenetre());
        tal.setDecalageFenetre(-4);
        assertEquals(-4, tal.getDecalageFenetre());
    }

    @Test
    @DisplayName("TAL - decalage de fenetre hors bornes refuse")
    void testTALDecalageFenetreInvalide() {
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f);

        assertThrows(IllegalArgumentException.class, () -> tal.setDecalageFenetre(10));
        assertThrows(IllegalArgumentException.class,
            () -> new TransmetteurAnalogiqueLogique("NRZ", 10, -1f, 1f, -10));
    }

    @Test
    @DisplayName("TAL - decalage positif simule une desynchronisation")
    void testTALDecalageFenetrePositif() throws Exception {
        int nbEch = 10;
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", nbEch, -1f, 1f, 5);
        tla.connecter(tal);

        Information<Boolean> entree = new Information<>();
        entree.add(true); entree.add(false); entree.add(true); entree.add(false);
        entree.add(true); entree.add(false); entree.add(true);

        tla.recevoir(entree);

        assertEquals(entree.nbElements(), tal.getInformationEmise().nbElements());
        assertNotEquals(entree, tal.getInformationEmise());
    }

    @Test
    @DisplayName("TAL - decalage negatif complete le debut de fenetre par du silence")
    void testTALDecalageFenetreNegatif() throws Exception {
        int nbEch = 10;
        TransmetteurLogiqueAnalogique tla = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique("NRZ", nbEch, -1f, 1f, -5);
        tla.connecter(tal);

        Information<Boolean> entree = new Information<>();
        entree.add(true); entree.add(false); entree.add(true); entree.add(false);
        entree.add(true); entree.add(false); entree.add(true);

        tla.recevoir(entree);

        assertEquals(entree.nbElements(), tal.getInformationEmise().nbElements());
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
}
