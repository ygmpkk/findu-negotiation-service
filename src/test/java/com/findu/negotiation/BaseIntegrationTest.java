package com.findu.negotiation;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 基础集成测试类
 * <p>
 * 配置H2内存数据库，自动创建Schema
 *
 * @author timothy
 * @date 2026/01/25
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestDataManager testDataManager;

    @BeforeEach
    void setUp() {
        // 每个测试方法执行前的初始化
    }

    /**
     * 测试数据管理器
     */
    @Service
    public static class TestDataManager {
        private final AtomicLong counter = new AtomicLong(1);

        /**
         * 生成唯一的日历ID
         */
        public String generateCalendarId() {
            return "cal_" + counter.getAndIncrement();
        }

        /**
         * 生成唯一的日程ID
         */
        public String generateEventId() {
            return "evt_" + counter.getAndIncrement();
        }

        /**
         * 生成唯一的参与人ID
         */
        public String generateAttendeeId() {
            return "att_" + counter.getAndIncrement();
        }

        /**
         * 生成唯一的服务提供方ID
         */
        public String generateProviderId() {
            return "provider_" + counter.getAndIncrement();
        }

        /**
         * 生成唯一的客户ID
         */
        public String generateCustomerId() {
            return "customer_" + counter.getAndIncrement();
        }
    }
}
