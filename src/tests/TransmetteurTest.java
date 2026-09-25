package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.TransmetteurParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurAnalogiqueLogique;
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
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique(10, 0f, 1f);
        Information<Float> signal = new Information<>();
        // 10 echantillons a 1.0 (bit 1)
        for (int i = 0; i < 10; i++) signal.add(1.0f);
        tal.recevoir(signal);
        assertTrue(tal.getInformationEmise().iemeElement(0));
    }

    @Test
    @DisplayName("TAL - decision correcte bit 0")
    void testTALDecisionBit0() throws Exception {
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique(10, 0f, 1f);
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
        TransmetteurAnalogiqueLogique tal = new TransmetteurAnalogiqueLogique(nbEch, 0f, 1f);
        tla.connecter(tal);

        Information<Boolean> entree = new Information<>();
        entree.add(true); entree.add(false); entree.add(true); entree.add(true); entree.add(false);
        tla.recevoir(entree);

        assertEquals(entree, tal.getInformationEmise());
    }
}
