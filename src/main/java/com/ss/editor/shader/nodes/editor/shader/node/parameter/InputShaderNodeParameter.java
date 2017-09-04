package com.ss.editor.shader.nodes.editor.shader.node.parameter;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_INPUT_PARAMETER;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of input shader node parameter.
 *
 * @author JavaSaBr
 */
public class InputShaderNodeParameter extends ShaderNodeParameter {

    public InputShaderNodeParameter(@NotNull final ShaderNodeVariable variable) {
        super(variable);
        FXUtils.addClassTo(this, CSS_SHADER_NODE_INPUT_PARAMETER);
    }

    @Override
    protected void createContent() {
        super.createContent();

        FXUtils.addToPane(getSocket(), this);
        FXUtils.addToPane(getNameLabel(), this);
        FXUtils.addToPane(getTypeLabel(), this);
    }
}
