package com.kk.taurus.playerbase.player;

import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.SegmentDownloader;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.ParsingLoadable;
import com.google.android.exoplayer2.util.UriUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CustomHlsDownloader extends CustomSegmentDownloader<HlsPlaylist> {

    /**
     * @param playlistUri       The {@link Uri} of the playlist to be downloaded.
     * @param streamKeys        Keys defining which renditions in the playlist should be selected for
     *                          download. If empty, all renditions are downloaded.
     * @param constructorHelper A {@link DownloaderConstructorHelper} instance.
     */
    public CustomHlsDownloader(
            Uri playlistUri, List<StreamKey> streamKeys, DownloaderConstructorHelper constructorHelper) {
        super(playlistUri, streamKeys, constructorHelper);
    }

    @Override
    protected HlsPlaylist getManifest(DataSource dataSource, Uri uri) throws IOException {
        return loadManifest(dataSource, uri);
    }

    @Override
    protected List<CustomSegmentDownloader.Segment> getSegments(
            DataSource dataSource, HlsPlaylist playlist, boolean allowIncompleteList) throws IOException {
        ArrayList<Uri> mediaPlaylistUris = new ArrayList<>();
        if (playlist instanceof HlsMasterPlaylist) {
            HlsMasterPlaylist masterPlaylist = (HlsMasterPlaylist) playlist;
            addResolvedUris(masterPlaylist.baseUri, masterPlaylist.variants, mediaPlaylistUris);
            addResolvedUris(masterPlaylist.baseUri, masterPlaylist.audios, mediaPlaylistUris);
            addResolvedUris(masterPlaylist.baseUri, masterPlaylist.subtitles, mediaPlaylistUris);
        } else {
            mediaPlaylistUris.add(Uri.parse(playlist.baseUri));
        }
        ArrayList<CustomSegmentDownloader.Segment> segments = new ArrayList<>();

        HashSet<Uri> seenEncryptionKeyUris = new HashSet<>();
        for (Uri mediaPlaylistUri : mediaPlaylistUris) {
            HlsMediaPlaylist mediaPlaylist;
            try {
                mediaPlaylist = (HlsMediaPlaylist) loadManifest(dataSource, mediaPlaylistUri);
                segments.add(new CustomSegmentDownloader.Segment(mediaPlaylist.startTimeUs, new DataSpec(mediaPlaylistUri)));
            } catch (IOException e) {
                if (!allowIncompleteList) {
                    throw e;
                }
                segments.add(new CustomSegmentDownloader.Segment(0, new DataSpec(mediaPlaylistUri)));
                continue;
            }
            HlsMediaPlaylist.Segment lastInitSegment = null;
            List<HlsMediaPlaylist.Segment> hlsSegments = mediaPlaylist.segments;
            for (int i = 0; i < hlsSegments.size(); i++) {
                HlsMediaPlaylist.Segment segment = hlsSegments.get(i);
                HlsMediaPlaylist.Segment initSegment = segment.initializationSegment;
                if (initSegment != null && initSegment != lastInitSegment) {
                    lastInitSegment = initSegment;
                    addSegment(segments, mediaPlaylist, initSegment, seenEncryptionKeyUris);
                }
                addSegment(segments, mediaPlaylist, segment, seenEncryptionKeyUris);
            }
        }
        return segments;
    }

    private static HlsPlaylist loadManifest(DataSource dataSource, Uri uri) throws IOException {
        return ParsingLoadable.load(dataSource, new HlsPlaylistParser(), uri, C.DATA_TYPE_MANIFEST);
    }

    private static void addSegment(
            ArrayList<CustomSegmentDownloader.Segment> segments,
            HlsMediaPlaylist mediaPlaylist,
            HlsMediaPlaylist.Segment hlsSegment,
            HashSet<Uri> seenEncryptionKeyUris) {
        long startTimeUs = mediaPlaylist.startTimeUs + hlsSegment.relativeStartTimeUs;
        if (hlsSegment.fullSegmentEncryptionKeyUri != null) {
            Uri keyUri = UriUtil.resolveToUri(mediaPlaylist.baseUri,
                    hlsSegment.fullSegmentEncryptionKeyUri);
            if (seenEncryptionKeyUris.add(keyUri)) {
                segments.add(new CustomSegmentDownloader.Segment(startTimeUs, new DataSpec(keyUri)));
            }
        }
        Uri resolvedUri = UriUtil.resolveToUri(mediaPlaylist.baseUri, hlsSegment.url);
        segments.add(new CustomSegmentDownloader.Segment(startTimeUs,
                new DataSpec(resolvedUri, hlsSegment.byterangeOffset, hlsSegment.byterangeLength, null)));
    }

    private static void addResolvedUris(String baseUri, List<HlsMasterPlaylist.HlsUrl> urls, List<Uri> out) {
        for (int i = 0; i < urls.size(); i++) {
            out.add(UriUtil.resolveToUri(baseUri, urls.get(i).url));
        }
    }
}
