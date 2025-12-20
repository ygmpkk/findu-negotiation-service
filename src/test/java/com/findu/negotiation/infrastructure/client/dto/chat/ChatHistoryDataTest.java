package com.findu.negotiation.infrastructure.client.dto.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatHistoryDataTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testTextContentDeserialization() throws Exception {
        String json = """
                {
                    "type": "text",
                    "content": "你好"
                }
                """;

        ChatHistoryData.MessageContent messageContent = objectMapper.readValue(json, ChatHistoryData.MessageContent.class);

        assertEquals("text", messageContent.getType());
        assertTrue(messageContent.isTextContent());
        assertFalse(messageContent.isImageContent());
        assertEquals("你好", messageContent.getContentAsText());
        assertNull(messageContent.getContentAsImage());
    }

    @Test
    void testImageContentDeserialization() throws Exception {
        String json = """
                {
                    "type": "image",
                    "content": {
                        "UUID": "1600102361-68d63538cddf59418aab1093-4l6hOAxJgMOmGrjSZ3UPu1phArT8Xcls.jpg",
                        "ImageFormat": 1,
                        "ImageInfoArray": [
                            {
                                "Type": 1,
                                "Size": 217894,
                                "Width": 1080,
                                "Height": 810,
                                "URL": "https://example.com/image.jpg"
                            }
                        ]
                    }
                }
                """;

        ChatHistoryData.MessageContent messageContent = objectMapper.readValue(json, ChatHistoryData.MessageContent.class);

        assertEquals("image", messageContent.getType());
        assertTrue(messageContent.isImageContent());
        assertFalse(messageContent.isTextContent());
        assertNull(messageContent.getContentAsText());

        ChatHistoryData.ImageContent imageContent = messageContent.getContentAsImage();
        assertNotNull(imageContent);
        assertEquals("1600102361-68d63538cddf59418aab1093-4l6hOAxJgMOmGrjSZ3UPu1phArT8Xcls.jpg", imageContent.getUuid());
        assertEquals(1, imageContent.getImageFormat());
        assertEquals(1, imageContent.getImageInfoArray().size());

        ChatHistoryData.ImageInfo imageInfo = imageContent.getImageInfoArray().get(0);
        assertEquals(1, imageInfo.getType());
        assertEquals(217894, imageInfo.getSize());
        assertEquals(1080, imageInfo.getWidth());
        assertEquals(810, imageInfo.getHeight());
        assertEquals("https://example.com/image.jpg", imageInfo.getUrl());
    }
}

