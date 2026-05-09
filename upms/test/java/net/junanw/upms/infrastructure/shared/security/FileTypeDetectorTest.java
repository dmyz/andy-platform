package net.junanw.upms.infrastructure.shared.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileTypeDetectorTest {

    @Test
    void detectMimeTypeShouldRecognizeAllowedMagicNumbers() {
        assertEquals("image/png", FileTypeDetector.detectMimeType(new byte[]{
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'
        }));
        assertEquals("image/jpeg", FileTypeDetector.detectMimeType(new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00
        }));
        assertEquals("application/pdf", FileTypeDetector.detectMimeType("%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void matchesShouldRequireMimeExtensionAndContentToAgree() {
        byte[] text = "plain text".getBytes(StandardCharsets.UTF_8);

        assertTrue(FileTypeDetector.matches("text/plain; charset=utf-8", "txt", text));
        assertFalse(FileTypeDetector.matches("image/png", "png", text));
        assertFalse(FileTypeDetector.matches("text/plain", "html", text));
    }
}
