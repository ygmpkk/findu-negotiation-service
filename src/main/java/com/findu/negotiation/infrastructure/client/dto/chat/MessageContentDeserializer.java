package com.findu.negotiation.infrastructure.client.dto.chat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MessageContentDeserializer extends JsonDeserializer<ChatHistoryData.MessageContent> {

    @Override
    public ChatHistoryData.MessageContent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ChatHistoryData.MessageContent messageContent = new ChatHistoryData.MessageContent();

        // Get the type field
        String type = node.get("type").asText();
        messageContent.setType(type);

        // Deserialize content based on type
        JsonNode contentNode = node.get("content");
        if ("text".equals(type)) {
            // For text type, content is a String
            messageContent.setContent(contentNode.asText());
        } else if ("image".equals(type)) {
            // For image type, content is an ImageContent object
            ChatHistoryData.ImageContent imageContent = mapper.treeToValue(contentNode, ChatHistoryData.ImageContent.class);
            messageContent.setContent(imageContent);
        } else {
            // For other types, keep as JsonNode or String
            messageContent.setContent(contentNode.toString());
        }

        return messageContent;
    }
}

