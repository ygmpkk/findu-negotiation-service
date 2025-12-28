# Demand Card Content Support

## Overview
Added support for parsing and handling `demand_card` type messages in the chat history content structure. This enables the system to extract structured demand information from IM messages containing demand cards.

## Implementation Details

### 1. Data Classes Added

#### `CustomContent`
Located in `ChatHistoryData.java`, this class represents the generic custom message content structure:
- `type`: The custom content type (e.g., "demand_card")
- `data`: JSON string containing the actual custom data
- `desc`: Description of the custom content

#### `DemandCardContent`
Located in `ChatHistoryData.java`, this class represents parsed demand card data:
- `type`: Always "demand_card"
- `version`: Version identifier (e.g., "im-demand-card-v2")
- `demandId` / `demand_id`: Unique demand identifier
- `nickname`: User's display name
- `avatar`: User's avatar URL
- `tag`: Optional tag information
- `subtitle`: Role description (e.g., "需求方")
- `demandTitle`: Title of the demand (e.g., "需求")
- `budget`: Budget information (e.g., "面议")
- `schedule`: Schedule/timing information
- `location`: Service location
- `rawDescription`: Detailed description as JSON string
- `source`: Origin of the demand (e.g., "ai_chat")
- `isAuto`: Whether automatically generated

### 2. Deserializer Updates

Updated `MessageContentDeserializer.java` to handle custom type messages:
- Detects messages with `type: "custom"`
- Parses the content as `CustomContent`
- If the custom content type is "demand_card", parses the nested `data` field as `DemandCardContent`
- Falls back to keeping raw `CustomContent` if parsing fails or for non-demand_card custom types

### 3. Helper Methods

Added convenience methods in `MessageContent` class:
- `getContentAsCustom()`: Returns content as CustomContent
- `getContentAsDemandCard()`: Returns content as DemandCardContent
- `isCustomContent()`: Checks if content is generic custom type
- `isDemandCardContent()`: Checks if content is specifically a demand_card

### 4. Business Logic Integration

Updated `NegotiationBizServiceImpl.java` to handle demand cards in conversation history:
- Extracts key information from demand cards (title, location, budget)
- Formats as readable text: "[需求卡片] {title}: 地点: {location}, 预算: {budget}"
- Includes in conversation history for negotiation context

## Message Structure

### Original Message Format
```json
{
  "type": "custom",
  "data": "{\"type\": \"demand_card\", \"version\": \"im-demand-card-v2\", ...}",
  "desc": "demand_card"
}
```

### MessageContent Wrapper Format
```json
{
  "type": "custom",
  "content": {
    "type": "demand_card",
    "data": "{\"type\": \"demand_card\", \"version\": \"im-demand-card-v2\", ...}",
    "desc": "demand_card"
  }
}
```

### Parsed DemandCardContent
The `data` field is parsed into a structured object with all demand card fields accessible via getter methods.

## Testing

Added comprehensive test coverage in `ChatHistoryDataTest.java`:

1. **testDemandCardContentDeserialization**: Tests parsing of complete demand card with all fields
2. **testCustomContentDeserialization**: Tests parsing of generic custom content (non-demand_card)

All existing tests continue to pass, ensuring backward compatibility.

## Usage Example

```java
// In message processing
for (var content : msg.getContent()) {
    if (content.isDemandCardContent()) {
        DemandCardContent demandCard = content.getContentAsDemandCard();
        String location = demandCard.getLocation();
        String budget = demandCard.getBudget();
        String title = demandCard.getDemandTitle();
        // Process demand card information...
    } else if (content.isCustomContent()) {
        CustomContent customContent = content.getContentAsCustom();
        // Handle other custom types...
    } else if (content.isTextContent()) {
        String text = content.getContentAsText();
        // Handle text messages...
    }
}
```

## Benefits

1. **Type Safety**: Structured objects instead of parsing JSON strings manually
2. **Extensibility**: Easy to add support for other custom message types
3. **Maintainability**: Clear separation of different content types
4. **Context Awareness**: Demand information now available in negotiation context
5. **Backward Compatibility**: Existing text and image handling unchanged

## Files Modified

1. `src/main/java/com/findu/negotiation/infrastructure/client/dto/chat/ChatHistoryData.java`
   - Added `CustomContent` class
   - Added `DemandCardContent` class
   - Added helper methods in `MessageContent`

2. `src/main/java/com/findu/negotiation/infrastructure/client/dto/chat/MessageContentDeserializer.java`
   - Updated deserializer to handle custom type
   - Added demand_card parsing logic

3. `src/main/java/com/findu/negotiation/application/NegotiationBizServiceImpl.java`
   - Updated conversation processing to extract demand card info

4. `src/test/java/com/findu/negotiation/infrastructure/client/dto/chat/ChatHistoryDataTest.java`
   - Added test for demand card deserialization
   - Added test for generic custom content deserialization

## Future Enhancements

1. Parse the `rawDescription` field into a structured object if needed frequently
2. Add support for other custom message types (e.g., service cards, product cards)
3. Extract additional metadata from `__imutils` field if needed
4. Add more sophisticated demand card information extraction in business logic

