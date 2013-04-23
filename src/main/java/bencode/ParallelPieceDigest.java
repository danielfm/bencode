package bencode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ParallelPieceDigest {
    private final FileSet fileSet;
    private final int pieceLength;

    public ParallelPieceDigest(List<File> files, int pieceLength) {
        this.fileSet = new FileSet(files);
        this.pieceLength = pieceLength;
    }

    public byte[] computeHash(int numWorkers) throws IOException {
        AtomicInteger hashedPieces = new AtomicInteger();

        int piece = 0;
        int totalPieces = this.fileSet.totalPieces(this.pieceLength);

        byte[] globalHash  = new byte[totalPieces * 20];
        byte[] pieceBuffer = new byte[this.pieceLength];

        int bytesRead = 0;
        int bytesLeft = this.pieceLength;

        FileInputStream in = null;

        ExecutorService pool = Executors.newFixedThreadPool(numWorkers);

        try {
            for (File file : this.fileSet.getFiles()) {
                in = new FileInputStream(file);
                int lastRead = 0;

                do {
                    lastRead = in.read(pieceBuffer, bytesRead, bytesLeft);

                    if (lastRead >= 0) {
                        bytesRead += lastRead;
                        bytesLeft -= lastRead;
                    }

                    // piece is ready for hashing
                    if (bytesLeft == 0) {
                        // new buffer for the next piece
                        byte[] pieceData = pieceBuffer;
                        pieceBuffer = new byte[this.pieceLength];

                        // submit piece for hashing
                        pool.execute(new PieceDigestJob(globalHash, pieceData, bytesRead, piece++, hashedPieces));

                        // new piece
                        bytesRead = 0;
                        bytesLeft = this.pieceLength;
                    }
                } while (lastRead > 0);

                // eof
                in.close();
            }

            // submit the final piece for hashing
            pool.execute(new PieceDigestJob(globalHash, pieceBuffer, bytesRead, piece++, hashedPieces));

            // wait for all workers to finish
            while (hashedPieces.get() < totalPieces);
        }
        finally {
            if (in != null) {
                in.close();
            }

            pool.shutdown();
        }

        return globalHash;
    }
}
