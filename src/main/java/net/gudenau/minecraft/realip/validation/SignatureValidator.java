package net.gudenau.minecraft.realip.validation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * A basic cryptographic signature validator. This uses the TCP Shield real IP public key to validate a signature
 * against a palyload.
 */
public final class SignatureValidator {
    private final PublicKey publicKey;
    
    /**
     * Creates a new validator by reading the signing_pub.key file.
     *
     * @throws RuntimeException If the signature could not be read correctly
     */
    public SignatureValidator(){
        try(var stream = SignatureValidator.class.getResourceAsStream("/assets/realip/signing_pub.key")) {
            if(stream == null){
                throw new FileNotFoundException("Failed to find signing_pub.key");
            }
            
            var encodedKey = stream.readAllBytes();
            var keySpec = new X509EncodedKeySpec(encodedKey);
    
            var factory = KeyFactory.getInstance("EC");
            publicKey = factory.generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to load public key for client verification", e);
        }
    }
    
    /**
     * Validates a signature.
     *
     * @param payload The data to verify
     * @param signatureBytes The expected signature of the data
     * @return True if valid, false if invalid
     */
    public boolean validate(byte[] payload, byte[] signatureBytes) {
        try {
            var signature = Signature.getInstance("SHA512withECDSA");
            signature.initVerify(publicKey);
            signature.update(payload);
            return signature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            return false;
        }
    }
}
