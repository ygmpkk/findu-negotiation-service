# MessageContent 多类型支持实现说明

## 概述
`MessageContent` 的 `content` 字段现在支持两种类型的数据：
1. **String** - 用于文本类型消息
2. **ImageContent** - 用于图片类型消息

## 实现细节

### 1. 数据结构

#### MessageContent
```java
@JsonDeserialize(using = MessageContentDeserializer.class)
static public class MessageContent {
    private String type;           // "text" 或 "image"
    private Object content;        // String 或 ImageContent
}
```

#### ImageContent
```java
static public class ImageContent {
    private String uuid;           // 图片UUID
    private Integer imageFormat;   // 图片格式
    private List<ImageInfo> imageInfoArray;  // 图片信息数组
}
```

#### ImageInfo
```java
static public class ImageInfo {
    private Integer type;    // 图片类型 (1: 原图, 2: 大图, 3: 缩略图)
    private Integer size;    // 文件大小(字节)
    private Integer width;   // 宽度(像素)
    private Integer height;  // 高度(像素)
    private String url;      // 图片URL
}
```

### 2. 自定义反序列化器

创建了 `MessageContentDeserializer` 来处理多态的 `content` 字段：

```java
public class MessageContentDeserializer extends JsonDeserializer<ChatHistoryData.MessageContent> {
    @Override
    public ChatHistoryData.MessageContent deserialize(JsonParser p, DeserializationContext ctxt) {
        // 根据 type 字段决定如何反序列化 content
        if ("text".equals(type)) {
            // 将 content 反序列化为 String
        } else if ("image".equals(type)) {
            // 将 content 反序列化为 ImageContent
        }
    }
}
```

### 3. 辅助方法

为了方便使用，`MessageContent` 提供了以下辅助方法：

```java
// 获取文本内容（当 type 为 "text" 时）
String getContentAsText()

// 获取图片内容（当 type 为 "image" 时）
ImageContent getContentAsImage()

// 检查是否为文本类型
boolean isTextContent()

// 检查是否为图片类型
boolean isImageContent()
```

## 使用示例

### 1. 文本消息

**JSON 格式：**
```json
{
    "type": "text",
    "content": "你好"
}
```

**使用方法：**
```java
MessageContent msg = ...;
if (msg.isTextContent()) {
    String text = msg.getContentAsText();
    System.out.println("文本内容: " + text);
}
```

### 2. 图片消息

**JSON 格式：**
```json
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
            },
            {
                "Type": 2,
                "Size": 0,
                "Width": 960,
                "Height": 720,
                "URL": "https://example.com/image_large.jpg"
            },
            {
                "Type": 3,
                "Size": 0,
                "Width": 264,
                "Height": 198,
                "URL": "https://example.com/image_thumb.jpg"
            }
        ]
    }
}
```

**使用方法：**
```java
MessageContent msg = ...;
if (msg.isImageContent()) {
    ImageContent image = msg.getContentAsImage();
    System.out.println("图片UUID: " + image.getUuid());
    System.out.println("图片格式: " + image.getImageFormat());
    
    for (ImageInfo info : image.getImageInfoArray()) {
        System.out.println("类型: " + info.getType());
        System.out.println("大小: " + info.getSize());
        System.out.println("尺寸: " + info.getWidth() + "x" + info.getHeight());
        System.out.println("URL: " + info.getUrl());
    }
}
```

### 3. 通用处理

```java
MessageContent msg = ...;
switch (msg.getType()) {
    case "text":
        String text = msg.getContentAsText();
        // 处理文本消息
        break;
    case "image":
        ImageContent image = msg.getContentAsImage();
        // 处理图片消息
        break;
    default:
        // 处理其他类型
        break;
}
```

## 修改的文件

1. **ChatHistoryData.java** - 添加了 `ImageContent` 和 `ImageInfo` 类，更新了 `MessageContent` 类
2. **MessageContentDeserializer.java** - 新增的自定义反序列化器
3. **ChatClient.java** - 更新为使用 Jackson ObjectMapper 进行反序列化
4. **ChatHistoryDataTest.java** - 新增单元测试验证功能

## 测试

已通过单元测试验证：
- ✅ 文本消息正确反序列化
- ✅ 图片消息正确反序列化
- ✅ 辅助方法正常工作
- ✅ 类型检查正常工作

运行测试：
```bash
mvn test -Dtest=ChatHistoryDataTest
```

## 兼容性

- ✅ 向后兼容：已有的文本消息处理不受影响
- ✅ 类型安全：使用 instanceof 和辅助方法确保类型安全
- ✅ 扩展性：可以轻松添加更多消息类型（如视频、音频等）

