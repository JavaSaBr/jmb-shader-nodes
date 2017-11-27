package com.ss.editor.shader.nodes.util;

import static java.util.stream.Collectors.toList;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.scene.VertexBuffer;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.util.GLSLType;
import com.ss.rlib.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * The utility class.
 *
 * @author JavaSaBr
 */
public class ShaderNodeUtils {

    /**
     * Find a material parameter with the name.
     *
     * @param materialDef the technique definition.
     * @param name         the name.
     * @return the shader nodes or null.
     */
    @FromAnyThread
    public static @Nullable MatParam findMatParameterByName(@NotNull final MaterialDef materialDef,
                                                            @NotNull final String name) {
        return materialDef.getMaterialParams().stream()
                .filter(matParam -> matParam.getName().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Find a world binding with the name.
     *
     * @param techniqueDef the technique definition.
     * @param name         the name.
     * @return the shader nodes or null.
     */
    @FromAnyThread
    public static @Nullable UniformBinding findWorldBindingByName(@NotNull final TechniqueDef techniqueDef,
                                                                  @NotNull final String name) {
        return techniqueDef.getWorldBindings().stream()
                .filter(binding -> binding.name().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Find an attribute with the name.
     *
     * @param techniqueDef the technique definition.
     * @param name         the name.
     * @return the shader nodes or null.
     */
    @FromAnyThread
    public static @Nullable ShaderNodeVariable findAttributeByName(@NotNull final TechniqueDef techniqueDef,
                                                                   @NotNull final String name) {
        return techniqueDef.getShaderGenerationInfo().getAttributes().stream()
                .filter(variable -> variable.getName().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Find a shader nodes with the name.
     *
     * @param techniqueDef the technique definition.
     * @param name         the name.
     * @return the shader nodes or null.
     */
    @FromAnyThread
    public static @Nullable ShaderNode findByName(@NotNull final TechniqueDef techniqueDef,
                                                  @NotNull final String name) {
        return techniqueDef.getShaderNodes().stream()
                .filter(shaderNode -> shaderNode.getName().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Compare two variables by name and namespace.
     *
     * @param first  the first variable.
     * @param second the second variable.
     * @return true of the variables are equal.
     */
    @FromAnyThread
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
    @FromAnyThread
    public static boolean equalsByName(@NotNull final ShaderNodeVariable first,
                                       @NotNull final ShaderNodeVariable second) {
        return StringUtils.equals(first.getName(), second.getName());
    }

    /**
     * Find an output mapping with the left variable by name and namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @Nullable VariableMapping findOutMappingByNNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                      @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getOutputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find an input mapping with the left variable by the name.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @Nullable VariableMapping findInMappingByNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                    @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByName(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find an input mapping with the right variable by the name and the namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the right variable.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @Nullable VariableMapping findInMappingByNNRightVar(@NotNull final ShaderNode shaderNode,
                                                                      @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getRightVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find input mappings with the right variable by the name and the namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the right variable.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @NotNull List<VariableMapping> findInMappingsByNNRightVar(@NotNull final ShaderNode shaderNode,
                                                                            @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getRightVariable(), variable))
                .collect(toList());
    }

    /**
     * Find input mappings with the right variable by the name and the namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the right variable.
     * @param nameSpace  the name space.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @NotNull List<VariableMapping> findInMappingsByNNRightVar(@NotNull final ShaderNode shaderNode,
                                                                            @NotNull final ShaderNodeVariable variable,
                                                                            @NotNull final String nameSpace) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByName(mapping.getRightVariable(), variable))
                .filter(mapping -> mapping.getRightVariable().getNameSpace().equals(nameSpace))
                .collect(toList());
    }

    /**
     * Find an input mapping with the left variable by the name and the namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the left variable.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @Nullable VariableMapping findInMappingByNNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                     @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable))
                .findAny().orElse(null);
    }

    /**
     * Find an input mapping with the left variable by the name and the namespace.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the left variable.
     * @param nameSpace  the name space.
     * @return the mapping or null.
     */
    @FromAnyThread
    public static @Nullable VariableMapping findInMappingByNNLeftVar(@NotNull final ShaderNode shaderNode,
                                                                     @NotNull final ShaderNodeVariable variable,
                                                                     @NotNull final String nameSpace) {
        return shaderNode.getInputMapping().stream()
                .filter(mapping -> equalsByName(mapping.getLeftVariable(), variable))
                .filter(mapping -> mapping.getLeftVariable().getNameSpace().equals(nameSpace))
                .findAny().orElse(null);
    }

    /**
     * Check the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the left variable.
     * @return true if the shader node has the output mapping with the left variable.
     */
    @FromAnyThread
    public static boolean hasOutMappingByLeftVar(@NotNull final ShaderNode shaderNode,
                                                 @NotNull final ShaderNodeVariable variable) {
        return shaderNode.getOutputMapping()
                .stream().anyMatch(mapping -> equalsByNameAndNameSpace(mapping.getLeftVariable(), variable));
    }

    /**
     * Check the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @param variable   the right variable.
     * @return true if the shader nodes has the input mapping with the right variable.
     */
    @FromAnyThread
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
    @FromAnyThread
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
            inNameSpace = ((ShaderNode) inObject).getName();
        } else {
            inNameSpace = inVar.getNameSpace();
        }

        final String outNameSpace;

        if (outObject instanceof ShaderNode) {
            outNameSpace = ((ShaderNode) outObject).getName();
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

    /**
     * Get the UI type of the attribute.
     *
     * @param attribute the attribute.
     * @return the UI type.
     */
    @FromAnyThread
    public static @NotNull String getAttributeUIType(@NotNull final VertexBuffer.Type attribute) {
        switch (attribute) {
            case BoneWeight:
            case BindPoseNormal:
            case Binormal:
            case Normal: {
                return "Vector 3 x Float";
            }
            case Size: {
                return "Float";
            }
            case Position:
            case BindPosePosition:
            case BindPoseTangent:
            case Tangent: {
                return "Vector 4 x Float";
            }
            case Color: {
                return "Color";
            }
            case InterleavedData: {
                return "Integer";
            }
            case Index: {
                return "Unsigned Integer";
            }
            case BoneIndex: {
                return "Vector 4 x Unsigned Integer";
            }
            case TexCoord:
            case TexCoord2:
            case TexCoord3:
            case TexCoord4:
            case TexCoord5:
            case TexCoord6:
            case TexCoord7:
            case TexCoord8: {
                return "Vector 2 x Float";
            }
        }
        throw new RuntimeException("unknown attribute " + attribute);
    }

    /**
     * Convert UI type of shader to real type.
     *
     * @param type the UI type.
     * @return the real type.
     */
    @FromAnyThread
    public static @NotNull String uiTypeToType(@NotNull final String type) {
        switch (type) {
            case "Vector 4 x Unsigned Integer":
                return "uvec4";
            case "Color":
                return "vec4";
            case "Vector 4 x Float":
                return "vec4";
            case "Vector 3 x Float":
                return "vec3";
            case "Vector 2 x Float":
                return "vec2";
            case "Unsigned Integer":
                return "uint";
            case "Integer":
                return "int";
            case "Float":
                return "float";
            default:
                return type;
        }
    }

    /**
     * Check the list of variables to contain a variable with the name and the namespace.
     *
     * @param variables the variables.
     * @param name      the name.
     * @param nameSpace the namespace.
     * @return true of the list contains it.
     */
    @FromAnyThread
    public static boolean containsByNN(@NotNull final Collection<ShaderNodeVariable> variables,
                                       @NotNull final String name, @NotNull final String nameSpace) {

        for (final ShaderNodeVariable variable : variables) {

            if (!StringUtils.equals(variable.getNameSpace(), nameSpace)) {
                continue;
            } else if (!StringUtils.equals(variable.getName(), name)) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Check is the in type accessible for the out type.
     *
     * @param inType  the in type.
     * @param outType the out type.
     * @return true if these types are accessible.
     */
    @FromAnyThread
    public static boolean isAccessibleType(@NotNull final String inType, @NotNull final String outType) {

        if (!inType.contains("|") && !outType.contains("|")) {
            return inType.equals(outType);
        }

        final String[] inTypes = inType.split("[|]");
        final String[] outTypes = outType.split("[|]");

        for (final String subInType : inTypes) {
            for (final String subOutType : outTypes) {
                if (subInType.equals(subOutType)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check the variable is required this.
     *
     * @param variable the variable.
     * @return true of this variable is required.
     */
    @FromAnyThread
    public static boolean isRequired(@NotNull final ShaderNodeVariable variable) {

        final GLSLType glslType = GLSLType.ofRawType(variable.getType());

        switch (glslType) {
            case SAMPLER_2D:
            case SAMPLER_CUBE: {
                return false;
            }
        }

        return StringUtils.isEmpty(variable.getDefaultValue());
    }

    /**
     * Try to calculate a right swizzling for the mapping.
     *
     * @param leftVar  the left variable.
     * @param rightVar the right variable.
     * @return the right swizzling or null.
     */
    @FromAnyThread
    public static @NotNull String calculateRightSwizzling(@NotNull final ShaderNodeVariable leftVar,
                                                          @NotNull final ShaderNodeVariable rightVar) {

        final String leftType = leftVar.getType();
        final String rightType = rightVar.getType();

        if (leftType == null || rightType == null) {
            return "";
        }

        switch (rightType) {
            case "vec4": {
                switch (leftType) {
                    case "vec3":
                        return "xyz";
                    case "vec2":
                        return "xy";
                    case "float":
                        return "x";
                }
                break;
            }
            case "vec3": {
                switch (leftType) {
                    case "vec2":
                        return "xy";
                    case "float":
                        return "x";
                }
                break;
            }
            case "vec2": {
                switch (leftType) {
                    case "float":
                        return "x";
                }
                break;
            }
        }

        return "";
    }

    /**
     * Try to calculate a left swizzling for the mapping.
     *
     * @param leftVar  the left variable.
     * @param rightVar the right variable.
     * @return the left swizzling or null.
     */
    @FromAnyThread
    public static @NotNull String calculateLeftSwizzling(@NotNull final ShaderNodeVariable leftVar,
                                                         @NotNull final ShaderNodeVariable rightVar) {

        //FIXME
        if (true) {
            return "";
        }

        final String leftType = leftVar.getType();
        final String rightType = rightVar.getType();

        if (leftType == null || rightType == null) {
            return "";
        }

        switch (leftType) {
            case "vec4": {
                switch (rightType) {
                    case "float":
                        return "xyzw";
                }
                break;
            }
            case "vec3": {
                switch (rightType) {
                    case "float":
                        return "xyz";
                }
                break;
            }
            case "vec2": {
                switch (rightType) {
                    case "float":
                        return "xy";
                }
                break;
            }
        }

        return "";
    }
}
