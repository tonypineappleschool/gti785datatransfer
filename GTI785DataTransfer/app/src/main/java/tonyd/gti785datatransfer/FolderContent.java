package tonyd.gti785datatransfer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class FolderContent implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(folders);
        dest.writeTypedList(files);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FolderContent createFromParcel(Parcel in) {
            return new FolderContent(in);
        }

        public FolderContent[] newArray(int size) {
            return new FolderContent[size];
        }
    };

    private FolderContent(Parcel in) {
        folders = new ArrayList<>();
        files = new ArrayList<>();
        in.readTypedList(folders, Folder.CREATOR);
        in.readTypedList(files, File.CREATOR);
    }
}
