package com.zype.android.Db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.zype.android.Db.Entity.Playlist;
import com.zype.android.Db.Entity.PlaylistVideo;
import com.zype.android.Db.Entity.Video;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Evgeny Cherkasov on 13.06.2018
 */

@Dao
public interface ZypeDao {

    // Playlist

    @Query("SELECT * FROM playlist WHERE playlist.parent_id = :parentPlaylistId AND playlist.active = 1 ORDER BY playlist.priority")
    public LiveData<List<Playlist>> getPlaylists(String parentPlaylistId);

    @Query("SELECT * FROM playlist WHERE playlist.parent_id = :parentPlaylistId AND playlist.active = 1 ORDER BY playlist.priority")
    public List<Playlist> getPlaylistsSync(String parentPlaylistId);

    @Query("SELECT * FROM playlist WHERE playlist._id = :playlistId LIMIT 1")
    public Playlist getPlaylistSync(String playlistId);

    @Insert(onConflict = REPLACE)
    public void insertPlaylists(List<Playlist> playlists);

    // Video

    /*
     * Retrieve playlist videos
     */
    @Query("SELECT * FROM video INNER JOIN playlist_video ON video._id = playlist_video.video_id WHERE playlist_video.playlist_id = :playlistId")
    public LiveData<List<Video>> getPlaylistVideos(String playlistId);

    @Query("SELECT * FROM video INNER JOIN playlist_video ON video._id = playlist_video.video_id WHERE playlist_video.playlist_id = :playlistId")
    public List<Video> getPlaylistVideosSync(String playlistId);

    /*
     * Insert (or update) a list of videos into the playlist
     */
    @Insert(onConflict = REPLACE)
    public void insertPlaylistVideos(List<PlaylistVideo> playlistVideos);

    /*
     * Delete specified video from the playlist
     */
    @Query("DELETE FROM playlist_video WHERE playlist_video.playlist_id = :playlistId " +
            "AND playlist_video.video_id = :videoId")
    public void deletePlaylistVideo(String playlistId, String videoId);

    /*
     * Delete all videos from the playlist
     */
    @Query("DELETE FROM playlist_video WHERE playlist_video.playlist_id = :playlistId")
    public void deletePlaylistVideos(String playlistId);

    /*
     * Retrieve a video by its 'id'
     */
    @Query("SELECT * FROM video WHERE video._id = :videoId LIMIT 1")
    public Video getVideoSync(String videoId);

    /*
     * Update video. Existing video will be replaced
     */
    @Update(onConflict = REPLACE)
    public void updateVideo(Video video);

    /*
     * Insert a list of videos. Existing videos will be replaced
     */
    @Insert(onConflict = REPLACE)
    public void insertVideos(List<Video> videos);

}
