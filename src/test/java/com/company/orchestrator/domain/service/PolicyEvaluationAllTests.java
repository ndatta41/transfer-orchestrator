package com.company.orchestrator.domain.service;

import com.company.orchestrator.policy.AtomicPolicy;
import com.company.orchestrator.policy.CompositePolicy;
import com.company.orchestrator.policy.Policy;
import com.company.orchestrator.policy.PolicyContext;
import com.company.orchestrator.policy.PolicyEvaluationResult;
import com.company.orchestrator.policy.atomic.*;
import com.company.orchestrator.policy.composite.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated unit tests for:
 * - PolicyEvaluationService
 * - All Atomic Policies
 * - All Composite Policies
 */
class PolicyEvaluationAllTests {

    private static PolicyContext baseContext() {
        return new PolicyContext(
                "consumer-1",
                "provider-1",
                "DATA",
                "EU",
                Set.of("ISO_9001"),
                "QUALITY_ANALYSIS",
                Instant.parse("2025-01-01T10:00:00Z"),
                ZoneId.of("UTC"),
                5
        );
    }

    @Nested
    class PolicyEvaluationServiceTests {

        @Test
        void evaluate_delegatesAndReturnsDefensiveCopy() {
            PolicyEvaluationService service = new PolicyEvaluationService();

            Policy policy = mock(AtomicPolicy.class);
            PolicyContext ctx = baseContext();

            PolicyEvaluationResult original =
                    new PolicyEvaluationResult(false, "DENIED");

            when(policy.evaluate(ctx)).thenReturn(original);

            PolicyEvaluationResult result =
                    service.evaluate(policy, ctx);

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason()).isEqualTo("DENIED");
            assertThat(result).isNotSameAs(original);

            verify(policy).evaluate(ctx);
        }
    }

    @Nested
    class CertificationPolicyTests {

        @Test
        void allowsWhenCertificationPresent() {
            CertificationPolicy policy =
                    new CertificationPolicy("ISO_9001");

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesWhenCertificationMissing() {
            CertificationPolicy policy =
                    new CertificationPolicy("SOC_2");

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("Missing required certification");
        }
    }

    @Nested
    class GeographicPolicyTests {

        @Test
        void allowsEuRegion() {
            GeographicPolicy policy = new GeographicPolicy();

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesNonEuRegion() {
            PolicyContext ctx = new PolicyContext(
                    "c", "p", "d",
                    "US",
                    Set.of(),
                    "USE",
                    Instant.now(),
                    ZoneId.of("UTC"),
                    0
            );

            PolicyEvaluationResult result =
                    new GeographicPolicy().evaluate(ctx);

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("outside EU");
        }
    }

    @Nested
    class RateLimitPolicyTests {

        @Test
        void allowsBelowLimit() {
            RateLimitPolicy policy =
                    new RateLimitPolicy(10);

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesAboveLimit() {
            PolicyContext ctx = new PolicyContext(
                    "c", "p", "d",
                    "EU",
                    Set.of(),
                    "USE",
                    Instant.now(),
                    ZoneId.of("UTC"),
                    50
            );

            PolicyEvaluationResult result =
                    new RateLimitPolicy(10).evaluate(ctx);

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("Rate limit exceeded");
        }
    }

    @Nested
    class TimeBasedPolicyTests {

        @Test
        void allowsWithinBusinessHours() {
            TimeBasedPolicy policy =
                    new TimeBasedPolicy(
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0),
                            ZoneId.of("UTC")
                    );

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesOutsideBusinessHours() {
            PolicyContext ctx = new PolicyContext(
                    "c", "p", "d",
                    "EU",
                    Set.of(),
                    "USE",
                    Instant.parse("2025-01-01T23:00:00Z"),
                    ZoneId.of("UTC"),
                    1
            );

            TimeBasedPolicy policy =
                    new TimeBasedPolicy(
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0),
                            ZoneId.of("UTC")
                    );

            PolicyEvaluationResult result =
                    policy.evaluate(ctx);

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("business hours");
        }
    }

    @Nested
    class UsagePolicyTests {

        @Test
        void allowsMatchingPurposeIgnoringCase() {
            UsagePolicy policy =
                    new UsagePolicy("QUALITY_ANALYSIS");

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesDifferentPurpose() {
            PolicyContext ctx = new PolicyContext(
                    "c", "p", "d",
                    "EU",
                    Set.of(),
                    "MARKETING",
                    Instant.now(),
                    ZoneId.of("UTC"),
                    1
            );

            PolicyEvaluationResult result =
                    new UsagePolicy("QUALITY_ANALYSIS")
                            .evaluate(ctx);

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("Usage purpose not allowed");
        }
    }

    @Nested
    class AndPolicyTests {

        @Test
        void allowsWhenAllPoliciesAllow() {
            Policy p1 = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext context) {
                    return PolicyEvaluationResult.allow();
                }
            };
            Policy p2 = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext context) {
                    return PolicyEvaluationResult.allow();
                }
            };

            AndPolicy policy =
                    new AndPolicy(List.of(p1, p2));

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void shortCircuitsOnFirstFailure() {
            Policy deny = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext context) {
                    return PolicyEvaluationResult.deny("FAIL");
                }
            };

            Policy neverCalled = mock(CompositePolicy.class);

            AndPolicy policy =
                    new AndPolicy(List.of(deny, neverCalled));

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isFalse();
            verifyNoInteractions(neverCalled);
        }
    }

    @Nested
    class OrPolicyTests {

        @Test
        void allowsWhenAnyPolicyAllows() {
            Policy deny = new CompositePolicy() {
                        @Override
                        public PolicyEvaluationResult evaluate(PolicyContext context) {
                            return PolicyEvaluationResult.deny("FAIL");
                        }
                    };

            Policy allow = new CompositePolicy() {
                        @Override
                        public PolicyEvaluationResult evaluate(PolicyContext context) {
                            return PolicyEvaluationResult.allow();
                        }
                    };

            OrPolicy policy =
                    new OrPolicy(List.of(deny, allow));

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }

        @Test
        void deniesWhenAllPoliciesDeny() {
            Policy deny1 = new CompositePolicy() {
                        @Override
                        public PolicyEvaluationResult evaluate(PolicyContext context) {
                            return PolicyEvaluationResult.deny("FAIL");
                        }
                    };

            Policy deny2 = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext context) {
                    return PolicyEvaluationResult.deny("FAIL");
                }
            };
            OrPolicy policy =
                    new OrPolicy(List.of(deny1, deny2));

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("OR-composed");
        }
    }

    @Nested
    class NotPolicyTests {

        @Test
        void deniesWhenInnerPolicyAllows() {
            Policy allow = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext context) {
                    return PolicyEvaluationResult.allow();
                }
            };

            NotPolicy policy =
                    new NotPolicy(allow);

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isFalse();
            assertThat(result.violationReason())
                    .contains("NOT policy violation");
        }

        @Test
        void allowsWhenInnerPolicyDenies() {
            Policy deny = new CompositePolicy() {
                @Override
                public PolicyEvaluationResult evaluate(PolicyContext ctx) {
                    return PolicyEvaluationResult.deny("FAIL");
                }
            };
            NotPolicy policy =
                    new NotPolicy(deny);

            PolicyEvaluationResult result =
                    policy.evaluate(baseContext());

            assertThat(result.allowed()).isTrue();
        }
    }
}
