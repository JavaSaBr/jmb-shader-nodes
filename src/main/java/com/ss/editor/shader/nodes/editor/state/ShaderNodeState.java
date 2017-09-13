package com.ss.editor.shader.nodes.editor.state;

import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;

/**
 * The implementation of storing state of {@link com.jme3.shader.ShaderNodeVariable}.
 *
 * @author JavaSaBr
 */
public class ShaderNodeState extends AbstractEditorState {

    private String name;

    public String getName() {
        return name;
    }
}
