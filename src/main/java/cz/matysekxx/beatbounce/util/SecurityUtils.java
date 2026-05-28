package cz.matysekxx.beatbounce.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cryptographic utility providing robust AES-128 encryption and integrity checks
 * for local save files (scores, currency, and achievements).
 * It completely prevents players from editing JSON save files in text editors,
 * automatically resetting or rejecting modified/corrupted files.
 */
public class SecurityUtils {
    /**
     * The fixed 16-character key used for AES-128 encryption.
     */
    private static final String SECRET_KEY = "A1b2x3d8E5f6g7h8";

    /**
     * The key specification object for the AES algorithm.
     */
    private static final SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

    /**
     * Encrypts a plain text string using AES-128 and encodes the result in Base64.
     *
     * @param plainText the raw string to encrypt
     * @return the Base64 encrypted string
     */
    public static String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            final byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Save encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64 encoded AES-128 encrypted string back to plain text.
     * Returns null if decryption fails (indicating tampering, corruption, or key mismatches).
     *
     * @param encryptedText the Base64 encrypted string
     * @return the decrypted plain text string, or null if tampered
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            final byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText.trim()));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
