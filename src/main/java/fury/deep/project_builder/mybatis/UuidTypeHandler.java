package fury.deep.project_builder.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.UUID;

/**
 * MyBatis type handler for {@link UUID} ↔ PostgreSQL {@code uuid}.
 * <p>
 * Without this, MyBatis binds UUIDs as plain strings and PostgreSQL rejects
 * them when the target column or cast is typed {@code uuid}.  This handler
 * uses {@link PreparedStatement#setObject(int, Object)} with the explicit
 * JDBC type code for UUID (1111 = OTHER), which the PostgreSQL JDBC driver
 * understands natively.
 * <p>
 * Register via mybatis.type-handlers-package in application.yml — the
 * package is already set to fury.deep.project_builder.mybatis so no extra
 * config is required.
 */
@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID uuid, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, uuid, Types.OTHER);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String val = rs.getString(columnName);
        return val == null ? null : UUID.fromString(val);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return val == null ? null : UUID.fromString(val);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String val = cs.getString(columnIndex);
        return val == null ? null : UUID.fromString(val);
    }
}