package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import visualisations.SondeLogique;
import visualisations.SondeAnalogique;
import visualisations.SondeTextuelle;
import visualisations.SondePuissance;
import visualisations.Vue;
import visualisations.VueDesynchronisation;
import transmetteurs.TransmetteurEncodeurCanal;
import transmetteurs.TransmetteurLogiqueAnalogique;

/**
 * Tests JUnit pour les classes de visualisation.
 * Mode headless active pour eviter l ouverture de fenetres GUI.
 */
public class VisualisationsTest {

    @BeforeAll
    static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    // SondeTextuelle - pas de GUI, entierement testable

    @Test
    @DisplayName("SondeTextuelle Boolean - construction et reception")
    void testSondeTextuelleBoolean() throws Exception {
        SondeTextuelle<Boolean> sonde = new SondeTextuelle<>("TestBool");
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false); info.add(true);
        sonde.recevoir(info);
        assertEquals(info, sonde.getInformationRecue());
    }

    @Test
    @DisplayName("SondeTextuelle Float - construction et reception")
    void testSondeTextuelleFloat() throws Exception {
        SondeTextuelle<Float> sonde = new SondeTextuelle<>("TestFloat");
        Information<Float> info = new Information<>();
        info.add(0.5f); info.add(1.0f); info.add(-0.5f);
        sonde.recevoir(info);
        assertEquals(info, sonde.getInformationRecue());
    }

    @Test
    @DisplayName("SondeTextuelle - information vide")
    void testSondeTextuelleVide() throws Exception {
        SondeTextuelle<Boolean> sonde = new SondeTextuelle<>("Vide");
        Information<Boolean> info = new Information<>();
        sonde.recevoir(info);
        assertEquals(info, sonde.getInformationRecue());
    }

    // SondePuissance - calcul puissance testable (GUI catchee)

    @Test
    @DisplayName("SondePuissance - reception et stockage")
    void testSondePuissance() {
        SondePuissance sonde = new SondePuissance("Puissance");
        Information<Float> info = new Information<>();
        info.add(1.0f); info.add(1.0f); info.add(1.0f);
        try {
            sonde.recevoir(info);
        } catch (Exception e) {
            // VueValeur peut echouer en mode headless
        }
        assertEquals(info, sonde.getInformationRecue());
    }

    @Test
    @DisplayName("SondePuissance - signal nul")
    void testSondePuissanceNulle() {
        SondePuissance sonde = new SondePuissance("PuissanceNulle");
        Information<Float> info = new Information<>();
        info.add(0.0f); info.add(0.0f); info.add(0.0f);
        try {
            sonde.recevoir(info);
        } catch (Exception e) {
            // mode headless
        }
        assertEquals(info, sonde.getInformationRecue());
    }

    // Vue - methodes statiques testables sans affichage

    @Test
    @DisplayName("Vue - resetPosition remet yPosition a zero")
    void testVueResetPosition() {
        Vue.resetPosition();
        assertEquals(0, Vue.getYPosition());
    }

    @Test
    @DisplayName("Vue - setXPosition modifie xPosition")
    void testVueSetXPosition() {
        Vue.setXPosition(100);
        assertEquals(100, Vue.getXPosition());
        Vue.setXPosition(0); // remise a zero pour les autres tests
    }

    @Test
    @DisplayName("Vue - getYPosition increment a chaque appel")
    void testVueGetYPosition() {
        Vue.resetPosition();
        int y1 = Vue.getYPosition();
        int y2 = Vue.getYPosition();
        assertTrue(y2 > y1);
    }

    @Test
    @DisplayName("Vue - kill ne leve pas d exception")
    void testVueKill() {
        assertDoesNotThrow(() -> Vue.kill());
    }

    // SondeLogique / SondeAnalogique - stockage info (GUI catchee)

    @Test
    @DisplayName("SondeLogique - informationRecue stockee avant affichage")
    void testSondeLogiqueStockage() {
        SondeLogique sonde = new SondeLogique("LogiqueTest", 10);
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false); info.add(true);
        try {
            sonde.recevoir(info);
        } catch (Exception e) {
            // VueCourbe peut echouer en headless
        }
        assertEquals(info, sonde.getInformationRecue());
    }

    @Test
    @DisplayName("SondeAnalogique - informationRecue stockee avant affichage")
    void testSondeAnalogiqueStockage() {
        SondeAnalogique sonde = new SondeAnalogique("AnalogiqueTest");
        Information<Float> info = new Information<>();
        info.add(0.5f); info.add(1.0f); info.add(0.0f);
        try {
            sonde.recevoir(info);
        } catch (Exception e) {
            // VueCourbe peut echouer en headless
        }
        assertEquals(info, sonde.getInformationRecue());
    }

    @Test
    @DisplayName("VueDesynchronisation - TEB nul sans decalage")
    void testVueDesynchronisationTebNul() throws Exception {
        int nbEch = 10;
        Information<Boolean> reference = informationDepuisChaine("1010101");
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);

        emetteur.recevoir(reference);

        float teb = VueDesynchronisation.calculerTebAvecDecalage(
            emetteur.getInformationEmise(), reference, "NRZ", nbEch, -1f, 1f, false, 0);
        assertEquals(0.0f, teb, 0.0f);
    }

    @Test
    @DisplayName("VueDesynchronisation - decalage cree des erreurs")
    void testVueDesynchronisationTebDesynchronise() throws Exception {
        int nbEch = 10;
        Information<Boolean> reference = informationDepuisChaine("1010101");
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);

        emetteur.recevoir(reference);

        float teb = VueDesynchronisation.calculerTebAvecDecalage(
            emetteur.getInformationEmise(), reference, "NRZ", nbEch, -1f, 1f, false, 5);
        assertTrue(teb > 0.0f);
    }

    @Test
    @DisplayName("VueDesynchronisation - TEB nul avec codage canal")
    void testVueDesynchronisationTebCodeur() throws Exception {
        int nbEch = 10;
        Information<Boolean> reference = informationDepuisChaine("1011001");
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);

        encodeur.recevoir(reference);
        emetteur.recevoir(encodeur.getInformationEmise());

        float teb = VueDesynchronisation.calculerTebAvecDecalage(
            emetteur.getInformationEmise(), reference, "NRZ", nbEch, -1f, 1f, true, 0);
        assertEquals(0.0f, teb, 0.0f);
    }

    @Test
    @DisplayName("VueDesynchronisation - le decalage modifie les bits avant decodage canal")
    void testVueDesynchronisationBitsBrutsCodeur() throws Exception {
        int nbEch = 10;
        Information<Boolean> reference = informationDepuisChaine("1011001");
        TransmetteurEncodeurCanal encodeur = new TransmetteurEncodeurCanal();
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", nbEch, -1f, 1f);

        encodeur.recevoir(reference);
        emetteur.recevoir(encodeur.getInformationEmise());

        Information<Boolean> bitsBrutsDecales = VueDesynchronisation.recevoirBrutAvecDecalage(
            emetteur.getInformationEmise(), "NRZ", nbEch, -1f, 1f, 7);
        Information<Boolean> bitsDecodes = VueDesynchronisation.recevoirAvecDecalage(
            emetteur.getInformationEmise(), "NRZ", nbEch, -1f, 1f, true, 7);

        assertNotEquals(encodeur.getInformationEmise(), bitsBrutsDecales);
        assertEquals(reference.nbElements(), bitsDecodes.nbElements());
    }

    private Information<Boolean> informationDepuisChaine(String bits) {
        Information<Boolean> information = new Information<>();
        for (int i = 0; i < bits.length(); i++) {
            information.add(bits.charAt(i) == '1');
        }
        return information;
    }
}
