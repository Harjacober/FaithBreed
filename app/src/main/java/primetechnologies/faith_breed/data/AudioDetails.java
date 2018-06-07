package primetechnologies.faith_breed.data;

public class AudioDetails {
    String audioName;
    String audioArtist;
    String audioImageLink;
    String audioDownloadLink;

    public String getAudioArtist() {
        return audioArtist;
    }

    public String getAudioImageLink() {
        return audioImageLink;
    }

    public String getAudioDownloadLink() {
        return audioDownloadLink;
    }

    public String getAudioName() {
        return audioName;
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
