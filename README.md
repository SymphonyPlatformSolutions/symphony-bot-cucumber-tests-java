# Symphony Bot Cucumber Tests

[![Maven Central](https://img.shields.io/maven-central/v/com.symphony.platformsolutions/symphony-bot-cucumber-tests-java)](https://mvnrepository.com/artifact/com.symphony.platformsolutions/symphony-bot-cucumber-tests-java) [![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](https://opensource.org/licenses/MIT) [![Email](https://img.shields.io/static/v1?label=contact&message=email&color=darkgoldenrod)](mailto:platformsolutions@symphony.com?subject=Cucumber%20Tests)

This project aims to provide Symphony bot developers with a foundation to write end-to-end behavioural tests using Cucumber

## Installation
### Maven
```xml
<dependency>
    <groupId>com.symphony.platformsolutions</groupId>
    <artifactId>symphony-bot-cucumber-tests</artifactId>
    <version>[0,)</version>
</dependency>
```

### Gradle
```groovy
compile: 'com.symphony.platformsolutions:symphony-bot-cucumber-tests:0.+'
```

## Requirements
* Project built on the Symphony Client Library SDK (``symphony-api-client-java``)
* Cucumber plugin for your IDE

## Writing a Test
* Cucumber tests have two parts:
  * Features (in English)
  * Step Definitions (in code)

This project provides a number of basic step definitions to simulate Symphony-related activity with a bot

### Example Feature
```gherkin
Feature: Basic Commands

  Scenario: Say hello
    Given a Symphony user types "hello"
    When a Symphony user sends the message in a room
    Then The bot should display the following response
      """
      Hi there!
      """
```

### Supported Steps
* **Given..**
   * the stream id is ``{string}``
   * a Symphony user types ``{string}``
   * there is a form with id ``{string}``
   * a Symphony user types ``{string}`` into the ``{string}`` text field
   * a Symphony user checks ``{string}`` for the ``{string}`` checkbox
   * a Symphony user chooses ``{string}`` for the ``{string}`` radio button
   * a Symphony user chooses ``{string}`` for the ``{string}`` dropdown box
   * a Symphony user chooses these values for the ``{string}`` person selector
   * a Symphony user attaches a file named ``{string}``
   * the user is an owner of the room
   * the user is not an owner of the room
* **When..**
   * a Symphony user sends the message in an IM
   * a Symphony user sends the message in a room
   * a Symphony user submits the form using the ``{string}`` button
* **Then..**
   * The bot should display the following response
   * The bot's response should contain
   * The bot's response should contain ``{string}``
   * The bot should send the following response data
   * The bot's response data should contain ``{string}``
   * The bot should send an attachment named ``{string}``

### Adding Features and Step Definitions
You should continue to add features and step definitions to test custom logic specific to your bot.

#### Feature File
```gherkin
Feature: Bot Friends

  Scenario: Respond to friends
    Given a Symphony user types "are we friends?"
      And the user is a friend
    When a Symphony user sends the message in an IM
    Then The bot should display the following response
      """
      Hello friend!
      """
```

#### Step Definitions File
```java
public class MyBotStepDefinitions implements En {
    public MyBotStepDefinitions() {
        Given("the user is a friend", () -> {
            FriendService.addFriend(SymStepDefinitions.getSampleUser().getUserId());
        });
    }
}
```

#### Implementation
There are two options for implementation:
* ``SymBotClient`` injection
  * In your listener implementations, add a constructor with a single ``SymBotClient`` parameter
  * Use that ``SymBotClient`` instance for all calls
* Static
  * Make your main class implement ``SymStaticMain``
  * Add a constructor with a single ``SymBotClient`` parameter
  * Override the ``getBotClient()`` method to return the shared ``SymBotClient`` instance

### IDE Configuration
* In the respective launch configuration for the feature, set the ``Glue`` as:
  * ``com.symphony.ps.sdk.bdd``
  * Your own package containing your custom Step Definitions
