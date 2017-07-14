package tonyd.gti785dataclient;

import java.util.List;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class FolderContent {

    private List<Folder> folders;
    private List<File> files;

    public FolderContent(List<Folder> folders, List<File> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "FolderContent{" +
                "folders=" + folders +
                ", files=" + files +
                '}';
    }

}
