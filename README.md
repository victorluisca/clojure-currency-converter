# Currency Converter CLI

A command-line application built in Clojure to fetch exchange rates and convert values using the Frankfurter API.

## Installation

1. Clone the repository:

```bash
git clone https://github.com/victorluisca/clojure-weather-cli.git
cd clojure-weather-cli
```

2. Make sure you have Clojure installed (1.12+):

```bash
clj --version
```

## Commands

* Convert `<amount> [--from CODE] [--to CODE]`
* List Currencies `--list` or `-l`
* Help `--help` or `-h`

## Build

To generate standalone executable JAR (Uberjar):

```bash
clj -T:build uber
```

Run the JAR:

```bash
java -jar target/currency-converter-0.1.0-standalone.jar 100 --from USD --to BRL
```
