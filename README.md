# rosebud

# [![CircleCI](https://circleci.com/gh/denisidoro/rosebud.svg?style=svg)](https://circleci.com/gh/denisidoro/rosebud)

A personal financial platform.

### Running

Simply clone the repository and run 
```bash
./scripts/start
```

### Visualizing

Head to [localhost:3000][localgrafana] to open the Grafana dashboard. By default, the user is `admin` and the password is `admin123`.

### Editing data

```bash
cp ./server/resources/example_log.edn ./log.edn
nano ./log.edn
```

### Etymology 

In The Sims, `rosebud` is a money cheat that gives an additional §1,000.

[localgrafana]: http://localhost:3000
[thesims]: https://www.ea.com/games/the-sims
