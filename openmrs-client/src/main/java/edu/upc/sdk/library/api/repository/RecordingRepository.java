package edu.upc.sdk.library.api.repository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import rx.Observable;

public class RecordingRepository extends BaseRepository {

    @Inject
    public  RecordingRepository(){}

    public Observable<String> saveRecording(@NotNull String filePath) {
        return null;
    }
}
