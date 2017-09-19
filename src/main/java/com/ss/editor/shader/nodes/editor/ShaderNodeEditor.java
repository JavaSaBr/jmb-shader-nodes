package com.ss.editor.shader.nodes.editor;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.api.editor.BaseFileEditorWithoutState;
import com.ss.editor.ui.component.editor.EditorDescription;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

/**
 * The editor to edit j3sn files.
 *
 * @author JavaSaBr
 */
public class ShaderNodeEditor extends BaseFileEditorWithoutState {

    @FXThread
    @Override
    protected void createContent(@NotNull final StackPane root) {

    }

    @FromAnyThread
    @Override
    public @NotNull EditorDescription getDescription() {
        return null;
    }
}
