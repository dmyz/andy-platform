package net.junanw.upms.foundation.platform.auth.application.code.send;

import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.application.code.model.response.VerificationCodeSendResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerificationCodeSendProcessorTest {

    @Test
    void shouldRouteRequestToMatchingSender() {
        VerificationCodeSender mobileSender = new VerificationCodeSender() {
            @Override
            public VerificationTargetType targetType() {
                return VerificationTargetType.MOBILE;
            }

            @Override
            public VerificationCodeSendResponse send(String target, String scene, int expireSeconds) {
                return new VerificationCodeSendResponse(scene, "MOBILE", mask(target), expireSeconds, "123456");
            }

            private String mask(String target) {
                return target.substring(0, 3) + "****" + target.substring(target.length() - 4);
            }
        };
        VerificationCodeSender emailSender = new VerificationCodeSender() {
            @Override
            public VerificationTargetType targetType() {
                return VerificationTargetType.EMAIL;
            }

            @Override
            public VerificationCodeSendResponse send(String target, String scene, int expireSeconds) {
                return new VerificationCodeSendResponse(scene, "EMAIL", target, expireSeconds, null);
            }
        };
        VerificationCodeSendProcessor processor = new VerificationCodeSendProcessorImpl(
                new VerificationCodeSenderRegistry(List.of(mobileSender, emailSender))
        );

        VerificationCodeSendResponse response = processor.send(new VerificationCodeSendRequest(
                VerificationTargetType.MOBILE,
                "13800138000",
                "LOGIN",
                300
        ));

        assertEquals("LOGIN", response.scene());
        assertEquals("MOBILE", response.targetType());
        assertEquals("138****8000", response.maskedTarget());
        assertEquals(300, response.expireSeconds());
        assertEquals("123456", response.devCode());
    }
}
