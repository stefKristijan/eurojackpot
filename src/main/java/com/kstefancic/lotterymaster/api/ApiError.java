package com.kstefancic.lotterymaster.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * Provides detailed information about a failure error occurred
 * while consuming APIs.
 *
 * Details contains:
 *
 * {@code code}: identifies the code of failure
 * {@code message}: a human readable message that explains the reason of the failure
 * {@code details}: provides additional information about the failure (eg for debugging purposes)
 */
@JsonPropertyOrder({"code", "message", "details"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {
    private final String code;
    private final String message;
    private final String details;

    public ApiError(String code, String message, String details) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.details = details;
    }

    /**
     * Return the error code.
     *
     * @return the error code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns detailed information about the error.
     *
     * @return detailed information about the error.
     */
    public String getDetails() {
        return details;
    }

}
