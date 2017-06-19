package ser;

/**
 * Created by ruedi on 10.04.17.
 */
public class I178 implements java.io.Serializable {
    public static void main(String[] args) throws java.io.IOException {
        new org.nustaq.serialization.FSTObjectOutput().writeObject(new I178());
    }
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.IOException("Boom!");
    }
}