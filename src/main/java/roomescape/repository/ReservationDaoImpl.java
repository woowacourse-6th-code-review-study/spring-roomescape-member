package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class ReservationDaoImpl implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            new ReservationTime(resultSet.getLong("time_id"), resultSet.getTime("time_value").toLocalTime()),
            new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"), resultSet.getString("theme_thumbnail"))
    );

    private RowMapper<ReservationTime> timeRowMapper = ((rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    ));
    private RowMapper<Theme> themeRowMapper = ((rs, rowNum) -> new Theme(
            rs.getLong("theme_id"),
            rs.getString("theme_name"),
            rs.getString("theme_description"),
            rs.getString("theme_thumbnail")
    ));

    public ReservationDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("RESERVATION")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT"
                + "    r.id as reservation_id, "
                + "    r.name, "
                + "    r.date, "
                + "    t.id as time_id, "
                + "    t.start_at as time_value , "
                + "    theme.id as theme_id, "
                + "    theme.name as theme_name, "
                + "    theme.description as theme_description, "
                + "    theme.thumbnail as theme_thumbnail "
                + "FROM reservation as r "
                + "INNER JOIN reservation_time as t "
                + "ON r.time_id = t.id "
                + "INNER JOIN theme "
                + "ON r.theme_id = theme.id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = "SELECT"
                + "    r.id as reservation_id, "
                + "    r.name, "
                + "    r.date, "
                + "    t.id as time_id, "
                + "    t.start_at as time_value, "
                + "    theme.id as theme_id, "
                + "    theme.name as theme_name, "
                + "    theme.description as theme_description, "
                + "    theme.thumbnail as theme_thumbnail "
                + "FROM reservation as r "
                + "INNER JOIN reservation_time as t "
                + "ON r.time_id = t.id "
                + "INNER JOIN theme "
                + "ON r.theme_id = theme.id "
                + "WHERE r.id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public Reservation insert(Reservation reservation) {
        Map<String, Object> reservationRow = new HashMap<>();
        reservationRow.put("name", reservation.getName());
        reservationRow.put("date", reservation.getDate());
        reservationRow.put("time_id", reservation.getTimeId());
        reservationRow.put("theme_id", reservation.getThemeId());
        Long id = simpleJdbcInsert.executeAndReturnKey(reservationRow).longValue();
        return new Reservation(id, reservation.getName(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = "select exists ( "
                + "    select 1 "
                + "    from reservation "
                + "    where date = ? and time_id = ? and theme_id = ?"
                + ")";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public List<ReservationTime> findByDateAndTheme(LocalDate date, Long themeId) {
        String sql = "select t.id, t.start_at "
                + "   from reservation as r "
                + "   inner join reservation_time as t "
                + "   on r.time_id = t.id "
                + "   where date = ? and theme_id = ?";
        return jdbcTemplate.query(sql, timeRowMapper, date, themeId);
    }

    @Override
    public List<Theme> findThemeOrderByReservationCount() {
        String sql = "select "
                + "   th.id as theme_id, "
                + "   th.name as theme_name, "
                + "   th.description as theme_description, "
                + "   th.thumbnail as theme_thumbnail, "
                + "   count(th.id) as reservation_count "
                + "   from reservation as r "
                + "   inner join theme as th "
                + "   on r.theme_id = th.id "
                + "   where r.date between dateadd('day', -7, current_date()) and dateadd('day', -1, current_date()) "
                + "   group by th.id "
                + "   order by reservation_count desc "
                + "   limit 10";

        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from reservation where id = ?", id);
    }
}
