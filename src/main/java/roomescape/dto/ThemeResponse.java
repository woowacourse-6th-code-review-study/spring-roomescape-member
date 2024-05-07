package roomescape.dto;

import roomescape.domain.Theme;

public record ThemeResponse(
        Long themeId,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName().value(),
                theme.getDescription().value(),
                theme.getThumbnail().value()
        );
    }
}
