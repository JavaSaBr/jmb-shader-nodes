package com.ss.editor.shader.nodes.editor.shader.node.global;

import com.jme3.material.ShaderGenerationInfo;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a global parameters.
 *
 * @author JavaSaBr
 */
public class GlobalShaderNodeElement extends ShaderNodeElement<ShaderGenerationInfo> {

    public GlobalShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderGenerationInfo object) {
        super(container, object);
    }
}
