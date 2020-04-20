# GasSMan - Telegram Bot Interface
## Technical Scope
GAS (Gruppo di Acquisto Solidale) Sales Management è un sistema Java basato su un'architettura MicroServices che alimenta due interfacce attraverso dei servizi REST : un bot di Telegram e un'interfaccia di amministrazione.
GasSMan ha come scopo quello di fornire ad un amminstratore di un GAS un'interfaccia per configurare la lista dei prodotti in distribuzione, e un bot grazie al quale gli inscritti possono consultare l'elenco dei prodotti disponibili ed effettuare gli ordini.

## Didactical Scope
Questo repository è un modulo di un progetto Java di esempio descritto nel libro (scritto in lingua italiana) **"Anche i microservizi nel loro piccolo s'incazzano - Guida alla comprensione dei principi di un'architettura in microservizi"** di [Giuseppe Vincenzi](https://gvincenzi.tumblr.com/).

## Technologies
Technologies used in this module of the project:
- Spring Boot TelegramBots
- Netflix Eureka Server
- Open Feign
- RabbitMQ
- Lombok
- Maven

## Installation
### Property file
Prima di lanciare il modulo, bisognerà creare il proprio file di properties, partendo dal file di default "application.yml" e chiamandolo "application-gassman-[NOME_DEL_PROFILO]" scegliendo un [NOME_DEL_PROFILO] a vostra scelta.
Nel file di properties troverete, tra le altre prooprietà, le seguenti chiavi da completare :
```yaml
rabbitmq :
    addresses: amqp://***
gassman:
  telegram:
    bot:
      username: YOUR_USERNAME
      token: YOUR_TOKEN

  api:
    username:
    password:
```

La chiave `rabbitmq` dovrà contenere l'indirizzo AMQP della vostra installazione di RabbitMQ.
La chiave `gassman>telegram>bot` dovrà contenere i dati che **BotFather** di Telegram ha associato al vostro bot.
La chiave `api` è una coppia username/password che sarà utilizzata per le chiamate ai servizi REST in BasicAuth : l'encoder che è utilizzato nell'implementazione proposta è `BCryptPasswordEncoder`.
Se volete ad esempio usera la coppia username/passwaord `api/gassman`, dovrete inserire nel file di propeties :
```yaml
rabbitmq :
    addresses: amqp://***
api:
  username: api
  password: $2a$10$ID/NjgCJ2tm2BCFCIdaV2.Z.Ttz2KrD1FtKebdLMooMDXu8OIYAdy
```

### Spring Profile
Il modulo va lanciato specificando uno spring active profile "gassman-[NOME_DEL_PROFILO]": il nome sarà quello che avrete scelto come suffisso del vostro file di properties.

### Start GasSMan - Telegram Bot Interface
Per lanciare il modulo e il server Eureka, basterà lanciare un comando install in MAVEN e, dopo aver posizionato il jar dove più conviene nel vostro file system, potrete lanciare il comando :

```
java -Dspring.profiles.active=gassman-[NOME_DEL_PROFILO] -jar gassman-telegram-bot-1.2.0.jar
```

Questo servizio deve essere lanciato con un Eureka Server già attivo e in ascolto, secondo quanto configurato nel file di properties (application.yml), sulla porta 8880.
Dopo aver correttamente lanciato questo modulo, potrete dunque andare sull'interfaccia di Eureka Server all'indirizzo http://localhost:8880 e verificare che il servizio gassman-telegram-bot-service è presente nella lista dei servizi attivi.

## Build Information
**Travis Ci page** : [Click here to view build history](https://travis-ci.org/gvincenzi/gassman-telegram-bot)

**Last build** : <img src="https://travis-ci.org/gvincenzi/gassman-telegram-bot.svg?branch=master" alt="last build satus">
