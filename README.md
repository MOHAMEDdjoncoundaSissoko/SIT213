# SIT213 - Chaîne de transmission

**Auteurs :** Ziani Amine, Sissoko Mohamed, Nanda Laurent, Blombou Ethan, Bouaboud Anis-Melwan

**Projet :** Simulation d'une chaîne de transmission numérique

---

## Ce que fait ce projet

On simule l'envoi d'un message binaire (une suite de 0 et de 1) d'une source vers une destination, à travers un ou plusieurs transmetteurs.

Deux modes sont possibles :

**Mode logique (par défaut)**

```
Source  -->  TransmetteurParfait  -->  Destination
```

Le canal est parfait : aucun bruit, aucune perte, le message arrive tel quel. Le TEB (Taux d'Erreur Binaire) vaut donc toujours 0.0. C'est la base de l'architecture avant d'ajouter du bruit.

**Mode analogique**

```
Source --> TransmetteurLogiqueAnalogique --> TransmetteurAnalogiqueParfait --> TransmetteurAnalogiqueLogique --> Destination
```

Ici le message binaire est converti en un vrai signal analogique (une forme d'onde), transmis à travers un canal, puis reconverti en binaire à la réception. On passe automatiquement dans ce mode dès qu'on utilise une des options `-form`, `-nbEch` ou `-ampl`.

Trois formes d'onde sont disponibles :
- `NRZ` : signal qui reste au niveau haut ou bas pendant tout le bit.
- `RZ` : signal actif seulement sur le tiers central du temps bit, retour à zéro sinon.
- `NRZT` : comme NRZ mais avec des transitions progressives (montée/descente) entre les niveaux, en fonction des bits voisins.

Dans les deux modes, la Source génère le message (imposé ou aléatoire), et à la fin on compare le message émis et le message reçu pour calculer le TEB.

Si on active l'option `-s`, des fenêtres graphiques s'ouvrent pour visualiser le signal (sondes logiques ou analogiques selon le mode).

---

## Compilation et lancement

Compiler le projet :
```bash
./compile
```

Lancer une simulation :
```bash
./simulateur [options]
```

Exemples concrets :
```bash
# par défaut : message aléatoire de 100 bits, chaîne logique parfaite
./simulateur

# message fixe qu'on impose (7 caractères minimum)
./simulateur -mess 0110101

# message aléatoire de 50 bits
./simulateur -mess 50

# même chose mais reproductible (même seed = même tirage)
./simulateur -mess 50 -seed 42

# avec affichage des signaux en fenêtre graphique
./simulateur -mess 0110101 -s

# chaîne analogique avec une forme NRZ
./simulateur -mess 1100 -form NRZ

# chaîne analogique avec amplitude et nombre d'échantillons personnalisés
./simulateur -mess 101 -form NRZT -nbEch 30 -ampl 0 5 -s
```

---

## Options

`-mess m` : précise le message ou sa longueur.
- Si m est une suite de 0 et 1 d'au moins 7 caractères → c'est le message à envoyer.
- Si m est un entier entre 1 et 6 chiffres → c'est le nombre de bits du message aléatoire à générer.
- Par défaut : 100 bits aléatoires.

`-seed v` : fixe la semence du générateur aléatoire. Utile pour rejouer exactement la même simulation.

`-s` : active les sondes graphiques pour voir le signal à l'émission et à la réception.

`-form f` : force le passage en mode analogique et fixe la forme d'onde (`NRZ`, `RZ` ou `NRZT`). Par défaut : `RZ`.

`-nbEch n` : force le passage en mode analogique et fixe le nombre d'échantillons par bit. Par défaut : 30.

`-ampl min max` : force le passage en mode analogique et fixe les amplitudes basse et haute du signal. Par défaut : 0.0 et 1.0.

---

## Tests automatiques

```bash
./runTests
```

Lance une douzaine de tests (chaîne logique, chaîne analogique dans ses différentes configurations, et cas d'erreurs) et affiche le bilan OK/KO.

## Nettoyage

```bash
./cleanAll
```

Supprime les fichiers compilés (`bin/`) et la documentation (`docs/`).

## Javadoc

```bash
./genDoc
```

Génère la documentation dans `docs/`.
