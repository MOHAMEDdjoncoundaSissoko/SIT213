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
import visualisations.VueDecalage;
import transmetteurs.CodeurCanal;
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

    // VueDecalage (fenetre interactive de desynchronisation) : calculs et dessin sans fenetre

    @Test
    @DisplayName("VueDecalage - taux d'erreur rang par rang, bit absent compte comme erreur")
    void testVueDecalageTauxErreur() {
        Information<Boolean> reference = new Information<>(new Boolean[] {true, false, true, true});
        assertEquals(0.0f, VueDecalage.tauxErreur(reference, new Information<>(new Boolean[] {true, false, true, true})));
        assertEquals(0.25f, VueDecalage.tauxErreur(reference, new Information<>(new Boolean[] {true, true, true, true})));
        assertEquals(0.5f, VueDecalage.tauxErreur(reference, new Information<>(new Boolean[] {true, false})));
        assertEquals(0.0f, VueDecalage.tauxErreur(new Information<>(), reference));
    }

    @Test
    @DisplayName("VueDecalage - decider puis decoder : decalage fort avec codeur inverse le message")
    void testVueDecalageDeciderDecoder() throws Exception {
        Information<Boolean> message = new Information<>(new Boolean[] {true, false, false, true, true, false});
        CodeurCanal codeur = new CodeurCanal();
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", 30, -1f, 1f);
        codeur.connecter(emetteur);
        codeur.recevoir(message);

        Information<Boolean> sansDecalage = VueDecalage.decoder(
            VueDecalage.decider(emetteur.getInformationEmise(), "NRZ", 30, -1f, 1f, 0));
        Information<Boolean> decale = VueDecalage.decoder(
            VueDecalage.decider(emetteur.getInformationEmise(), "NRZ", 30, -1f, 1f, 16));

        assertEquals(message, sansDecalage);
        assertEquals(1.0f, VueDecalage.tauxErreur(message, decale));
    }

    @Test
    @DisplayName("VueDecalage - le trace marque en rouge les fenetres mal decidees")
    void testVueDecalageTrace() throws Exception {
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", 30, -1f, 1f);
        emetteur.recevoir(new Information<>(new Boolean[] {true, false, true, true, false, false, true}));
        Information<Float> signal = emetteur.getInformationEmise();
        Information<Boolean> bits = emetteur.getInformationRecue();

        assertFalse(contientFondRouge(signal, bits, 0));
        assertTrue(contientFondRouge(signal, bits, 16));
    }

    /** Dessine la vue dans une image et cherche la couleur de fond des fenetres mal decidees. */
    private static boolean contientFondRouge(Information<Float> signal, Information<Boolean> bits, int decalage)
        throws Exception {
        java.awt.image.BufferedImage image =
            new java.awt.image.BufferedImage(960, 330, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        VueDecalage.tracer(g, 960, 330, signal, signal, bits,
            VueDecalage.decider(signal, "NRZ", 30, -1f, 1f, decalage), 30, -1f, 1f, decalage);
        int fondRouge = new java.awt.Color(255, 210, 210).getRGB();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == fondRouge) return true;
            }
        }
        return false;
    }
}
