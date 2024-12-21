# LifeLog (VERY WIP)

Project originally created [on GitHub](https://github.com/toasterofbread/lifelog) on 2024-08-04

Try me at https://lifelog.toastbits.dev/ (demo log source coming soon)

## About

This project was created to replace the multi-hundred-page Google doc I use to write about shows, films, and books.

The goals areto have:
- [ ] A performant interface that can be used to easily log my thoughts on things
- [ ] Support (including responsive UI) for desktop, web, and Android
- [ ] Fast saving and loading of logs from a remote source for easy sync
- [ ] A plugin system for when I inevitably want to add new types of logs
- [ ] An easy way to browse logs by metadata (such as for when I want to find all the times I've watched Bloom Into You)
- [x] A reason for this project to exist when Obsidian already does most of this
    - It's FOSS

## Features
###### (Only the interesting and/or stupidly impractical ones)

- All non-asset content stored in a human-readable partially-markdown format
- Logs stored accessed and stored over http using the Git protocol, not a web API (see KoGit below)

## Related libraries

- [KoGit](https://gitlab.com/toasterofbread/kogit) - A limited-functionality Git client written in pure multiplatform Kotlin (originally a module in LifeLog)
- [ComposeKit](https://gitlab.com/toasterofbread/composekit) - Several modules in ComposeKit (such as navigation) as well as its modularisation refactor were written specifically for LifeLog

## Development

### Running the project (debug)

#### Desktop

```shell
./gradlew application:app:run
```

#### Android

```shell
./gradlew application:app:installDebug
```

#### Web

Note: LifeLog is developed and tested on Firefox. Issues may arise when using other browsers.

```shell
./gradlew application:app:wasmJsBrowserDevelopmentExecutableDistribution

# Then run a HTTP server in application/app/build/outputs/wasmDistribution/<lifelog-...> and open it in a browser
```
