package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefine;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefines;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to rename a define.
 *
 * @author JavaSaBr
 */
public class RenameShaderNodeDefinitionDefineOperation extends AbstractEditorOperation<ChangeConsumer> {

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
     * The list of defines.
     */
    @NotNull
    private final ShaderNodeDefinitionDefines defines;

    /**
     * The define.
     */
    @NotNull
    private final ShaderNodeDefinitionDefine define;

    public RenameShaderNodeDefinitionDefineOperation(@NotNull final String oldName, @NotNull final String newName,
                                                     @NotNull final ShaderNodeDefinitionDefines defines,
                                                     @NotNull final ShaderNodeDefinitionDefine define) {
        this.oldName = oldName;
        this.newName = newName;
        this.defines = defines;
        this.define = define;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        defines.rename(define, newName);
        editor.notifyFXChangeProperty(defines, define, Messages.MODEL_PROPERTY_NAME);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        defines.rename(define, oldName);
        editor.notifyFXChangeProperty(defines, define, Messages.MODEL_PROPERTY_NAME);
    }
}
