package bank.card.management.util;

import bank.card.management.exception.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {
    
    private EncryptionUtil encryptionUtil;
    
    private String testSecret = "test-secret-key-2024-minimum-32-characters-for-aes-256";
    
    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil(testSecret);
    }
    
    @Test
    void testEncrypt_Success() {
        String plainText = "1234567890123456";
        String encrypted = encryptionUtil.encrypt(plainText);
        
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertFalse(encrypted.isEmpty());
    }
    
    @Test
    void testDecrypt_Success() {
        String plainText = "1234567890123456";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testEncryptDecrypt_LongCardNumber() {
        String plainText = "1234567890123456789";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testEncryptDecrypt_ShortCardNumber() {
        String plainText = "1234";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testEncryptDecrypt_WithSpaces() {
        String plainText = "1234 5678 9012 3456";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testEncrypt_EmptyString() {
        String plainText = "";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    void testDecrypt_InvalidEncryptedText() {
        String invalidEncrypted = "invalid-encrypted-text";
        
        assertThrows(EncryptionException.class, () -> encryptionUtil.decrypt(invalidEncrypted));
    }
    
    @Test
    void testEncryptDecrypt_Consistency() {
        String plainText = "1234567890123456";
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);
        
        String decrypted1 = encryptionUtil.decrypt(encrypted1);
        String decrypted2 = encryptionUtil.decrypt(encrypted2);
        
        assertEquals(plainText, decrypted1);
        assertEquals(plainText, decrypted2);
    }
}
