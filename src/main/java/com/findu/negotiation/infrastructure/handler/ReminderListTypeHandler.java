package com.findu.negotiation.infrastructure.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReminderVO List JSON 类型处理器
 * <p>
 * 用于 MyBatis 在 ReminderVO 列表对象和数据库 JSON/VARCHAR 类型之间进行转换
 *
 * @author timothy
 * @date 2026/01/25
 */
@MappedTypes({List.class, Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ReminderListTypeHandler extends BaseTypeHandler<List<ReminderVO>> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<ReminderVO> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Error converting ReminderVO list to JSON", e);
        }
    }

    @Override
    public List<ReminderVO> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<ReminderVO> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<ReminderVO> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<ReminderVO> parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ReminderVO>>() {});
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to ReminderVO list", e);
        }
    }
}
