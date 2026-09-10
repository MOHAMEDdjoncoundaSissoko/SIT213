# SIT213 - Etape 1

**Auteurs :** Ziani Amine, Sissoko M.Djoncounda, Nanda Laurent
**Projet :** Simulation d'une chaîne de transmission numérique

---

## Ce que fait ce projet

L'idée de base est de simuler ce qui se passe quand on envoie un message binaire (une suite de 0 et de 1) d'un point A à un point B à travers un canal de transmission.

Dans cette première étape, le canal est parfait : aucun bruit, aucune perte. Le message arrive exactement tel qu'il a été envoyé. L'intérêt c'est de poser les bases de l'architecture avant d'ajouter du bruit dans les étapes suivantes.

La chaîne se décompose en trois blocs :

```
Source  -->  TransmetteurParfait  -->  DestinationFinale
```

- La **Source** génère le message : soit un message qu'on lui impose (ex : `0110101`), soit un message aléatoire d'une longueur donnée.
- Le **TransmetteurParfait** reçoit les bits et les retransmet tels quels, sans toucher à rien.
- La **DestinationFinale** reçoit et stocke le message pour qu'on puisse le comparer avec ce qui a été envoyé.

A la fin, on calcule le **TEB (Taux d'Erreur Binaire)** : c'est le rapport entre le nombre de bits mal reçus et le nombre de bits total. Ici le TEB vaut toujours 0.0 puisque le transmetteur est parfait.

Si on active l'option `-s`, des fenêtres graphiques s'ouvrent pour visualiser le signal en sortie de la source et en sortie du transmetteur.

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
# par défaut : message aléatoire de 100 bits
./simulateur

# message fixe qu'on impose (7 caractères minimum)
./simulateur -mess 0110101

# message aléatoire de 50 bits
./simulateur -mess 50

# même chose mais reproductible (même seed = même tirage)
./simulateur -mess 50 -seed 42

# avec affichage des signaux en fenêtre graphique
./simulateur -mess 0110101 -s
```

---

## Options

`-mess m` : précise le message ou sa longueur.
- Si m est une suite de 0 et 1 d'au moins 7 caractères → c'est le message à envoyer.
- Si m est un entier entre 1 et 6 chiffres → c'est le nombre de bits du message aléatoire à générer.
- Par défaut : 100 bits aléatoires.

`-seed v` : fixe la semence du générateur aléatoire. Utile pour rejouer exactement la même simulation.

`-s` : active les sondes graphiques pour voir le signal à l'émission et à la réception.

---

## Tests automatiques

```bash
./runTests
```

Lance 6 tests et affiche le bilan OK/KO.

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
