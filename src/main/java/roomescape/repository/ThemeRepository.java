package roomescape.repository;

import roomescape.domain.Theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    List<Theme> findAll();

    Optional<Theme> findById(long id);

    Theme save(Theme theme);

    void deleteById(long id);
}
