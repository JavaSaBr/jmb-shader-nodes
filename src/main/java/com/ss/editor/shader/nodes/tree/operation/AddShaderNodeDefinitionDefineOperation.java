package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefine;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefines;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to add a local variable.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeDefinitionDefineOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node local variables.
     */
    @NotNull
    private final ShaderNodeDefinitionDefines defines;

    /**
     * The shader node local variable.
     */
    @NotNull
    private final ShaderNodeDefinitionDefine define;

    public AddShaderNodeDefinitionDefineOperation(@NotNull final ShaderNodeDefinitionDefines defines,
                                                  @NotNull final ShaderNodeDefinitionDefine define) {
        this.defines = defines;
        this.define = define;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        defines.add(define);
        editor.notifyFXAddedChild(defines, define, -1, true);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        defines.remove(define);
        editor.notifyFXRemovedChild(defines, define);
    }
}
