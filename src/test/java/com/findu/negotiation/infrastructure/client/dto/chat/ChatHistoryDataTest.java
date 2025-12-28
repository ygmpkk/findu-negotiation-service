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

    @Test
    void testDemandCardContentDeserialization() throws Exception {
        String json = """
                {
                    "type": "custom",
                    "content": {
                        "type": "demand_card",
                        "data": "{\\"type\\": \\"demand_card\\", \\"version\\": \\"im-demand-card-v2\\", \\"demandId\\": \\"20824794-09fe-4556-abe9-4186aa45baa7-18e5\\", \\"demand_id\\": \\"20824794-09fe-4556-abe9-4186aa45baa7-18e5\\", \\"nickname\\": \\"Timothy\\", \\"avatar\\": \\"https://findu-media.oss-cn-hangzhou.aliyuncs.com/profile/avatar/693ffe6855aa7641415118e5_20251219095123_5fa8f4f805c3.jpg\\", \\"tag\\": \\"\\", \\"subtitle\\": \\"需求方\\", \\"demandTitle\\": \\"需求\\", \\"budget\\": \\"面议\\", \\"schedule\\": \\"\\", \\"location\\": \\"杭州市西湖区浙大附近余杭塘河绿道\\", \\"rawDescription\\": \\"{\\\\\\"service\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"需求描述\\\\\\", \\\\\\"value\\\\\\": \\\\\\"杭州马拉松周期性训练（冲刺PB）\\\\\\"}, \\\\\\"industry\\\\\\": {\\\\\\"Running Buddy\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"跑步陪跑\\\\\\", \\\\\\"value\\\\\\": \\\\\\"马拉松备赛，目标冲刺PB，当前半马PB 1:52\\\\\\"}, \\\\\\"AdditionalInformation\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"补充信息\\\\\\", \\\\\\"value\\\\\\": \\\\\\"需要针对性的周期化系统训练计划\\\\\\"}}, \\\\\\"expectedPrice\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"期望价格\\\\\\", \\\\\\"value\\\\\\": \\\\\\"面议\\\\\\"}, \\\\\\"serviceMethod\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"服务位置\\\\\\", \\\\\\"value\\\\\\": \\\\\\"线下\\\\\\"}, \\\\\\"serviceLocation\\\\\\": {\\\\\\"name\\\\\\": \\\\\\"服务地区\\\\\\", \\\\\\"value\\\\\\": \\\\\\"杭州市西湖区浙大附近余杭塘河绿道\\\\\\"}}\\", \\"source\\": \\"ai_chat\\", \\"isAuto\\": true}",
                        "desc": "demand_card"
                    }
                }
                """;

        ChatHistoryData.MessageContent messageContent = objectMapper.readValue(json, ChatHistoryData.MessageContent.class);

        assertEquals("custom", messageContent.getType());
        assertTrue(messageContent.isDemandCardContent());
        assertFalse(messageContent.isTextContent());
        assertFalse(messageContent.isImageContent());

        ChatHistoryData.DemandCardContent demandCard = messageContent.getContentAsDemandCard();
        assertNotNull(demandCard);
        assertEquals("demand_card", demandCard.getType());
        assertEquals("im-demand-card-v2", demandCard.getVersion());
        assertEquals("20824794-09fe-4556-abe9-4186aa45baa7-18e5", demandCard.getDemandId());
        assertEquals("20824794-09fe-4556-abe9-4186aa45baa7-18e5", demandCard.getDemandIdAlt());
        assertEquals("Timothy", demandCard.getNickname());
        assertEquals("https://findu-media.oss-cn-hangzhou.aliyuncs.com/profile/avatar/693ffe6855aa7641415118e5_20251219095123_5fa8f4f805c3.jpg", demandCard.getAvatar());
        assertEquals("需求方", demandCard.getSubtitle());
        assertEquals("需求", demandCard.getDemandTitle());
        assertEquals("面议", demandCard.getBudget());
        assertEquals("杭州市西湖区浙大附近余杭塘河绿道", demandCard.getLocation());
        assertEquals("ai_chat", demandCard.getSource());
        assertTrue(demandCard.getIsAuto());
        assertNotNull(demandCard.getRawDescription());
    }

    @Test
    void testCustomContentDeserialization() throws Exception {
        String json = """
                {
                    "type": "custom",
                    "content": {
                        "type": "other_custom_type",
                        "data": "{\\"someField\\": \\"someValue\\"}",
                        "desc": "other_custom"
                    }
                }
                """;

        ChatHistoryData.MessageContent messageContent = objectMapper.readValue(json, ChatHistoryData.MessageContent.class);

        assertEquals("custom", messageContent.getType());
        assertTrue(messageContent.isCustomContent());
        assertFalse(messageContent.isDemandCardContent());
        assertFalse(messageContent.isTextContent());
        assertFalse(messageContent.isImageContent());

        ChatHistoryData.CustomContent customContent = messageContent.getContentAsCustom();
        assertNotNull(customContent);
        assertEquals("other_custom_type", customContent.getType());
        assertEquals("other_custom", customContent.getDesc());
        assertNotNull(customContent.getData());
    }
}

