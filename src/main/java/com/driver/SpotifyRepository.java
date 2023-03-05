package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user=new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        for (Artist a : artists) {
            if (a.getName().equals(artistName)) {
                artist = a;
                break;
            }
        }
        if (artist == null) {
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        if (artistAlbumMap.containsKey(artist)) {
            artistAlbumMap.get(artist).add(album);
        } else {
            List<Album> albums = new ArrayList<>();
            albums.add(album);
            artistAlbumMap.put(artist, albums);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = null;
        for (Artist artist : artistAlbumMap.keySet()) {
            List<Album> albums = artistAlbumMap.get(artist);
            for (Album a : albums) {
                if (a.getTitle().equals(albumName)) {
                    album = a;
                    break;
                }
            }
            if (album != null) {
                break;
            }
        }
        if (album == null) {
            throw new Exception("Album not found");
        }
        Song song = new Song(title, length);
        List<Song> songs = albumSongMap.get(album);
        if (songs == null) {
            songs = new ArrayList<>();
            albumSongMap.put(album, songs);
        }
        songs.add(song);
        this.songs.add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Playlist playlist = new Playlist(title);
        List<Song> playlistSongs = new ArrayList<>();
        int totalDuration = 0;
        for (Album album : albums) {
            List<Song> songs = albumSongMap.get(album);
            for (Song song : songs) {
                if (!playlistSongs.contains(song)) {
                    playlistSongs.add(song);
                    totalDuration += 1; // Incrementing duration by 1 for each song added to the playlist
                    if (totalDuration >= length) {
                        break;
                    }
                }
            }
            if (totalDuration >= length) {
                break;
            }
        }
        playlistSongMap.put(playlist, playlistSongs);
        playlistListenerMap.put(playlist, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Playlist playlist = new Playlist(title);
        List<Song> playlistSongs = new ArrayList<>();
        for (String songTitle : songTitles) {
            Song song = null;
            for (Song s : songs) {
                if (s.getTitle().equals(songTitle)) {
                    song = s;
                    break;
                }
            }
            if (song == null) {
                throw new IllegalArgumentException("Song not found: " + songTitle);
            }
            playlistSongs.add(song);
        }
        playlistSongMap.put(playlist, playlistSongs);
        playlistListenerMap.put(playlist, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User not found");
        }

        for (Playlist playlist : userPlaylistMap.get(user)) {
            if (playlist.getTitle().equals(playlistTitle)) {
                return playlist;
            }
        }

        throw new Exception("Playlist not found");
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User not found");
        }

        // Find the song object with the given title
        Song song = null;
        for (Song s : songs) {
            if (s.getTitle().equals(songTitle)) {
                song = s;
                break;
            }
        }
        if (song == null) {
            throw new Exception("Song not found");
        }

        // Add the user to the list of users who liked the song
        List<User> usersLiked = songLikeMap.get(song);
        if (usersLiked == null) {
            usersLiked = new ArrayList<>();
            songLikeMap.put(song, usersLiked);
        }
        usersLiked.add(user);

        return song;
    }

    public String mostPopularArtist() {
        Map<Artist, Integer> artistLikeCountMap = new HashMap<>();

        // Count the number of likes for each artist
        for (Song song : songLikeMap.keySet()) {
            List<User> likedByUsers = songLikeMap.get(song);
            Album album = null;
            for (Album a : albumSongMap.keySet()) {
                if (albumSongMap.get(a).contains(song)) {
                    album = a;
                    break;
                }
            }
            if (album == null) {
                continue;
            }
            Artist artist = null;
            for (Artist a : artistAlbumMap.keySet()) {
                if (artistAlbumMap.get(a).contains(album)) {
                    artist = a;
                    break;
                }
            }
            if (artist == null) {
                continue;
            }
            artistLikeCountMap.put(artist, artistLikeCountMap.getOrDefault(artist, 0) + likedByUsers.size());
        }

        // Find the artist with the most likes
        int maxLikes = -1;
        Artist mostPopularArtist = null;
        for (Artist artist : artistLikeCountMap.keySet()) {
            int likes = artistLikeCountMap.get(artist);
            if (likes > maxLikes) {
                maxLikes = likes;
                mostPopularArtist = artist;
            }
        }

        return mostPopularArtist != null ? mostPopularArtist.getName() : "No artists found";
    }

    public String mostPopularSong() {
        Map<Song, Integer> songLikesCount = new HashMap<>();

        // Count the number of likes for each song
        for (Song song : songLikeMap.keySet()) {
            int likes = songLikeMap.get(song).size();
            songLikesCount.put(song, likes);
        }

        // Find the song with the highest number of likes
        Song mostPopularSong = null;
        int maxLikes = 0;
        for (Song song : songLikesCount.keySet()) {
            int likes = songLikesCount.get(song);
            if (likes > maxLikes) {
                mostPopularSong = song;
                maxLikes = likes;
            }
        }

        if (mostPopularSong == null) {
            return "No songs found";
        } else {
            return mostPopularSong.getTitle();
        }
    }
}
