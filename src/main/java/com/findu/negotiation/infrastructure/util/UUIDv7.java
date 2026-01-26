package com.findu.negotiation.infrastructure.util;

import java.util.Random;
import java.util.UUID;

/**
 * UUID v7 生成器
 * <p>
 * UUID v7 是 RFC 9562 定义的基于时间戳的 UUID，具有以下特点：
 * - 时间可排序
 * - 去重且唯一
 * - 适合分布式系统
 * <p>
 * 格式：XXXXXXXX-XXXX-7XXX-XXXX-XXXXXXXXXXXX
 * - 前48位：Unix 时间戳（毫秒）
 * - 第4个字符：版本号 7
 * - 后74位：随机序列
 *
 * @author timothy
 * @date 2026/01/25
 */
public final class UUIDv7 {

    private static final Random RANDOM = new Random();
    // 用于同一毫秒内递增的计数器，防止重复
    private static long lastTimestamp = 0;
    private static int counter = 0;

    private UUIDv7() {
        // 工具类，禁止实例化
    }

    /**
     * 生成 UUID v7 字符串（无连字符）
     *
     * @return 32位十六进制字符串
     */
    public static String generate() {
        return generateUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID v7 对象
     *
     * @return UUID 对象
     */
    public static UUID generateUUID() {
        final long timestamp = System.currentTimeMillis();

        // 处理同一毫秒内的并发
        final int sequence;
        synchronized (UUIDv7.class) {
            if (timestamp == lastTimestamp) {
                counter = (counter + 1) & 0x0FFF; // 12位计数器
                if (counter == 0) {
                    // 计数器溢出，等待下一毫秒
                    while (timestamp == System.currentTimeMillis()) {
                        // 忙等待
                    }
                }
            } else {
                counter = 0;
                lastTimestamp = timestamp;
            }
            sequence = counter;
        }

        // UUID v7 布局：
        // time_high (32 bits) | time_mid (16 bits) | ver (4 bits) | time_seq (12 bits) | var (2 bits) | rand (62 bits)
        //
        // 00000000000000000000000000000000-000000000000-0xxx-xxxx-xxxxxxxxxxxx
        // |------- timestamp (48 bits) ------|ver|seq (12)|var|--- random --|

        final long timeHigh = timestamp >>> 16;           // 时间戳高32位
        final long timeMid = timestamp & 0xFFFF;          // 时间戳低16位
        final long timeSeq = (sequence & 0x0FFF);         // 12位序列

        final long mostSigBits = (timeHigh << 32)
                | (timeMid << 16)
                | 0x7000          // 版本号 7
                | timeSeq;        // 12位序列

        final long leastSigBits = RANDOM.nextLong();     // 64位随机数

        // 设置变体位（RFC 9562 规定变体为 10xx）
        // leastSigBits 的最高两位应该是 10
        final long variantBits = (leastSigBits & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(mostSigBits, variantBits);
    }
}
