# rosebud

A personal financial platform.

![Preview](https://user-images.githubusercontent.com/3226564/55515856-05ffef00-5642-11e9-8fc7-47535535c24e.jpg)

:warning: This repository will receive no further support. I'll continue development in a private repo so that I can faster iterate over it without worrying about exposing personal data. In case you really like this project, please contact me so that we can better coordinate efforts. :warning:

### Running

Simply clone the repository and run 
```bash
./scripts/start
```

### Visualizing

Head to [localhost:3000][localgrafana] to open the Grafana dashboard. 

By default, the user is `admin` and the password is `admin123`.

### Editing data

```bash
cp ./server/resources/example_log.edn ./log.edn
# edit ./log.edn
```

### Etymology 

In [The Sims][thesims], `rosebud` is a money cheat that gives an additional §1,000.

[localgrafana]: http://localhost:3000
[thesims]: https://www.ea.com/games/the-sims
