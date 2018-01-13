package com.ss.editor.shader.nodes.ui.preview;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.ui.preview.FilePreview;
import com.ss.editor.ui.preview.FilePreviewFactory;
import com.ss.rlib.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of {@link FilePreviewFactory} to provide plugin's implementations of file previews.
 *
 * @author JavaSaBr
 */
public class SndFilePreviewFactory implements FilePreviewFactory {

    @NotNull
    private static final SndFilePreviewFactory INSTANCE = new SndFilePreviewFactory();

    public static @NotNull SndFilePreviewFactory getInstance() {
        return INSTANCE;
    }

    @Override
    @FxThread
    public void createFilePreviews(@NotNull final Array<FilePreview> result) {
        result.add(new SndFilePreview());
    }
}
