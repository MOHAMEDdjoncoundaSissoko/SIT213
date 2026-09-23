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

/**
 * Tests JUnit pour les classes de visualisation.
 * Mode headless active pour eviter l ouverture de fenetres GUI.
 */
public class VisualisationsTest {

    @BeforeAll
    static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    // -------------------------------------------------------
    // SondeTextuelle - pas de GUI, entierement testable
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // SondePuissance - calcul puissance testable (GUI catchee)
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // Vue - methodes statiques testables sans affichage
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // SondeLogique / SondeAnalogique - stockage info (GUI catchee)
    // -------------------------------------------------------

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
}
