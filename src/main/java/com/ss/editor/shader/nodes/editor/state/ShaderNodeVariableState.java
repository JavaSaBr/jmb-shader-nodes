package com.ss.editor.shader.nodes.editor.state;

import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;

/**
 * The implementation of storing state of {@link com.jme3.shader.ShaderNodeVariable}.
 *
 * @author JavaSaBr
 */
public class ShaderNodeVariableState extends AbstractEditorState {

    private String name;

    private String nameSpace;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
    }
}
