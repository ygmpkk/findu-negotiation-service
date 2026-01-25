package com.findu.negotiation.infrastructure.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.findu.negotiation.domain.calendar.vo.LocationVO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LocationVO JSON 类型处理器
 * <p>
 * 用于 MyBatis 在 LocationVO 对象和数据库 JSON/VARCHAR 类型之间进行转换
 *
 * @author timothy
 * @date 2026/01/25
 */
@MappedTypes(LocationVO.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class LocationTypeHandler extends BaseTypeHandler<LocationVO> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocationVO parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Error converting LocationVO to JSON", e);
        }
    }

    @Override
    public LocationVO getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public LocationVO getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public LocationVO getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private LocationVO parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, LocationVO.class);
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to LocationVO", e);
        }
    }
}
