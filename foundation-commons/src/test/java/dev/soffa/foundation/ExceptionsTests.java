package dev.soffa.foundation;

import dev.soffa.foundation.errors.*;
import dev.soffa.foundation.models.ResponseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionsTests {

    private static final String ERROR = "error";

    @Test
    public void testExceptions() {
        assertEquals(ResponseStatus.SERVER_ERROR, ErrorUtil.resolveErrorCode(new ConfigurationException(ERROR)));
        assertEquals(ResponseStatus.SERVER_ERROR, ErrorUtil.resolveErrorCode(new ConfigurationException(ERROR, (Throwable) null)));

        assertEquals(ResponseStatus.CONFLICT, ErrorUtil.resolveErrorCode(new ConflictException(ERROR)));
        assertEquals(ResponseStatus.CONFLICT, ErrorUtil.resolveErrorCode(new ConflictException(ERROR, (Throwable) null)));

        assertEquals(ResponseStatus.SERVER_ERROR, ErrorUtil.resolveErrorCode(new DatabaseException(ERROR)));
        assertEquals(ResponseStatus.SERVER_ERROR, ErrorUtil.resolveErrorCode(new DatabaseException(ERROR, (Throwable) null)));

        assertEquals(-1, ErrorUtil.resolveErrorCode(new FakeException(ERROR)));
        assertEquals(ResponseStatus.FORBIDDEN, ErrorUtil.resolveErrorCode(new ForbiddenException(ERROR)));
        assertEquals(ResponseStatus.BAD_REQUEST, ErrorUtil.resolveErrorCode(new FunctionalException(ERROR)));
        assertEquals(ResponseStatus.UNAUTHORIZED, ErrorUtil.resolveErrorCode(new InvalidAuthException(ERROR)));
        assertEquals(ResponseStatus.BAD_REQUEST, ErrorUtil.resolveErrorCode(new InvalidTenantException(ERROR)));
        assertEquals(ResponseStatus.UNAUTHORIZED, ErrorUtil.resolveErrorCode(new InvalidTokenException(ERROR)));
        assertEquals(ResponseStatus.NO_CONTENT, ErrorUtil.resolveErrorCode(new NoContentException(ERROR)));
        assertEquals(ResponseStatus.NOT_IMLEMENTED, ErrorUtil.resolveErrorCode(new NotImplementedException(ERROR)));
        assertEquals(ResponseStatus.EXPECTATION_FAILED, ErrorUtil.resolveErrorCode(new RequirementException(ERROR)));
        assertEquals(ResponseStatus.NOT_FOUND, ErrorUtil.resolveErrorCode(new ResourceNotFoundException(ERROR)));
        assertEquals(ResponseStatus.BAD_REQUEST, ErrorUtil.resolveErrorCode(new RetryException(ERROR)));
        assertEquals(ResponseStatus.SERVER_ERROR, ErrorUtil.resolveErrorCode(new TechnicalException(ERROR)));
        assertEquals(ResponseStatus.REQUEST_TIMEOUT, ErrorUtil.resolveErrorCode(new TimeoutException(ERROR)));
        assertEquals(ResponseStatus.NOT_IMLEMENTED, ErrorUtil.resolveErrorCode(new TodoException(ERROR)));
        assertEquals(ResponseStatus.UNAUTHORIZED, ErrorUtil.resolveErrorCode(new UnauthorizedException(ERROR)));
        assertEquals(ResponseStatus.BAD_REQUEST, ErrorUtil.resolveErrorCode(new ValidationException(ERROR)));
    }


}
