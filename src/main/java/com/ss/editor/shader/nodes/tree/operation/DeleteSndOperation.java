package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The operation to delete a shader node definition.
 *
 * @author JavaSaBr
 */
public class DeleteSndOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The definition list.
     */
    @NotNull
    private final SndList definitionList;

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    /**
     * The previous position.
     */
    private int index;

    public DeleteSndOperation(@NotNull final SndList definitionList,
                              @NotNull final ShaderNodeDefinition definition) {
        this.definitionList = definitionList;
        this.definition = definition;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        final List<ShaderNodeDefinition> definitions = definitionList.getDefinitions();
        index = definitions.indexOf(definition);
        definitions.remove(definition);
        editor.notifyFXRemovedChild(definitionList, definition);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        definitionList.getDefinitions().add(index, definition);
        editor.notifyFXAddedChild(definitionList, definition, index, false);
    }
}
