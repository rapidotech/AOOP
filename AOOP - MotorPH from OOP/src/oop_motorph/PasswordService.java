package oop_motorph;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** BCrypt policy shared by account creation, verification, reset, and enrollment tools. */
public final class PasswordService {
    public static final int BCRYPT_COST = 12;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] DEFAULTS = {"1234", "123456789012", "password1234", "password12345",
            "changeme12345", "motorph123456", "admin12345678"};

    private PasswordService() { }

    public static void validateNewPassword(char[] password) {
        if (password == null || Character.codePointCount(password, 0, password.length) < 12) {
            throw new IllegalArgumentException("Use at least 12 characters for your new password.");
        }
        if (utf8Length(password) > 72) {
            throw new IllegalArgumentException("Your password must fit within 72 UTF-8 bytes. Shorten it; characters will never be silently truncated.");
        }
        for (char character : password) {
            if (Character.isISOControl(character)) {
                throw new IllegalArgumentException("Passwords cannot contain control characters.");
            }
        }
        if (isKnownDefault(password)) {
            throw new IllegalArgumentException("Choose a personal password instead of a known default or repeated password.");
        }
    }

    public static String hashPassword(char[] password) {
        validateNewPassword(password);
        return BCrypt.with(BCrypt.Version.VERSION_2B).hashToString(BCRYPT_COST, password);
    }

    public static boolean verifyPassword(char[] password, String storedHash) {
        if (password == null || password.length == 0 || !isSupportedHash(storedHash)) { return false; }
        try {
            if (utf8Length(password) > 72) { return false; }
            return BCrypt.verifyer().verify(password, storedHash).verified;
        } catch (IllegalArgumentException failure) {
            return false;
        }
    }

    public static boolean isSupportedHash(String hash) {
        return hash != null && hash.matches("\\$2[aby]\\$12\\$[./A-Za-z0-9]{53}");
    }

    /** Unique temporary proof, delivered separately by the protected enrollment tool. */
    public static char[] generateTemporaryPassword() {
        byte[] entropy = new byte[24];
        RANDOM.nextBytes(entropy);
        byte[] encoded = Base64.getUrlEncoder().withoutPadding().encode(entropy);
        try {
            char[] temporary = new char[encoded.length];
            for (int i = 0; i < encoded.length; i++) { temporary[i] = (char) encoded[i]; }
            return temporary;
        } finally {
            Arrays.fill(entropy, (byte) 0);
            Arrays.fill(encoded, (byte) 0);
        }
    }

    private static int utf8Length(char[] password) {
        ByteBuffer encoded = null;
        try {
            encoded = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(password));
            return encoded.remaining();
        } catch (CharacterCodingException failure) {
            throw new IllegalArgumentException("Your password contains an invalid Unicode character.");
        } finally {
            if (encoded != null && encoded.hasArray()) { Arrays.fill(encoded.array(), (byte) 0); }
        }
    }

    private static boolean isKnownDefault(char[] password) {
        int start = 0, end = password.length;
        while (start < end && Character.isWhitespace(password[start])) { start++; }
        while (end > start && Character.isWhitespace(password[end - 1])) { end--; }
        boolean repeated = end > start;
        boolean repeatedDefault = end > start;
        for (int i = start; i < end; i++) {
            repeated &= password[i] == password[start];
            repeatedDefault &= password[i] == "1234".charAt((i - start) % 4);
        }
        if (repeated || repeatedDefault) { return true; }
        for (String known : DEFAULTS) {
            if (known.length() != end - start) { continue; }
            boolean equal = true;
            for (int i = 0; i < known.length(); i++) {
                equal &= Character.toLowerCase(password[start + i]) == known.charAt(i);
            }
            if (equal) { return true; }
        }
        return false;
    }
}
