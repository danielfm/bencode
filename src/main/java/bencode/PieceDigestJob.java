package bencode;

import java.util.concurrent.atomic.AtomicInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PieceDigestJob implements Runnable {
    private byte[] globalHash;
    private byte[] data;
    private int dataLength;
    private int piece;
    private AtomicInteger hashedPieces;

    public PieceDigestJob(byte[] globalHash, byte[] data, int dataLength, int piece, AtomicInteger hashedPieces) {
        this.globalHash = globalHash;
        this.data = data;
        this.dataLength = dataLength;
        this.piece = piece;
        this.hashedPieces = hashedPieces;
    }

    public void run() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA1");

            // computes the sha-1 hash for this piece
            sha.update(this.data, 0, this.dataLength);
            byte[] hash = sha.digest();

            // updates the global hash
            for (int i = 0; i < hash.length; i++) {
                this.globalHash[20 * this.piece + i] = hash[i];
            }

            // increment the number of hashed pieces until now
            this.hashedPieces.incrementAndGet();

            // giving a hand to the GC
            this.globalHash = null;
            this.data = null;
        }
        catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }
}
