package edu.upc.openmrs.test;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MockFailure<T> implements Call<T> {

    private final Throwable throwable;

    public MockFailure(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Response<T> execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        callback.onFailure(this, throwable);
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
        // This method is intentionally empty
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Timeout timeout() {
        return null;
    }
}