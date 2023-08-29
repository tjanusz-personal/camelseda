# Camel SEDA
This project is a sample Spring Boot Application integrated w/CAMEL to demonstrate a potential bug with the latest 4.x version of
Camel and the SEDA components. It was created so CAMEL team member could investigate potential issues my current team is experiencing.

Summary:
* Spring Boot Web Application - using Spring Boot 3.x
* Camel routes/SEDA - using Camel 4.0.0
* Application utilizes Camel's dynamic routing feature which may contribute to the issue?
* Requires Java 17
* Application logic is in Kotlin but is pretty basic so should be easily followable

Issue summary:
* Within the Camel 4.x version an application using dynamic routing and the SEDA component after creating and deleting a route dynamically 2xs 
upon creation of the route the 3rd time appears to 'hang' the SEDA queue thus hanging the servlet etc.
* Camel version 4.0.0-M1 works fine! See gradle.properties for comments

# Steps to Reproduce Possible Issue
**This app is HARD WIRED TO THE SPECIFIC SCENARIO listed below! The normal application would have much more functionality, 
error checking, etc.**

* build application locally (see below)
* Start spring boot web application locally
```bash
# Navigate to build/libs directory
cd build/libs

# start application with logging DEBUG for camel
java -jar camelseda-0.0.1-SNAPSHOT.jar -X"mx1024m" -D"server.port=8089"  --logging.level.org.apache.camel=DEBUG --0
```
* Using an HTTP tool (like post man) send HTTP requests to the localhost endpoint in this SPECIFIC ORDER
```
# Sample normal command
POST: localhost:8080/camel/dialcommand/
BODY: --id e70c0565 --dhInitialize

# Sample kill route command
POST: localhost:8080/camel/dialcommand/
BODY: --id e70c0565 --dhKill

Repeat sequence 2x and on the 3rd attempt at the 'normal command' the queue will hang and servlet won't respond
```


## Building

This application uses Gradle 8 for dependency management and building. 

Build the app using:

```
# mac osx
./gradlew clean build
```

## Running Locally
See the summary section for an example of how to run locally. Basically it's just start spring boot application JAR with some basic
parameters.

```bash
# start application with logging DEBUG for camel
java -jar camelseda-0.0.1-SNAPSHOT.jar -X"mx1024m" -D"server.port=8089"  --logging.level.org.apache.camel=DEBUG --0
```


## Tests
There are no unit tests for this project besides the basic spring boot context load test.

