# Simulateur de Chaîne de Transmission Numérique - SIT213

Ce projet est un simulateur en Java permettant de modéliser, simuler et analyser les performances d'une chaîne de transmission numérique complète. Il permet d'étudier l'impact de différents codages en ligne, de l'ajout d'un canal bruité (Bruit Blanc Gaussien Additif - AWGN), et des modulations sur la qualité du signal transmis, mesurée par le Taux d'Erreur Binaire (TEB).

## Fonctionnalités principales

* **Source d'information** : Génération de messages binaires (séquences aléatoires de $N$ bits ou fixées par une suite de 0 et de 1).
* **Codage en ligne (Bande de base)** : Transformation du flux binaire en un signal analogique.
  * `NRZ` (Non-Return to Zero)
  * `NRZT` (Non-Return to Zero Trapezoidal)
  * `RZ` (Return to Zero)
* **Canal de transmission** : 
  * Canal idéal (sans déformation).
  * Canal avec bruit blanc gaussien additif paramétré par le rapport signal sur bruit $E_b/N_0$.
* **Réception et Décision** : Échantillonnage du signal reçu et seuillage pour reconstituer le message logique.
* **Sondes (Visualisation)** : Affichage graphique des signaux temporels à chaque nœud de la chaîne.

## 🛠️ Architecture du projet

Le projet respecte une architecture modulaire orientée objet (interfaces `SourceInterface`, `DestinationInterface`). 

```
[Source] -> (Bits) -> [Transmetteur] -> (Signal analogique) -> [Canal + Bruit] -> (Signal bruité) -> [Récepteur] -> (Bits) -> [Destination]
                                                                                                                        |
                                                                                                                        v
                                                                                                           [Comparateur (TEB)]
```

## Compilation et exécution

Depuis la racine du projet :
```bash
./compile      # compile src/ vers bin/
./runTests     # tests de bout en bout du simulateur
./genDoc       # génère la javadoc dans docs/
./cleanAll     # vide bin/ et docs/
```

### Syntaxe
```bash
./simulateur [options]
```

**Options (conformes à la commande unique SIT213) :**
* `-mess m` : suite de 0 et de 1 d'au moins 7 caractères (message à émettre), ou entier d'au plus 6 chiffres (longueur d'un message aléatoire). Défaut : 100 bits aléatoires.
* `-s` : active les sondes (affichage graphique des signaux).
* `-seed v` : semence entière des générateurs aléatoires (message et bruit), pour rejouer une simulation à l'identique.
* `-form f` : forme d'onde `NRZ`, `NRZT` ou `RZ`. Défaut : `RZ`.
* `-nbEch ne` : nombre d'échantillons par bit. Défaut : 30. **Minimum : 10**, choix de l'équipe : en dessous, le tiers central du RZ et la rampe du NRZT tiennent sur 0 ou 1 échantillon et la forme d'onde n'est plus représentée correctement.
* `-ampl min max` : amplitudes flottantes, avec `min < max` (sinon erreur). Défaut : 0.0 et 1.0.
* `-snrpb s` : canal bruité (bruit blanc additif gaussien), `s` est le rapport signal sur bruit par bit $E_b/N_0$ en dB (flottant). Défaut : canal non bruité.

La présence d'au moins une des options `-form`, `-nbEch`, `-ampl` ou `-snrpb` active la simulation analogique. En cas d'argument invalide, le simulateur affiche l'erreur et se termine avec un code de retour non nul.

---

## Exemples

Valeurs mesurées avec `-seed 1` sur 10 000 bits (le TEB varie légèrement d'une semence à l'autre).

| Commande | TEB |
|---|---|
| `./simulateur -mess 10000 -form NRZ -ampl -1 1` | 0.0 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 8 -seed 1` | ≈ 0.25 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 20 -seed 1` | ≈ 0.003 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 25 -seed 1` | 0.0 |
| `./simulateur -mess 0110100101 -form RZ -nbEch 20 -ampl 0 5 -s` | 0.0 (avec affichage des sondes) |

**Remarque sur les performances en présence de bruit :** le récepteur actuel décide sur l'échantillon central de chaque bit. Il n'exploite donc qu'un échantillon sur `nbEch` et perd $10\log_{10}(\text{nbEch})$ dB (≈ 14,8 dB pour 30 échantillons) par rapport au récepteur optimal. Le TEB mesuré suit $Q\big(\sqrt{2\,(E_b/N_0)/\text{nbEch}}\big)$ en NRZ antipodal ; voir le rapport de l'étape 3.

## Auteurs
Groupe B4 – FIP2A : BLOMBOU Ethan, BOUABOUD Anis-Melwan, NANDA Laurent, SISSOKO Mohamed Djoncounda, ZIANI Mohamed Amine.
Projet SIT213 – IMT Atlantique.
