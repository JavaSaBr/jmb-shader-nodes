package com.ss.editor.shader.nodes.editor;

import com.ss.editor.state.editor.impl.material.MaterialEditor3DState;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the 3D part of the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditor3DState extends MaterialEditor3DState<ShaderNodesFileEditor>  {

    public ShaderNodesEditor3DState(@NotNull final ShaderNodesFileEditor fileEditor) {
        super(fileEditor);
    }
}
