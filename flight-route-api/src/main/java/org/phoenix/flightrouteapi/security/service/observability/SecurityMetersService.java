package org.phoenix.flightrouteapi.security.service.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class SecurityMetersService {

    public static final String INVALID_TOKENS_METER_EXPECTED_TAG = "cause";
    public static final String INVALID_TOKENS_METER_NAME = "security.authentication.invalid_tokens";
    public static final String INVALID_TOKENS_METER_DESCRIPTION = "Indicates the rate of invalid JWTs";
    public static final String INVALID_TOKENS_METER_BASE_UNIT = "errors";

    public static final String INVALID_TOKENS_METER_CAUSE_DIMENSION_INVALID_SIGNATURE = "invalid-signature";
    public static final String INVALID_TOKENS_METER_CAUSE_DIMENSION_EXPIRED = "expired";
    public static final String INVALID_TOKENS_METER_CAUSE_DIMENSION_UNSUPPORTED = "unsupported";
    public static final String INVALID_TOKENS_METER_CAUSE_DIMENSION_MALFORMED = "malformed";

    private final Counter tokenInvalidSignatureCounter;
    private final Counter tokenExpiredCounter;
    private final Counter tokenUnsupportedCounter;
    private final Counter tokenMalformedCounter;

    public SecurityMetersService(MeterRegistry registry) {
        this.tokenInvalidSignatureCounter = invalidTokensCounterForCauseBuilder(
                INVALID_TOKENS_METER_CAUSE_DIMENSION_INVALID_SIGNATURE).register(registry);
        this.tokenExpiredCounter = invalidTokensCounterForCauseBuilder(
                INVALID_TOKENS_METER_CAUSE_DIMENSION_EXPIRED).register(registry);
        this.tokenUnsupportedCounter = invalidTokensCounterForCauseBuilder(
                INVALID_TOKENS_METER_CAUSE_DIMENSION_UNSUPPORTED).register(registry);
        this.tokenMalformedCounter = invalidTokensCounterForCauseBuilder(
                INVALID_TOKENS_METER_CAUSE_DIMENSION_MALFORMED).register(registry);
    }

    private Counter.Builder invalidTokensCounterForCauseBuilder(String cause) {
        return Counter.builder(INVALID_TOKENS_METER_NAME)
                .baseUnit(INVALID_TOKENS_METER_BASE_UNIT)
                .description(INVALID_TOKENS_METER_DESCRIPTION)
                .tag(INVALID_TOKENS_METER_EXPECTED_TAG, cause);
    }

    public void trackTokenInvalidSignature() {
        this.tokenInvalidSignatureCounter.increment();
    }

    public void trackTokenExpired() {
        this.tokenExpiredCounter.increment();
    }

    public void trackTokenUnsupported() {
        this.tokenUnsupportedCounter.increment();
    }

    public void trackTokenMalformed() {
        this.tokenMalformedCounter.increment();
    }
}
