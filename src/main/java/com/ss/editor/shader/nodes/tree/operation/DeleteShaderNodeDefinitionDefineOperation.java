package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefine;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefines;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to delete a define.
 *
 * @author JavaSaBr
 */
public class DeleteShaderNodeDefinitionDefineOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node definition defines.
     */
    @NotNull
    private final ShaderNodeDefinitionDefines defines;

    /**
     * The define.
     */
    @NotNull
    private final ShaderNodeDefinitionDefine define;

    /**
     * The previous position.
     */
    private int index;

    public DeleteShaderNodeDefinitionDefineOperation(@NotNull final ShaderNodeDefinitionDefines defines,
                                                     @NotNull final ShaderNodeDefinitionDefine define) {
        this.defines = defines;
        this.define = define;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        index = defines.indexOf(define);
        defines.remove(define);
        editor.notifyFXRemovedChild(defines, define);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        defines.add(index, define);
        editor.notifyFXAddedChild(defines, define, index, false);
    }
}
