package com.ss.editor.shader.nodes.ui.component.editor.state;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.AttributeShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.WorldShaderNodeElement;
import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
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

    @NotNull
    private static final Logger LOGGER = LoggerManager.getLogger(TechniqueDefState.class);

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
     * The list of shader nodes states.
     */
    @NotNull
    private List<ShaderNodeState> shaderNodeStates;

    /**
     * The list of shader variable states.
     */
    @NotNull
    private List<ShaderNodeVariableState> shaderVariableStates;

    /**
     * The location of output nodes.
     */
    @NotNull
    private Vector2f outputNodeLocation;

    /**
     * The location of input nodes.
     */
    @NotNull
    private Vector2f inputNodeLocation;

    /**
     * The width of output nodes.
     */
    private int outputNodeWidth;

    /**
     * The width of input nodes.
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
    @FxThread
    public void cleanUp(@NotNull final MaterialDef materialDef, @NotNull final TechniqueDef techniqueDef) {

        for (final Iterator<ShaderNodeVariableState> iterator = shaderVariableStates.iterator(); iterator.hasNext(); ) {

            final ShaderNodeVariableState state = iterator.next();

            if (MaterialShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final MatParam parameter = findMatParameterByName(materialDef, state.getName());

                if (parameter == null) {
                    iterator.remove();
                    notifyChange();
                }

            } else if (WorldShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final UniformBinding binding = findWorldBindingByName(techniqueDef, state.getName());

                if (binding == null) {
                    iterator.remove();
                    notifyChange();
                }

            } else if (AttributeShaderNodeElement.NAMESPACE.equals(state.getNameSpace())) {

                final ShaderNodeVariable attribute = findAttributeByName(techniqueDef, state.getName());

                if (attribute == null) {
                    iterator.remove();
                    notifyChange();
                }
            }
        }

        for (final Iterator<ShaderNodeState> iterator = shaderNodeStates.iterator(); iterator.hasNext(); ) {

            final ShaderNodeState state = iterator.next();
            final ShaderNode shaderNode = findByName(techniqueDef, state.getName());

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
     * @param width    the width.
     */
    @FxThread
    public void notifyChange(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location,
                             final double width) {

        LOGGER.debug(variable, location, (var, pos) -> "Changed shader node variable: " + var + " to location " + pos);

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

        LOGGER.debug(this, defState -> "New version of tech def state is:" + defState);

        notifyChange();
    }

    /**
     * Notify about changes the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @param location   the location.
     * @param width      the width.
     */
    @FxThread
    public void notifyChange(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location,
                             final double width) {

        LOGGER.debug(shaderNode, location, (node, pos) -> "Changed shader node: " + node + " to location " + pos);

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

        LOGGER.debug(this, defState -> "New version of tech def state is:" + defState);

        notifyChange();
    }

    /**
     * Find a state of the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @return the state or null.
     */
    @FxThread
    public @Nullable ShaderNodeState getState(@NotNull final ShaderNode shaderNode) {
        return shaderNodeStates.stream()
                .filter(varState -> varState.getName().equals(shaderNode.getName()))
                .findAny().orElse(null);
    }

    /**
     * Find a state of the shader nodes variable.
     *
     * @param variable the shader nodes variable.
     * @return the state or null.
     */
    @FxThread
    public @Nullable ShaderNodeVariableState getState(@Nullable final ShaderNodeVariable variable) {
        if (variable == null) return null;
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
    @FxThread
    public @NotNull String getName() {
        return name;
    }

    /**
     * Get the location of input nodes.
     *
     * @return the location of input nodes.
     */
    @FxThread
    public @NotNull Vector2f getInputNodeLocation() {
        return inputNodeLocation;
    }

    /**
     * Get the location of output nodes.
     *
     * @return the location of output nodes.
     */
    @FxThread
    public @NotNull Vector2f getOutputNodeLocation() {
        return outputNodeLocation;
    }

    /**
     * Set the location of input nodes.
     *
     * @param inputNodeLocation the location of input nodes.
     */
    @FxThread
    public void setInputNodeLocation(@NotNull final Vector2f inputNodeLocation) {
        final Vector2f prev = getInputNodeLocation();
        this.inputNodeLocation = inputNodeLocation;
        if (!prev.equals(inputNodeLocation)) notifyChange();
    }

    /**
     * Set the location of output nodes.
     *
     * @param outputNodeLocation the location of output nodes.
     */
    @FxThread
    public void setOutputNodeLocation(@NotNull final Vector2f outputNodeLocation) {
        final Vector2f prev = getOutputNodeLocation();
        this.outputNodeLocation = outputNodeLocation;
        if (!prev.equals(outputNodeLocation)) notifyChange();
    }

    /**
     * Get the width of output nodes.
     *
     * @return the width of output nodes.
     */
    @FxThread
    public int getOutputNodeWidth() {
        return outputNodeWidth;
    }

    /**
     * Get the width of input nodes.
     *
     * @return the width of input nodes.
     */
    @FxThread
    public int getInputNodeWidth() {
        return inputNodeWidth;
    }

    /**
     * Set the width of output nodes.
     *
     * @param outputNodeWidth the width of output nodes.
     */
    @FxThread
    public void setOutputNodeWidth(final int outputNodeWidth) {
        final int prev = getOutputNodeWidth();
        this.outputNodeWidth = outputNodeWidth;
        if (prev != outputNodeWidth) notifyChange();
    }

    /**
     * Set the width of input nodes.
     *
     * @param inputNodeWidth the width of input nodes.
     */
    @FxThread
    public void setInputNodeWidth(final int inputNodeWidth) {
        final int prev = getInputNodeWidth();
        this.inputNodeWidth = inputNodeWidth;
        if (prev != inputNodeWidth) notifyChange();
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("TechniqueDefState");
        builder.append("(name:").append(name).append(")");

        if (!shaderNodeStates.isEmpty()) {
            builder.append("\n\t\tShader Nodes:\n");

            for (final ShaderNodeState state : shaderNodeStates) {
                builder.append("\t\t\t").append(state).append("\n");
            }

            builder.delete(builder.length() - 1, builder.length());
        }

        if (!shaderVariableStates.isEmpty()) {
            builder.append("\n\t\tShader Variables:\n");

            for (final ShaderNodeVariableState state : shaderVariableStates) {
                builder.append("\t\t\t").append(state).append("\n");
            }

            builder.delete(builder.length() - 1, builder.length());
        }

        return builder.toString();
    }
}
