// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package simulateur;

import destinations.Destination;
import destinations.DestinationFinale;
import information.Information;
import sources.Source;
import sources.SourceAleatoire;
import sources.SourceFixe;
import transmetteurs.Transmetteur;
import transmetteurs.TransmetteurAnalogiqueLogique;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import transmetteurs.TransmetteurParfait;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;

public class Simulateur {
   private boolean affichage = false;
   private boolean messageAleatoire = true;
   private boolean aleatoireAvecGerme = false;
   private Integer seed = null;
   private int nbBitsMess = 100;
   private String messageString = "100";
   private boolean simulationAnalogique = false;
   private String forme = "RZ";
   private int nbEch = 30;
   private float aMin = 0.0f;
   private float aMax = 1.0f;
   private Source<Boolean> source = null;
   private Transmetteur<Boolean, Boolean> transmetteurLogique = null;
   private Destination<Boolean> destination = null;

   public Simulateur(String[] var1) throws ArgumentsException {
        this.analyseArguments(var1);
        if (this.messageAleatoire) {
            if (this.aleatoireAvecGerme) {
                this.source = new SourceAleatoire(this.nbBitsMess, this.seed);
            } else {
                this.source = new SourceAleatoire(this.nbBitsMess);
            }
        } else {
            this.source = new SourceFixe(this.messageString);
        }


      // Création commune de la destination
      this.destination = new DestinationFinale();

      if (!this.simulationAnalogique) {
            // --- CE QUI EXISTE DÉJÀ ---
            this.transmetteurLogique = new TransmetteurParfait();
            this.source.connecter(this.transmetteurLogique);
            this.transmetteurLogique.connecter(this.destination);
            
            if (this.affichage) {
               this.source.connecter(new SondeLogique("Emetteur", 10));
               this.transmetteurLogique.connecter(new SondeLogique("Recepteur", 10));
            }
        } else {
            // --- NOUVEAU : CAS ANALOGIQUE ---
            TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique(this.forme, this.nbEch, this.aMin, this.aMax);
            TransmetteurAnalogiqueParfait canal = new TransmetteurAnalogiqueParfait(nbEch, aMin, aMax);
            TransmetteurAnalogiqueLogique recepteur = new TransmetteurAnalogiqueLogique(this.nbEch, this.aMin, this.aMax);

            this.source.connecter(emetteur);
            emetteur.connecter(canal);
            canal.connecter(recepteur);
            recepteur.connecter(this.destination);

            if (this.affichage) {
               emetteur.connecter(new SondeAnalogique("Signal emis"));
               canal.connecter(new SondeAnalogique("Signal recu"));
            }
        }
   }

   private void analyseArguments(String[] var1) throws ArgumentsException {
      for(int var2 = 0; var2 < var1.length; ++var2) {
         if (var1[var2].matches("-s")) {
            this.affichage = true;
         } else if (var1[var2].matches("-seed")) {
            this.aleatoireAvecGerme = true;
            ++var2;

            try {
               this.seed = Integer.valueOf(var1[var2]);
            } catch (Exception var4) {
               throw new ArgumentsException("Valeur du parametre -seed  invalide :" + var1[var2]);
            }
         } else if (var1[var2].matches("-mess")) {
            ++var2;
            this.messageString = var1[var2];
            if (var1[var2].matches("[0,1]{7,}")) {
               this.messageAleatoire = false;
               this.nbBitsMess = var1[var2].length();
            } else {
               if (!var1[var2].matches("[0-9]{1,6}")) {
                  throw new ArgumentsException("Valeur du parametre -mess invalide : " + var1[var2]);
               }

               this.messageAleatoire = true;
               this.nbBitsMess = Integer.valueOf(var1[var2]);
               if (this.nbBitsMess < 1) {
                  throw new ArgumentsException("Valeur du parametre -mess invalide : " + this.nbBitsMess);
               }
            }
         } else if (var1[var2].matches("-form")) {
            this.simulationAnalogique = true;
            ++var2;
            this.forme = var1[var2];
         } else if (var1[var2].matches("-nbEch")) {
            this.simulationAnalogique = true;
            ++var2;
            try {
               this.nbEch = Integer.parseInt(var1[var2]);
               
               // AJOUTER CECI :
               if (this.nbEch < 1) {
                   throw new ArgumentsException("Valeur du parametre -nbEch invalide : " + this.nbEch + " (doit être > 0)");
               }
               
            } catch (Exception var4) {
               throw new ArgumentsException("Valeur du parametre -nbEch invalide : " + var1[var2]);
            }
         } else if (var1[var2].matches("-ampl")) {
            this.simulationAnalogique = true;
            try {
               ++var2;
               this.aMin = Float.parseFloat(var1[var2]);
               ++var2;
               this.aMax = Float.parseFloat(var1[var2]);
            } catch (Exception var4) {
               throw new ArgumentsException("Valeurs du parametre -ampl invalides");
            }
         } else {
            // Si l'option n'est rien de tout ça, c'est une erreur
            throw new ArgumentsException("Option invalide :" + var1[var2]);
         }
      }
   }

   public void execute() throws Exception {
      this.source.emettre();
   }

   public float calculTauxErreurBinaire() {
      Information var1 = this.source.getInformationEmise();
      Information var2 = this.destination.getInformationRecue();
      int var3 = var1.nbElements();
      int var4 = 0;

      for(int var5 = 0; var5 < var3; ++var5) {
         if (!((Boolean)var1.iemeElement(var5)).equals(var2.iemeElement(var5))) {
            ++var4;
         }
      }

      return (float)var4 / (float)var3;
   }

   public static void main(String[] var0) {
      Simulateur var1 = null;

      try {
         var1 = new Simulateur(var0);
      } catch (Exception var4) {
         System.out.println(var4);
         System.exit(1);
      }

      try {
         var1.execute();
         String var2 = "java  Simulateur  ";

         for(int var3 = 0; var3 < var0.length; ++var3) {
            var2 = var2 + var0[var3] + "  ";
         }

         System.out.println(var2 + "  =>   TEB : " + var1.calculTauxErreurBinaire());
      } catch (Exception var5) {
         System.out.println(var5);
         var5.printStackTrace();
         System.exit(-2);
      }

   }
}
