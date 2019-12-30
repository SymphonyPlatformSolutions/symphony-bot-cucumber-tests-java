## Symphony Bot Cucumber Tests
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
   * a Symphony user attaches ``{string}``
   * the user is an owner of the room
   * the user is not an owner of the room
* **When..**
  * a Symphony user sends the message in an IM
  * a Symphony user sends the message in a room
* **Then..**
  * The bot should display the following response
  * The bot should send this data ``{string}``
  * The bot should send this attachment ``{string}``

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
