# rosebud

# [![CircleCI](https://circleci.com/gh/denisidoro/rosebud.svg?style=svg)](https://circleci.com/gh/denisidoro/rosebud)

A personal financial platform.

![Preview](https://user-images.githubusercontent.com/3226564/55515856-05ffef00-5642-11e9-8fc7-47535535c24e.jpg)


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

In [The Sims][thesims], `rosebud` is a money cheat that gives an additional §1,000.

[localgrafana]: http://localhost:3000
[thesims]: https://www.ea.com/games/the-sims
