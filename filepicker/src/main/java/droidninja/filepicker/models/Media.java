package droidninja.filepicker.models;

public class Media extends BaseFile {

  public int mediaType;

  public Media(int id, String name, String path, int mediaType, double fileSize) {
    super(id, name, path, fileSize);
    this.mediaType = mediaType;
  }

  public Media() {
    super(0,null,null,0);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Media)) return false;

    Media media = (Media) o;

    return id == media.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public String getPath() {
    return (path!=null)?path:"";
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMediaType() {
    return mediaType;
  }

  public void setMediaType(int mediaType) {
    this.mediaType = mediaType;
  }

  public double getFileSize() {
    return fileSize;
  }

  public void setFileSize(double fileSize) {
    this.fileSize = fileSize;
  }

  @Override public String toString() {
    return "Media{" +
        "mediaType=" + mediaType +
        ", id=" + id +
        ", name='" + name + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
