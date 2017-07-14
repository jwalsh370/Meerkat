package com.jahanwalsh.meerkat;

import com.wrapper.spotify.models.Track;

import java.util.List;

/**
 * Created by jahanwalsh on 7/14/17.
 */

public class Search {

    public interface View {
        void reset();

        void addData(List<Track> items);
    }

    public interface ActionListener {

        void init(String token);

        String getCurrentQuery();

        void search(String searchQuery);

        void loadMoreResults();

        void selectTrack(Track item);

        void resume();

        void pause();

        void destroy();

    }
}