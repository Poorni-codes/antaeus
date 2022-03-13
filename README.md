## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## My Solution

1. During the application startup, I am creating a coroutine scope in the main method, where a new scope is launched on scheduled basis using Library called krontab which is Crontab-like
syntax in scheduling of some Kotlin Coroutines tasks to do from time to time.

2. Inside the given scope there are 2 coroutine tasks as given below:
    I am establishing a PUB/SUB model as below
    a. activeMQPublisher  -> uses activeMQ connection factory to establish a publisher TOPIC where all the pending Invoices are sent
    b. activeMQSubscriber -> uses a DURABLE SUBSCRIBER TOPIC, which listens to published messages from the producer topic asynchronously 

3. Once the messages are dequeued from the consumer , it is sent to a billing service -> Where it is charged based on the invoice and updated to PAID status in database.

4. Once invoice is charged, those messages can be purged with timetolive value(available in activeMQ), right now I am purging every hour the messages
that has been updated.

5. In the case of failover scenario for producer, where it becomes unavailable, the subscriber is will be online and looks for the TOPIC to get active again.
Once the producer topic comes up, it fetches invoices from the database only if there is any PENDING status.

6. In case of failover scenario for consumer, the consumer goes offline. Now the producer topic which has new messages enqueued hold a copy of these messages for the offline subscriber
using its unique client ID. Once the subscriber service comes up, the producer pushes the messages to the subscriber thereby no messages are lost.

7. The coroutine scope is still active (until asked to get closed) and light threaded which does not interact with the main activities of the application, making it easy to do scheduling
without compromising on the application performances.

## Libraries :

1. APACHE ACTIVEMQ CONNECTION : Version 5.16.4
   Topic connection is established in this solution to benefit one to many connections, thereby many consumers services can be added without disturbing the existing application architecture.
   Durable topics however are different as they must logically persist an instance of each suitable message for every durable consumer, since each durable consumer gets their own copy 
   of the message.(helpful for failover scenario)

2. KRONTAB : Version 0.7.1
   Library for using Crontab-like syntax in scheduling of some Kotlin Coroutines tasks to do from time to time
   Unlike other schedulers, A coroutine is an instance of suspendable computation & KRONTAB scheduling on top of it makes it very easy to launch tasks easily without causing performance delays.

## How to run the project :

1. Clone the project and run the docker yml file using below command
    docker compose up
    This helps in starting an activeMQ locally. 

2. Start the application

3. Open the ActiveMQ webconsole to view the messages queued when an application starts (http://127.0.0.1:8161)

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
