package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndList;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to add a shader node definition.
 *
 * @author JavaSaBr
 */
public class AddSndOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The definition list.
     */
    @NotNull
    private final SndList definitionList;

    /**
     * The added definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public AddSndOperation(@NotNull final SndList definitionList,
                           @NotNull final ShaderNodeDefinition definition) {
        this.definitionList = definitionList;
        this.definition = definition;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        definitionList.getDefinitions().add(definition);
        editor.notifyFXAddedChild(definitionList, definition, -1, true);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        definitionList.getDefinitions().remove(definition);
        editor.notifyFXRemovedChild(definitionList, definition);
    }
}
