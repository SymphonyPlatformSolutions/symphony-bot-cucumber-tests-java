package com.symphony.ps.sdk.bdd;

import clients.SymBotClient;
import clients.symphony.api.MessagesClient;
import clients.symphony.api.StreamsClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java8.En;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import listeners.ConnectionListener;
import listeners.ElementsListener;
import listeners.IMListener;
import listeners.RoomListener;
import model.*;
import model.events.SymphonyElementsAction;
import org.mockito.ArgumentCaptor;
import utils.SymMessageParser;
import utils.TagBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SymStepDefinitions implements En {
    // Mocks
    private SymBotClient botClient = mock(SymBotClient.class);
    private MessagesClient messagesClient = mock(MessagesClient.class);
    private StreamsClient streamsClient = mock(StreamsClient.class);

    // Listeners
    private IMListener imListener = TestUtils.locateImplementation(IMListener.class, botClient);
    private RoomListener roomListener = TestUtils.locateImplementation(RoomListener.class, botClient);
    private ConnectionListener connectionListener = TestUtils.locateImplementation(ConnectionListener.class, botClient);
    private ElementsListener elementsListener = TestUtils.locateImplementation(ElementsListener.class, botClient);

    // Captors
    private ArgumentCaptor<String> streamIdCaptor = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<OutboundMessage> msgCaptor = ArgumentCaptor.forClass(OutboundMessage.class);

    // Data
    public User user = getSampleUser();
    public static String streamId = "x";
    private String messageText;
    private String data;
    private List<Attachment> attachments;
    private String formId;
    private Map<String, Object> formValues = new HashMap<>();
    private OutboundMessage outMsg;

    // Templates
    private static final String mlTemplate = getMLTemplate();
    private static Pattern mentionsPattern = Pattern.compile("(.*)(@\\[[\\w+\\s+\\-\\,\\.\\(\\)]+\\])(.*)");

    public SymStepDefinitions() {
        SymMessageParser.createInstance(botClient);
        when(botClient.getMessagesClient()).thenReturn(messagesClient);
        when(botClient.getStreamsClient()).thenReturn(streamsClient);

        SymStaticMain mainClass = TestUtils.locateImplementation(SymStaticMain.class, botClient);
        if (mainClass != null) {
            imListener = TestUtils.locateImplementation(IMListener.class);
            roomListener = TestUtils.locateImplementation(RoomListener.class);
            connectionListener = TestUtils.locateImplementation(ConnectionListener.class);
            elementsListener = TestUtils.locateImplementation(ElementsListener.class);
        }

        Given("the stream id is {string}", (String id) -> streamId = id);

        Given("a Symphony user types {string}", (String messageText) -> {
            this.messageText = messageText;
            this.data = getMentionsDataFromMessageText(messageText);
        });

        Given("there is a form with id {string}", (String formId) -> this.formId = formId);

        Given("a Symphony user types {string} into the {string} text field",
            (String value, String key) -> formValues.put(key, value)
        );

        Given("a Symphony user checks {string} for the {string} checkbox",
            (String value, String key) -> formValues.put(key, value)
        );

        Given("a Symphony user chooses {string} for the {string} radio button",
            (String value, String key) -> formValues.put(key, value)
        );

        Given("a Symphony user chooses {string} for the {string} dropdown box",
            (String value, String key) -> formValues.put(key, value)
        );

        Given("a Symphony user chooses these values for the {string} person selector",
            (String key, List<Long> values) -> formValues.put(key, values)
        );

        Given("a Symphony user attaches a file named {string}", (String fileName) -> {
            if (attachments == null) {
                attachments = new ArrayList<>();
            }
            Attachment attachment = new Attachment();
            attachment.setName(fileName);
            attachments.add(attachment);
        });

        Given("the user is an owner of the room", () -> {
            RoomMember member = new RoomMember();
            member.setId(getSampleUser().getUserId());
            member.setOwner(true);
            List<RoomMember> roomMembers = Collections.singletonList(member);
            when(streamsClient.getRoomMembers(streamId)).thenReturn(roomMembers);
        });

        Given("the user is not an owner of the room", () -> {
            RoomMember member = new RoomMember();
            member.setId(getSampleUser().getUserId());
            member.setOwner(false);
            List<RoomMember> roomMembers = Collections.singletonList(member);
            when(streamsClient.getRoomMembers(streamId)).thenReturn(roomMembers);
        });

        When("a Symphony user sends the message in an IM", () -> {
            imListener.onIMMessage(getInboundMessage());
            verify(messagesClient).sendMessage(streamIdCaptor.capture(), msgCaptor.capture());
            outMsg = msgCaptor.getValue();
        });

        When("a Symphony user sends the message in a room", () -> {
            roomListener.onRoomMessage(getInboundMessage());
            verify(messagesClient).sendMessage(streamIdCaptor.capture(), msgCaptor.capture());
            outMsg = msgCaptor.getValue();
        });

        When("a Symphony user submits the form using the {string} button", (String buttonName) -> {
            formValues.put("action", buttonName);
            SymphonyElementsAction sea = new SymphonyElementsAction();
            sea.setFormId(formId);
            sea.setFormValues(formValues);
            elementsListener.onElementsAction(user, sea);
            verify(messagesClient).sendMessage(streamIdCaptor.capture(), msgCaptor.capture());
            outMsg = msgCaptor.getValue();
        });

        Then("The bot should display the following response", (String expected) -> {
            expected = processShorthand(expected);
            assertEquals(expected, outMsg.getMessage());
        });

        Then("The bot's response should contain", (String expected) -> {
            expected = processShorthand(expected);
            assertTrue(outMsg.getMessage().contains(expected));
        });

        Then("The bot's response should contain {string}", (String expected) -> {
            expected = processShorthand(expected);
            assertTrue(outMsg.getMessage().contains(expected));
        });

        Then("The bot should send the following response data",
            (String expected) -> assertEquals(expected, outMsg.getData()));

        Then("The bot's response data should contain {string}",
            (String expected) -> assertTrue(outMsg.getData().contains(expected)));

        Then("The bot should send an attachment named {string}",
            (String expected) -> {
                boolean match = Arrays.stream(outMsg.getAttachment())
                    .anyMatch(a -> a.getName().equalsIgnoreCase(expected));
                assertTrue(match);
            });
    }

    private String processShorthand(String input) {
        String output = input.replaceAll("\n", "<br/>");
        Matcher m = mentionsPattern.matcher(output);
        while (m.find()) {
            String displayName = m.group(2).trim();
            String mentionML = getMentionMLFromDisplayName(displayName);
            output = output.replace(displayName, mentionML);
        }
        return output;
    }

    public InboundMessage getInboundMessage() {
        InboundMessage inMsg = new InboundMessage();
        Stream stream = new Stream();
        stream.setStreamId(streamId);
        inMsg.setStream(stream);
        inMsg.setUser(user);
        inMsg.setMessage(String.format(mlTemplate, messageText));
        inMsg.setData(data);
        inMsg.setAttachments(attachments);
        return inMsg;
    }

    public IMListener getImListener() {
        return imListener;
    }

    public RoomListener getRoomListener() {
        return roomListener;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public ElementsListener getElementsListener() {
        return elementsListener;
    }

    public MessagesClient getMessagesClient() {
        return messagesClient;
    }

    public StreamsClient getStreamsClient() {
        return streamsClient;
    }

    public static String getMentionMLFromDisplayName(String displayName) {
        return String.format("<mention uid=\"%d\" />", getUserFromDisplayName(displayName).getUserId());
    }

    public static User getUserFromDisplayName(String displayName) {
        long userId = (new BigInteger(displayName.getBytes())).longValue();
        String firstName = displayName;
        String lastName = displayName;
        if (displayName.contains(" ")) {
            firstName = displayName.substring(0, displayName.indexOf(" "));
            lastName = displayName.substring(displayName.indexOf(" ") + 1);
        }
        User user = new User();
        user.setUserId(userId);
        user.setDisplayName(displayName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName + "@" + lastName + ".com");
        user.setUsername(firstName + "." + lastName);
        return user;
    }

    public static String getMentionsDataFromMessageText(String messageText) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mentionsNode = mapper.createObjectNode();

        Matcher m = mentionsPattern.matcher(messageText);
        while (m.find()) {
            User user = getUserFromDisplayName(m.group(2).trim());
            ArrayNode arrayNode = mapper.createArrayNode();
            ObjectNode idNode = mapper.createObjectNode();
            idNode.put("type", "com.symphony.user.userId");
            idNode.put("value", user.getUserId());
            arrayNode.add(idNode);
            ObjectNode mentionNode = mapper.createObjectNode();
            mentionNode.put("type", "com.symphony.user.mention");
            mentionNode.set("id", arrayNode);
            mentionsNode.set(mentionsNode.size() + "", mentionNode);
        }
        return mentionsNode.toString();
    }

    public static User getSampleUser() {
        User user = new User();
        user.setDisplayName("John Doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserId(11111111L);
        user.setEmail("john@doe.com");
        user.setUsername("john.doe");
        return user;
    }

    private static String getMLTemplate() {
        return TagBuilder.builder("div")
            .addField("data-format", "PresentationML")
            .addField("data-version", "2.0")
            .addField("class", "wysiwyg")
            .setContents("<p>%s</p>")
            .build();
    }
}
