package bencode;

import java.util.List;
import java.io.File;

public class FileSet {
    private List<File> files;

    public FileSet(List<File> files) {
        this.files = files;
    }

    public List<File> getFiles() {
        return files;
    }

    public long getTotalSize() {
        long size = 0L;

        for (File file : this.files) {
            size += file.length();
        }

        return size;
    }

    public int totalPieces(int pieceLength) {
        return (int) Math.ceil((double) this.getTotalSize() / pieceLength);
    }
}
