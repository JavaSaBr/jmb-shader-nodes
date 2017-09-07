package com.ss.editor.shader.nodes.editor.operation;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of attaching new parameter to a shader node.
 *
 * @author JavaSaBr
 */
public class AttachParameterToShaderNodeOperation extends ShaderNodeOperation {

    @NotNull
    private final ShaderNode shaderNode;

    @Nullable
    private final VariableMapping newMapping;

    @Nullable
    private final VariableMapping oldMapping;

    public AttachParameterToShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                                @Nullable final VariableMapping newMapping,
                                                @Nullable final VariableMapping oldMapping) {
        this.shaderNode = shaderNode;
        this.newMapping = newMapping;
        this.oldMapping = oldMapping;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

        if (oldMapping != null) {
            inputMapping.remove(oldMapping);
        }

        if (newMapping != null) {
            inputMapping.add(newMapping);
        }
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        notify(editor, oldMapping, newMapping);
    }


    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

        if (newMapping != null) {
            inputMapping.remove(newMapping);
        }

        if (oldMapping != null) {
            inputMapping.add(oldMapping);
        }
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(final @NotNull ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        notify(editor, newMapping, oldMapping);
    }

    private void notify(@NotNull final ShaderNodesChangeConsumer editor, @Nullable final VariableMapping oldMapping,
                        @Nullable final VariableMapping newMapping) {

        if (oldMapping != null && newMapping != null) {
            editor.notifyReplacedMapping(shaderNode, oldMapping, newMapping);
        } else if (oldMapping != null) {
            editor.notifyRemoveMapping(shaderNode, oldMapping);
        } else if (newMapping != null) {
            editor.notifyAddMapping(shaderNode, newMapping);
        }
    }
}
