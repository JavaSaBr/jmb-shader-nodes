package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to change documentation of shader node definition.
 *
 * @author JavaSaBr
 */
public class ChangeSndDocumentationOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    @NotNull
    private final String oldDocumentation;

    @NotNull
    private final String newDocumentation;

    public ChangeSndDocumentationOperation(@NotNull final ShaderNodeDefinition definition,
                                           @NotNull final String oldDocumentation,
                                           @NotNull final String newDocumentation) {
        this.definition = definition;
        this.oldDocumentation = oldDocumentation;
        this.newDocumentation = newDocumentation;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        definition.setDocumentation(newDocumentation);
        editor.notifyFXChangeProperty(definition, "documentation");
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        definition.setDocumentation(oldDocumentation);
        editor.notifyFXChangeProperty(definition, "documentation");
    }
}
