package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class H2ThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public H2ThemeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("THEME")
                .usingGeneratedKeyColumns("ID");
    }

    private Theme mapRowTheme(ResultSet rs, int rowNum) throws SQLException {
        return new Theme(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("THUMBNAIL")
        );
    }

    @Override
    public List<Theme> findAll() {
        final String sql = "SELECT * FROM THEME";

        return jdbcTemplate.query(sql, this::mapRowTheme);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = "SELECT * FROM THEME WHERE ID = ?";

        return jdbcTemplate.query(sql, this::mapRowTheme, id)
                .stream()
                .findAny();
    }

    @Override
    public Theme save(final Theme theme) {
        final SqlParameterSource params = new BeanPropertySqlParameterSource(theme);
        final Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return theme.assignId(id);
    }

    @Override
    public int delete(final Long id) {
        final String sql = "DELETE FROM THEME WHERE ID = ?";

        return jdbcTemplate.update(sql, id);
    }
}
