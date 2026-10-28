package dev.midnightcoder.cafe.security.oauth;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-30
 */
@NullMarked
public interface OAuth2UserInfo {
    Map<String, Object> attributes();

    @Nullable
    String getId();

    @Nullable
    String getName();

    @Nullable
    String getEmail();

    @Nullable
    String getImageUrl();

    @Nullable
    String getPreferredIdentity();
}
