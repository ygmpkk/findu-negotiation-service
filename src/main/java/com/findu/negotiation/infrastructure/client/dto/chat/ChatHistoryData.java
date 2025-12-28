package com.findu.negotiation.infrastructure.client.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryData {

    @JsonProperty("user_a")
    private String providerId;

    @JsonProperty("user_b")
    private String customerId;

    @JsonProperty("msg_count")
    private Integer msgCount;

    @JsonProperty("complete")
    private Boolean complete;

    @JsonProperty("last_msg_key")
    private String lastMsgKey;

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class ChatMessage {
        private String from;
        private String to;

        @JsonProperty("msg_time")
        private Long msgTime;

        @JsonProperty("msg_random")
        private Long msgRandom;

        @JsonProperty("msg_seq")
        private Long msgSeq;

        @JsonProperty("msg_key")
        private String msgKey;

        @JsonProperty("is_peer_read")
        private Integer isPeerRead;

        private List<MessageContent> content;
    }

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonDeserialize(using = MessageContentDeserializer.class)
    static public class MessageContent {
        @JsonProperty("type")
        private String type;

        @JsonProperty("content")
        private Object content; // Can be String for text or ImageContent for image

        /**
         * Get content as String (for text type)
         */
        public String getContentAsText() {
            if (content instanceof String) {
                return (String) content;
            }
            return null;
        }

        /**
         * Get content as ImageContent (for image type)
         */
        public ImageContent getContentAsImage() {
            if (content instanceof ImageContent) {
                return (ImageContent) content;
            }
            return null;
        }

        /**
         * Get content as CustomContent (for custom type)
         */
        public CustomContent getContentAsCustom() {
            if (content instanceof CustomContent) {
                return (CustomContent) content;
            }
            return null;
        }

        /**
         * Get content as DemandCardContent (for demand_card type within custom)
         */
        public DemandCardContent getContentAsDemandCard() {
            if (content instanceof DemandCardContent) {
                return (DemandCardContent) content;
            }
            return null;
        }

        /**
         * Check if content is text type
         */
        public boolean isTextContent() {
            return "text".equals(type) && content instanceof String;
        }

        /**
         * Check if content is image type
         */
        public boolean isImageContent() {
            return "image".equals(type) && content instanceof ImageContent;
        }

        /**
         * Check if content is custom type
         */
        public boolean isCustomContent() {
            return "custom".equals(type) && content instanceof CustomContent;
        }

        /**
         * Check if content is demand_card type
         */
        public boolean isDemandCardContent() {
            return "custom".equals(type) && content instanceof DemandCardContent;
        }
    }

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class ImageContent {
        @JsonProperty("UUID")
        private String uuid;

        @JsonProperty("ImageFormat")
        private Integer imageFormat;

        @JsonProperty("ImageInfoArray")
        private List<ImageInfo> imageInfoArray;
    }

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class ImageInfo {
        @JsonProperty("Type")
        private Integer type;

        @JsonProperty("Size")
        private Integer size;

        @JsonProperty("Width")
        private Integer width;

        @JsonProperty("Height")
        private Integer height;

        @JsonProperty("URL")
        private String url;
    }

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class CustomContent {
        @JsonProperty("type")
        private String type;

        @JsonProperty("data")
        private String data; // JSON string for custom data

        @JsonProperty("desc")
        private String desc;
    }

    @ToString
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class DemandCardContent {
        @JsonProperty("type")
        private String type;

        @JsonProperty("version")
        private String version;

        @JsonProperty("demandId")
        private String demandId;

        @JsonProperty("demand_id")
        private String demandIdAlt;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("avatar")
        private String avatar;

        @JsonProperty("tag")
        private String tag;

        @JsonProperty("subtitle")
        private String subtitle;

        @JsonProperty("demandTitle")
        private String demandTitle;

        @JsonProperty("budget")
        private String budget;

        @JsonProperty("schedule")
        private String schedule;

        @JsonProperty("location")
        private String location;

        @JsonProperty("rawDescription")
        private String rawDescription;

        @JsonProperty("source")
        private String source;

        @JsonProperty("isAuto")
        private Boolean isAuto;
    }
}
