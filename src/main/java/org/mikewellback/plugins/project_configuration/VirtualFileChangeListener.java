package org.mikewellback.plugins.project_configuration;

import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;
import java.util.EventListener;

public class VirtualFileChangeListener implements VirtualFileListener {

    private final ContentsChangedListener listener;

    public VirtualFileChangeListener(ContentsChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        VirtualFileListener.super.propertyChanged(event);
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        VirtualFileListener.super.contentsChanged(event);
        if (listener != null) {
            listener.contentsChanged(event);
        }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        VirtualFileListener.super.fileCreated(event);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        VirtualFileListener.super.fileDeleted(event);
        if (listener != null) {
            listener.contentsDeleted(event);
        }
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        VirtualFileListener.super.fileMoved(event);
        if (listener != null) {
            listener.contentsDeleted(event);
        }
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
        VirtualFileListener.super.fileCopied(event);
    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
        VirtualFileListener.super.beforePropertyChange(event);
    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent event) {
        VirtualFileListener.super.beforeContentsChange(event);
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
        VirtualFileListener.super.beforeFileDeletion(event);
    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
        VirtualFileListener.super.beforeFileMovement(event);
    }

    public interface ContentsChangedListener extends EventListener {
        void contentsChanged(@NotNull VirtualFileEvent event);
        void contentsDeleted(@NotNull VirtualFileEvent event);
    }
}