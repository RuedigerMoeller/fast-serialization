package ser.issue52;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * Created by ruedi on 01/02/15.
 */
public class I52 {

    // Fermat F4, largest known fermat prime
    private static final BigInteger PUBLIC_EXP = new BigInteger("10001", 16);;
    private static final int STRENGTH = 1024;

    @Test
    public void test() throws Exception {
        // install BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());

        // generate a keypair
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
        RSAKeyGenParameterSpec params = new RSAKeyGenParameterSpec(STRENGTH, PUBLIC_EXP);
        gen.initialize(params, new SecureRandom());
        KeyPair keyPair = gen.generateKeyPair();

        FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

        // serialize
        byte[] serialized = fst.asByteArray(keyPair);

        // deserialize --> crash
        KeyPair deserialized = (KeyPair) fst.asObject(serialized);
    }
}
