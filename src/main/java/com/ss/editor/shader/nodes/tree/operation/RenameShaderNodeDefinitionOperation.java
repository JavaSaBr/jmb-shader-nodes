package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionList;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to rename a shader node definition.
 *
 * @author JavaSaBr
 */
public class RenameShaderNodeDefinitionOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The old name.
     */
    @NotNull
    private final String oldName;

    /**
     * The new name.
     */
    @NotNull
    private final String newName;

    /**
     * The list of definitions.
     */
    @NotNull
    private final ShaderNodeDefinitionList definitionList;

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public RenameShaderNodeDefinitionOperation(@NotNull final String oldName, @NotNull final String newName,
                                               @NotNull final ShaderNodeDefinitionList definitionList,
                                               @NotNull final ShaderNodeDefinition definition) {
        this.oldName = oldName;
        this.newName = newName;
        this.definitionList = definitionList;
        this.definition = definition;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        definition.setName(newName);
        editor.notifyFXChangeProperty(definitionList, definition, Messages.MODEL_PROPERTY_NAME);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        definition.setName(oldName);
        editor.notifyFXChangeProperty(definitionList, definition, Messages.MODEL_PROPERTY_NAME);
    }
}
