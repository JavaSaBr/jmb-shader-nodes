package com.ss.editor.shader.nodes.util;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.OutputShaderNodeParameter;
import com.ss.rlib.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The utility class.
 *
 * @author JavaSaBr
 */
public class ShaderNodeUtils {

    /**
     * Compare two variables by name and namespace.
     *
     * @param first  the first variable.
     * @param second the second variable.
     * @return true of the variables are equal.
     */
    public static boolean equalsByNameAndNameSpace(@NotNull final ShaderNodeVariable first,
                                                   @NotNull final ShaderNodeVariable second) {
        return StringUtils.equals(first.getName(), second.getName()) &&
                StringUtils.equals(first.getNameSpace(), second.getNameSpace());
    }

    /**
     * Compare two variables by name.
     *
     * @param first  the first variable.
     * @param second the second variable.
     * @return true of the variables are equal.
     */
    public static boolean equalsByName(@NotNull final ShaderNodeVariable first,
                                       @NotNull final ShaderNodeVariable second) {
        return StringUtils.equals(first.getName(), second.getName());
    }

    /**
     * Find an output mapping with the left variable by name and namespace.
     *
     * @param shaderNode the shader node.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    public static @Nullable VariableMapping findOutMappingByNNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                      @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getOutputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find an input mapping with the left variable by name.
     *
     * @param shaderNode the shader node.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    public static @Nullable VariableMapping findInMappingByNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                    @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByName(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find an input mapping with the left variable by name and namespace.
     *
     * @param shaderNode the shader node.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    public static @Nullable VariableMapping findInMappingByNNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                    @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Check the shader node.
     *
     * @param shaderNode the shader node.
     * @param variable   the left variable.
     * @return true if the shader node has the output mapping with the left variable.
     */
    public static boolean hasOutMappingByLeftVar(@NotNull final ShaderNode shaderNode,
                                                 @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getOutputMapping()
                .stream().anyMatch(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable));
    }

    /**
     * Check the shader node.
     *
     * @param shaderNode the shader node.
     * @param variable   the right variable.
     * @return true if the shader node has the input mapping with the right variable.
     */
    public static boolean hasInMappingByRightVar(@NotNull final ShaderNode shaderNode,
                                                 @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping()
                .stream().anyMatch(mapping -> equalsByNameAndNameSpace(mapping.getRightVariable(), variable));
    }

    /**
     * Make a new mapping between the parameters.
     *
     * @param inputParameter  the input parameter.
     * @param outputParameter the output parameter.
     * @return the new mapping.
     */
    public static @NotNull VariableMapping makeMapping(@NotNull final InputShaderNodeParameter inputParameter,
                                                       @NotNull final OutputShaderNodeParameter outputParameter) {

        final ShaderNodeElement<?> inElement = inputParameter.getNodeElement();
        final Object inObject = inElement.getObject();

        final ShaderNodeElement<?> outElement = outputParameter.getNodeElement();
        final Object outObject = outElement.getObject();

        final ShaderNodeVariable inVar = inputParameter.getVariable();
        final ShaderNodeVariable outVar = outputParameter.getVariable();

        final String inNameSpace;

        if (inObject instanceof ShaderNode) {
            inNameSpace = ((ShaderNode) inObject).getDefinition().getName();
        } else {
            inNameSpace = inVar.getNameSpace();
        }

        final String outNameSpace;

        if (outObject instanceof ShaderNode) {
            outNameSpace = ((ShaderNode) outObject).getDefinition().getName();
        } else {
            outNameSpace = outVar.getNameSpace();
        }

        final boolean isShaderOutput = inElement instanceof OutputGlobalShaderNodeElement;

        final VariableMapping newMapping = new VariableMapping();
        newMapping.setLeftVariable(new ShaderNodeVariable(inVar.getType(), inNameSpace, inVar.getName(),
                null, inVar.getPrefix()));
        newMapping.setRightVariable(new ShaderNodeVariable(outVar.getType(), outNameSpace, outVar.getName(),
                null, outVar.getPrefix()));

        newMapping.getLeftVariable().setShaderOutput(isShaderOutput);

        return newMapping;
    }
}
