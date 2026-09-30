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
  * Canal à trajets multiples (jusqu'à 5 trajets indirects, retardés et atténués), bruité ou non.
* **Codage de canal** (optionnel) : chaque bit est émis sur trois bits (`0 → 010`, `1 → 101`) ; le décodeur corrige une erreur par paquet de trois bits.
* **Réception et Décision** : filtre adapté à la forme d'onde (corrélation de chaque temps bit avec $s_1 - s_0$) puis seuillage pour reconstituer le message logique.
* **Sondes (Visualisation)** : Affichage graphique des signaux temporels à chaque nœud de la chaîne.

## 🛠️ Architecture du projet

Le projet respecte une architecture modulaire orientée objet (interfaces `SourceInterface`, `DestinationInterface`). 

```
[Source] -> (Bits) -> [Transmetteur] -> (Signal analogique) -> [Canal + Bruit] -> (Signal bruité) -> [Récepteur] -> (Bits) -> [Destination]
                                                                                                                        |
                                                                                                                        v
                                                                                                           [Comparateur (TEB)]
```

## Contenu de l'archive

| Élément | Contenu |
|---|---|
| `src/` | sources Java du simulateur, par paquetage :<br>`sources` (source fixe ou aléatoire), `transmetteurs` (codeur et décodeur de canal, émetteur, canaux parfait, bruité et à trajets multiples, récepteur à filtre adapté), `destinations`, `information`, `visualisations` (sondes et vues graphiques), `simulateur` (programme principal et analyse des options) et `tests` (tests unitaires JUnit) |
| `bin/` | classes compilées ; **vide dans l'archive**, rempli par `./compile` |
| `docs/` | documentation Javadoc ; **vide dans l'archive**, remplie par `./genDoc` |
| `lib/` | bibliothèques de test : JUnit 4, Hamcrest et JUnit Platform Console Standalone (utilisées pour compiler et lancer les tests de `src/tests`) |
| `compile` | compile toutes les sources de `src/` (tests compris) vers `bin/` |
| `genDoc` | génère la Javadoc des sources (hors tests) dans `docs/` |
| `cleanAll` | vide `bin/` et `docs/` |
| `simulateur` | lance une simulation ; options conformes à la commande unique (voir ci-dessous) |
| `runTests` | autotests de bout en bout du simulateur (étapes 1 à 5) : 20 exécutions nominales et 12 cas d'erreur |
| `README.md` | ce fichier |

Les tests unitaires JUnit se lancent, après `./compile`, avec :
```bash
java -jar lib/junit-platform-console-standalone-6.1.3.jar execute -cp bin --scan-classpath
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
* `-ti dt ar [dt ar ...]` : canal à trajets multiples. Chaque couple ajoute un trajet indirect décalé de `dt` échantillons (entier ≥ 0) et d'amplitude relative `ar` (flottant) : $r(n) = s(n) + \sum_k ar_k \, s(n - dt_k)$. De 1 à 5 couples. Sans `-snrpb`, seuls les échos perturbent le signal ; avec `-snrpb`, le bruit gaussien est ajouté après les échos. Défaut : pas de trajet indirect.
* `-codeur` : active le codage de canal. Un codeur est inséré juste après la source (`0 → 010`, `1 → 101`) et un décodeur juste avant la destination (mot de code le plus proche, soit une erreur corrigée par paquet de trois bits). Le TEB est mesuré entre la source et la destination. Défaut : pas de codage.

La présence d'au moins une des options `-form`, `-nbEch`, `-ampl`, `-snrpb` ou `-ti` active la simulation analogique. En cas d'argument invalide, le simulateur affiche l'erreur et se termine avec un code de retour non nul.

---

## Exemples

Valeurs mesurées avec `-seed 1` sur 10 000 bits (le TEB varie légèrement d'une semence à l'autre).

| Commande | TEB |
|---|---|
| `./simulateur -mess 10000 -form NRZ -ampl -1 1` | 0.0 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 0 -seed 1` | ≈ 0.095 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 4 -seed 1` | ≈ 0.021 |
| `./simulateur -mess 10000 -form NRZT -ampl -1 1 -snrpb 8 -seed 1` | ≈ 0.0007 |
| `./simulateur -mess 0110100101 -form RZ -nbEch 20 -ampl 0 5 -s` | 0.0 (avec affichage des sondes) |
| `./simulateur -mess 60000 -form NRZ -nbEch 10 -ampl -1 1 -snrpb 2 -seed 1` | ≈ 0.038 |
| `./simulateur -mess 60000 -form NRZ -nbEch 10 -ampl -1 1 -snrpb 2 -seed 1 -codeur` | ≈ 0.0041 (théorie $3p^2 - 2p^3$ avec $p = 0{,}0375$) |
| `./simulateur -mess 10000 -form NRZ -nbEch 10 -ampl -1 1 -ti 10 0.5 -seed 1` | 0.0 (écho trop faible pour franchir le seuil) |
| `./simulateur -mess 10000 -form NRZ -nbEch 10 -ampl -1 1 -ti 10 0.9 20 0.9 -seed 1` | ≈ 0.25 (erreur dès que les deux bits précédents sont opposés au bit courant) |

**Performances en présence de bruit :** le récepteur utilise un filtre adapté. Pour chaque bit, il corrèle les `nbEch` échantillons reçus avec $g = s_1 - s_0$ (différence des formes d'un bit 1 et d'un bit 0, générées par l'émetteur) et compare le résultat au seuil $(\lVert s_1\rVert^2 - \lVert s_0\rVert^2)/2$. La même formule sert pour NRZ, NRZT et RZ. Le TEB mesuré suit la théorie : $Q\big(\sqrt{2E_b/N_0}\big)$ en NRZ antipodal ($1{,}25\cdot10^{-2}$ à 4 dB), $Q\big(\sqrt{E_b/N_0}\big)$ en NRZ unipolaire. À l'étape 3, le récepteur décidait sur le seul échantillon central et perdait $10\log_{10}(\text{nbEch})$ dB (≈ 14,8 dB pour 30 échantillons).

## Auteurs
Groupe B4 – FIP2A : BLOMBOU Ethan, BOUABOUD Anis-Melwan, NANDA Laurent, SISSOKO Mohamed Djoncounda, ZIANI Mohamed Amine.
Projet SIT213 – IMT Atlantique.
