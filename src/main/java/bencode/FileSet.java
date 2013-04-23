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

    public int totalPieces(int pieceLength) {
        return (int) Math.ceil((double) this.totalSize() / pieceLength);
    }

    private long totalSize() {
        long size = 0L;

        for (File file : this.files) {
            size += file.length();
        }

        return size;
    }
}
