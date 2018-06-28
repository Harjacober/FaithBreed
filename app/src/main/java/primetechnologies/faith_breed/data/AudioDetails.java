package primetechnologies.faith_breed.data;

public class AudioDetails {
    private String audioName;
    private String audioArtist;
    private String audioImageLink;
    private String audioDownloadLink;



    public String getAudioName() {
        return audioName;
    }
    public String getAudioDownloadLink() {
        return audioDownloadLink;
    }
    public String getAudioImageLink() {
        return audioImageLink;
    }
    public String getAudioArtist() {
        return audioArtist;
    }




    public void setAudioArtist(String audioArtist) {
        this.audioArtist = audioArtist;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public void setAudioImageLink(String audioImageLink) {
        this.audioImageLink = audioImageLink;
    }

    public void setAudioDownloadLink(String audioDownloadLink) {
        this.audioDownloadLink = audioDownloadLink;
    }
}
