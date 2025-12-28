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
        } else if ("custom".equals(type)) {
            // For custom type, first parse as CustomContent
            ChatHistoryData.CustomContent customContent = mapper.treeToValue(contentNode, ChatHistoryData.CustomContent.class);

            // Check if the custom content is a demand_card
            if ("demand_card".equals(customContent.getType())) {
                try {
                    // Parse the data field as DemandCardContent
                    ChatHistoryData.DemandCardContent demandCardContent = mapper.readValue(
                        customContent.getData(),
                        ChatHistoryData.DemandCardContent.class
                    );
                    messageContent.setContent(demandCardContent);
                } catch (Exception e) {
                    // If parsing fails, keep the CustomContent with raw data string
                    messageContent.setContent(customContent);
                }
            } else {
                // For other custom types, keep as CustomContent
                messageContent.setContent(customContent);
            }
        } else {
            // For other types, keep as JsonNode or String
            messageContent.setContent(contentNode.toString());
        }

        return messageContent;
    }
}

