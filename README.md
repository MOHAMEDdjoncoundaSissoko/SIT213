# Simulateur de Chaîne de Transmission Numérique - SIT213

Ce projet est un simulateur en Java permettant de modéliser, simuler et analyser les performances d'une chaîne de transmission numérique complète. Il permet d'étudier l'impact de différents codages en ligne, de l'ajout d'un canal bruité (Bruit Blanc Gaussien Additif - AWGN), et des modulations sur la qualité du signal transmis, mesurée par le Taux d'Erreur Binaire (TEB).

## Fonctionnalités principales

* **Source d'information** : Génération de messages binaires (séquences aléatoires de $N$ bits ou déterministes à partir d'une chaîne de caractères).
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

Le projet respecte une architecture modulaire orientée objet (Interfaces `Emetteur`, `Recepteur`). 

```
[Source] -> (Bits) -> [Transmetteur] -> (Signal analogique) -> [Canal + Bruit] -> (Signal bruité) -> [Récepteur] -> (Bits) -> [Destination]
                                                                                                                        |
                                                                                                                        v
                                                                                                           [Comparateur (TEB)]
```

## Compilation et Exécution

### Compilation
Placez-vous à la racine du projet et compilez les fichiers Java :
```bash
javac *.java
```

### Syntaxe de base
```bash
./simulateur [options]
```

**Options disponibles :**
* `-mess <n | string>` : Longueur du message aléatoire (entier) ou chaîne de caractères à transmettre.
* `-form <NRZ | NRZT | RZ>` : Type de forme d'onde.
* `-nbEch <n>` : Nombre d'échantillons par bit.
* `-ampl <min> <max>` : Amplitudes du signal (ex: `-1 1`).
* `-ebn0 <valeur>` : Rapport signal sur bruit par bit ($E_b/N_0$) en dB.
* `-sondes` : Active les graphiques (oscilloscopes temporels).

---

## Cas d'usage concrets et Analyse des performances

Voici plusieurs scénarios progressifs pour comprendre l'impact des paramètres sur la transmission.

### Cas n°1 : Le canal idéal (Le monde parfait)
On transmet 10 000 bits avec un codage NRZ classique. Aucun bruit n'est ajouté.

**Commande :**
```bash
./simulateur -mess 10000 -form NRZ -nbEch 30 -ampl -1 1
```

**Résultat attendu :**
> TEB : 0.0

**Analyse :** Le signal analogique généré arrive parfaitement intact au récepteur. L'échantillonnage et la décision se font sans ambiguïté. Le Taux d'Erreur Binaire est nul.

### Cas n°2 : Introduction d'un bruit modéré ($E_b/N_0 = 8 \text{ dB}$)
On ajoute un bruit gaussien, mais la puissance du signal reste nettement supérieure à celle du bruit.

**Commande :**
```bash
./simulateur -mess 10000 -form NRZT -nbEch 30 -ampl -1 1 -ebn0 8
```

**Résultat attendu :**
> TEB : ~ 0.002 (soit 0.2% d'erreur)

**Analyse :** Le signal reçu est "bruité" (tremblotant si vous activez `-sondes`), mais l'amplitude du bruit est généralement insuffisante pour faire passer un niveau bas au-dessus du seuil de décision (ou inversement). Quelques erreurs isolées apparaissent.

### Cas n°3 : Canal fortement bruité ($E_b/N_0 = 1 \text{ dB}$)
On dégrade fortement le rapport signal sur bruit. Le signal commence à être noyé dans le bruit.

**Commande :**
```bash
./simulateur -mess 10000 -form NRZT -nbEch 30 -ampl -1 1 -ebn0 1
```

**Résultat attendu :**
> TEB : ~ 0.15 à 0.25

**Analyse :** Avec un $E_b/N_0$ aussi faible, le bruit gaussien provoque de fortes variations de tension. Le récepteur prend très souvent la mauvaise décision lors de l'échantillonnage. Jusqu'à un quart des bits reçus sont faux.

### Cas n°4 : Transmission d'un message textuel (avec sondes)
Au lieu de bits aléatoires, on veut transmettre un mot précis et visualiser les étapes.

**Commande :**
```bash
./simulateur -mess "HELLO" -form RZ -nbEch 20 -ampl 0 5 -sondes
```

**Analyse :** Le simulateur convertit la chaîne "HELLO" en binaire (via ASCII). Il utilise un codage Return to Zero avec des niveaux de tension de 0V à 5V. L'option `-sondes` ouvrira des fenêtres graphiques permettant de voir le flux binaire initial, le signal RZ généré, et le signal à la réception.

---

## Courbe du TEB en fonction du $E_b/N_0$

Si vous effectuez une campagne de tests en faisant varier le `-ebn0` de $0$ à $10$ dB, vous observerez que la courbe du TEB suit la forme de la fonction ERFC (fonction d'erreur complémentaire), validant ainsi le modèle théorique des télécommunications :
* **$E_b/N_0 < 0 \text{ dB}$** : TEB proche de $0.5$ (Le signal est illisible, c'est comme tirer à pile ou face).
* **$E_b/N_0 \approx 5 \text{ dB}$** : TEB critique (chute drastique des erreurs).
* **$E_b/N_0 > 10 \text{ dB}$** : TEB proche de $0$ (Transmission fiable).

## 👥 Auteurs
* **[ ]**
* Projet SIT213 - Simulation de systèmes de télécommunications.