package cn.linguaverse.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** JWT：签发/解析往返、篡改拒绝、异密钥拒绝、过期拒绝。 */
class JwtServiceTest {

    private static final String SECRET =
            "linguaverse-dev-secret-please-change-in-prod-0123456789abcdef";
    private static final String OTHER_SECRET =
            "another-secret-key-for-unit-testing-purposes-0123456789abcdef";

    private final JwtService service = new JwtService(SECRET, 7);

    @Test
    @DisplayName("签发 → 解析 userId 往返一致")
    void issueAndParseRoundTrip() {
        String token = service.issue(42L, "demo", "USER");
        assertThat(service.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("篡改签名 → 拒绝")
    void tamperedTokenRejected() {
        String token = service.issue(42L, "demo", "USER");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThatThrownBy(() -> service.parseUserId(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("异密钥签发 → 拒绝")
    void foreignKeyTokenRejected() {
        String foreign = new JwtService(OTHER_SECRET, 7).issue(42L, "demo", "USER");
        assertThatThrownBy(() -> service.parseUserId(foreign))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("过期 token → 拒绝")
    void expiredTokenRejected() {
        String expired = new JwtService(SECRET, -1).issue(42L, "demo", "USER");
        assertThatThrownBy(() -> service.parseUserId(expired))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("畸形 token → 拒绝")
    void malformedTokenRejected() {
        assertThatThrownBy(() -> service.parseUserId("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }
}
