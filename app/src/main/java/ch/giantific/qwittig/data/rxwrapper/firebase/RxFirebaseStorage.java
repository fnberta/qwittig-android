package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.InputStream;

import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToTaskOnCompleteOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToTaskOnFailureSuccessOnSubscribe;
import rx.Single;

/**
 * Created by Nick Moskalenko on 24/05/2016.
 */
public class RxFirebaseStorage {

    @NonNull
    public static Single<byte[]> getBytes(@NonNull final StorageReference storageRef,
                                          final long maxDownloadSizeBytes) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.getBytes(maxDownloadSizeBytes)));
    }

    @NonNull
    public static Single<Uri> getDownloadUrl(@NonNull final StorageReference storageRef) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.getDownloadUrl()));
    }

    @NonNull
    public static Single<FileDownloadTask.TaskSnapshot> getFile(@NonNull final StorageReference storageRef,
                                                                @NonNull final File destinationFile) {
        return Single.create(new ListenToTaskOnFailureSuccessOnSubscribe<>(storageRef.getFile(destinationFile)));
    }

    @NonNull
    public static Single<FileDownloadTask.TaskSnapshot> getFile(@NonNull final StorageReference storageRef,
                                                                @NonNull final Uri destinationUri) {
        return Single.create(new ListenToTaskOnFailureSuccessOnSubscribe<>(storageRef.getFile(destinationUri)));
    }

    @NonNull
    public static Single<StorageMetadata> getMetadata(@NonNull final StorageReference storageRef) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.getMetadata()));
    }

    @NonNull
    public static Single<StreamDownloadTask.TaskSnapshot> getStream(@NonNull final StorageReference storageRef) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.getStream()));
    }

    @NonNull
    public static Single<StreamDownloadTask.TaskSnapshot> getStream(@NonNull final StorageReference storageRef,
                                                                    @NonNull final StreamDownloadTask.StreamProcessor processor) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.getStream(processor)));
    }


    @NonNull
    public static Single<UploadTask.TaskSnapshot> putBytes(@NonNull final StorageReference storageRef,
                                                           @NonNull final byte[] bytes) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.putBytes(bytes)));
    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putBytes(@NonNull final StorageReference storageRef,
                                                           @NonNull final byte[] bytes,
                                                           @NonNull final StorageMetadata metadata) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.putBytes(bytes, metadata)));
    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putFile(@NonNull final StorageReference storageRef,
                                                          @NonNull final Uri uri) {
        return Single.create(new ListenToTaskOnFailureSuccessOnSubscribe<>(storageRef.putFile(uri)));
    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putFile(@NonNull final StorageReference storageRef,
                                                          @NonNull final Uri uri,
                                                          @NonNull final StorageMetadata metadata) {
        return Single.create(new ListenToTaskOnFailureSuccessOnSubscribe<>(storageRef.putFile(uri, metadata)));

    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putFile(@NonNull final StorageReference storageRef,
                                                          @NonNull final Uri uri,
                                                          @NonNull final StorageMetadata metadata,
                                                          @NonNull final Uri existingUploadUri) {
        return Single.create(new ListenToTaskOnFailureSuccessOnSubscribe<>(storageRef.putFile(uri, metadata, existingUploadUri)));

    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putStream(@NonNull final StorageReference storageRef,
                                                            @NonNull final InputStream stream,
                                                            @NonNull final StorageMetadata metadata) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.putStream(stream, metadata)));
    }

    @NonNull
    public static Single<UploadTask.TaskSnapshot> putStream(@NonNull final StorageReference storageRef,
                                                            @NonNull final InputStream stream) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.putStream(stream)));
    }

    @NonNull
    public static Single<StorageMetadata> updateMetadata(@NonNull final StorageReference storageRef,
                                                         @NonNull final StorageMetadata metadata) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.updateMetadata(metadata)));
    }

    @NonNull
    public static Single<Void> delete(@NonNull final StorageReference storageRef) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(storageRef.delete()));
    }
}
