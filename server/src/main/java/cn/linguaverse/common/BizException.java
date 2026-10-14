package cn.linguaverse.common;

/** 业务异常：携带 HTTP 状态码与提示文案，由全局异常处理器转成统一响应。 */
public class BizException extends RuntimeException {
    private final int httpStatus;

    public BizException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public static BizException badRequest(String message) {
        return new BizException(400, message);
    }

    public static BizException unauthorized(String message) {
        return new BizException(401, message);
    }

    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }

    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    public static BizException conflict(String message) {
        return new BizException(409, message);
    }

    public static BizException tooManyRequests(String message) {
        return new BizException(429, message);
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
