package com.ss.editor.shader.nodes.editor.state;

import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.shader.node.main.AttributeShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.WorldShaderNodeElement;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The implementation of storing state of {@link com.jme3.material.TechniqueDef}.
 *
 * @author JavaSaBr
 */
public class TechniqueDefState extends AbstractEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;

    /**
     * The name of technique definition.
     */
    @NotNull
    private String name;

    /**
     * The list of shader node states.
     */
    @NotNull
    private List<ShaderNodeState> shaderNodeStates;

    /**
     * The list of shader variable states.
     */
    @NotNull
    private List<ShaderNodeVariableState> shaderVariableStates;

    public TechniqueDefState(@NotNull final String name) {
        this.name = name;
        this.shaderNodeStates = new ArrayList<>();
        this.shaderVariableStates = new ArrayList<>();
    }

    /**
     * Remove all not exists states.
     *
     * @param materialDef  the material definition.
     * @param techniqueDef the technique definition.
     */
    @FXThread
    public void cleanUp(@NotNull final MaterialDef materialDef, @NotNull final TechniqueDef techniqueDef) {

        for (Iterator<ShaderNodeVariableState> iterator = shaderVariableStates.iterator(); iterator.hasNext(); ) {

            final ShaderNodeVariableState state = iterator.next();

            if (MaterialShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final MatParam parameter = ShaderNodeUtils.findMatParameterByName(materialDef, state.getName());

                if (parameter == null) {
                    iterator.remove();
                    notifyChange();
                }

            } else if (WorldShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final UniformBinding binding = ShaderNodeUtils.findWorldBindingByName(techniqueDef, state.getName());

                if (binding == null) {
                    iterator.remove();
                    notifyChange();
                }

            } else if (AttributeShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final ShaderNodeVariable attribute = ShaderNodeUtils.findAttributeByName(techniqueDef, state.getName());

                if (attribute == null) {
                    iterator.remove();
                    notifyChange();
                }
            }
        }

        for (Iterator<ShaderNodeState> iterator = shaderNodeStates.iterator(); iterator.hasNext(); ) {

            final ShaderNodeState state = iterator.next();
            final ShaderNode shaderNode = ShaderNodeUtils.findByName(techniqueDef, state.getName());

            if (shaderNode == null) {
                iterator.remove();
                notifyChange();
            }
        }
    }

    /**
     * Get the name of technique definition.
     *
     * @return the name of technique definition.
     */
    @FXThread
    public @NotNull String getName() {
        return name;
    }
}
