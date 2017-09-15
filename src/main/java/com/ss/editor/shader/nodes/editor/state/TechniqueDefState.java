package com.ss.editor.shader.nodes.editor.state;

import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.component.shader.node.main.AttributeShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.node.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.node.main.WorldShaderNodeElement;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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

    /**
     * The location of output node.
     */
    @NotNull
    private Vector2f outputNodeLocation;

    /**
     * The location of input node.
     */
    @NotNull
    private Vector2f inputNodeLocation;

    /**
     * The width of output node.
     */
    private int outputNodeWidth;

    /**
     * The width of input node.
     */
    private int inputNodeWidth;

    public TechniqueDefState(@NotNull final String name) {
        this.name = name;
        this.shaderNodeStates = new ArrayList<>();
        this.shaderVariableStates = new ArrayList<>();
        this.inputNodeLocation = new Vector2f(10, 10);
        this.outputNodeLocation = new Vector2f(10, 10);
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
     * Notify about changed variable.
     *
     * @param variable the variable.
     * @param location the location.
     * @param width the width.
     */
    @FXThread
    public void notifyChange(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location,
                             final double width) {

        final Optional<ShaderNodeVariableState> state = shaderVariableStates.stream()
                .filter(variableState -> variableState.getNameSpace().equals(variable.getNameSpace()))
                .filter(variableState -> variableState.getName().equals(variable.getName()))
                .findAny();

        if (state.isPresent()) {
            final ShaderNodeVariableState variableState = state.get();
            variableState.setLocation(location);
            variableState.setWidth((int) width);
        } else {
            shaderVariableStates.add(new ShaderNodeVariableState(variable.getName(),
                    variable.getNameSpace(), location, (int) width));
        }

        notifyChange();
    }

    /**
     * Notify about changes the shader node.
     *
     * @param shaderNode the shader node.
     * @param location the location.
     * @param width the width.
     */
    @FXThread
    public void notifyChange(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location,
                             final double width) {

        final Optional<ShaderNodeState> state = shaderNodeStates.stream()
                .filter(variableState -> variableState.getName().equals(shaderNode.getName()))
                .findAny();

        if (state.isPresent()) {
            final ShaderNodeState nodeState = state.get();
            nodeState.setLocation(location);
            nodeState.setWidth((int) width);
        } else {
            shaderNodeStates.add(new ShaderNodeState(shaderNode.getName(), location, (int) width));
        }

        notifyChange();
    }

    /**
     * Find a state of the shader node.
     *
     * @param shaderNode the shader node.
     * @return the state or null.
     */
    @FXThread
    public @Nullable ShaderNodeState getState(@NotNull final ShaderNode shaderNode) {
        return shaderNodeStates.stream().filter(variableState -> variableState.getName().equals(shaderNode.getName()))
                .findAny().orElse(null);
    }

    /**
     * Find a state of the shader node variable.
     *
     * @param variable the shader node variable.
     * @return the state or null.
     */
    @FXThread
    public @Nullable ShaderNodeVariableState getState(@Nullable final ShaderNodeVariable variable) {
        return shaderVariableStates.stream()
                .filter(variableState -> variableState.getNameSpace().equals(variable.getNameSpace()))
                .filter(variableState -> variableState.getName().equals(variable.getName()))
                .findAny().orElse(null);
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

    /**
     * Get the location of input node.
     *
     * @return the location of input node.
     */
    @FXThread
    public @NotNull Vector2f getInputNodeLocation() {
        return inputNodeLocation;
    }

    /**
     * Get the location of output node.
     *
     * @return the location of output node.
     */
    @FXThread
    public @NotNull Vector2f getOutputNodeLocation() {
        return outputNodeLocation;
    }

    /**
     * Set the location of input node.
     *
     * @param inputNodeLocation the location of input node.
     */
    @FXThread
    public void setInputNodeLocation(@NotNull final Vector2f inputNodeLocation) {
        final Vector2f prev = getInputNodeLocation();
        this.inputNodeLocation = inputNodeLocation;
        if (!prev.equals(inputNodeLocation)) notifyChange();
    }

    /**
     * Set the location of output node.
     *
     * @param outputNodeLocation the location of output node.
     */
    @FXThread
    public void setOutputNodeLocation(@NotNull final Vector2f outputNodeLocation) {
        final Vector2f prev = getOutputNodeLocation();
        this.outputNodeLocation = outputNodeLocation;
        if (!prev.equals(outputNodeLocation)) notifyChange();
    }

    /**
     * Get the width of output node.
     *
     * @return the width of output node.
     */
    @FXThread
    public int getOutputNodeWidth() {
        return outputNodeWidth;
    }

    /**
     * Get the width of input node.
     *
     * @return the width of input node.
     */
    @FXThread
    public int getInputNodeWidth() {
        return inputNodeWidth;
    }

    /**
     * Set the width of output node.
     *
     * @param outputNodeWidth the width of output node.
     */
    @FXThread
    public void setOutputNodeWidth(final int outputNodeWidth) {
        final int prev = getOutputNodeWidth();
        this.outputNodeWidth = outputNodeWidth;
        if (prev != outputNodeWidth) notifyChange();
    }

    /**
     * Set the width of input node.
     *
     * @param inputNodeWidth the width of input node.
     */
    @FXThread
    public void setInputNodeWidth(final int inputNodeWidth) {
        final int prev = getInputNodeWidth();
        this.inputNodeWidth = inputNodeWidth;
        if (prev != inputNodeWidth) notifyChange();
    }

    @Override
    public String toString() {
        return "TechniqueDefState{" + "name='" + name + '\'' + ", shaderNodeStates=" + shaderNodeStates +
                ", shaderVariableStates=" + shaderVariableStates + ", outputNodeLocation=" + outputNodeLocation +
                ", inputNodeLocation=" + inputNodeLocation + ", outputNodeWidth=" + outputNodeWidth +
                ", inputNodeWidth=" + inputNodeWidth + '}';
    }
}
