package com.jme3.shader.glsl;

import com.jme3.asset.AssetManager;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.Shader;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.ShaderNodeVariable;

/**
 * The implementation of a shader generator with using AST GLSL to generate a result shader.
 *
 * @author JavaSaBr
 */
public class ASTGlsl150ShaderGenerator extends ASTGlsl100ShaderGenerator {

    public ASTGlsl150ShaderGenerator(final AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    protected String getLanguageAndVersion(final ShaderType type) {
        return "GLSL150";
    }

    @Override
    protected int getVersion(final ShaderType type) {
        return 150;
    }

    @Override
    protected void declareVarying(final StringBuilder source, final ShaderNodeVariable var, final boolean input) {
        declareVariable(source, var, true, input ? "in" : "out");
    }

    @Override
    protected void declareAttribute(final StringBuilder source, final ShaderNodeVariable var) {
        declareVariable(source, var, false, "in");
    }

    @Override
    protected void generateUniforms(StringBuilder source, ShaderGenerationInfo info, ShaderType type) {
        generateCompatibilityDefines(source, type);
        super.generateUniforms(source, info, type);
    }

    @Override
    protected void generateStartOfMainSection(StringBuilder source, ShaderGenerationInfo info, Shader.ShaderType type) {
        source.append("\n");

        if (type == Shader.ShaderType.Fragment) {
            for (ShaderNodeVariable global : info.getFragmentGlobals()) {
                declareVariable(source, global, null, true, "out");
            }
        }
        source.append("\n");

        appendIndent(source);
        source.append("void main() {\n");
        indent();

        if (type == Shader.ShaderType.Vertex) {
            declareGlobalPosition(info, source);
        } else if (type == Shader.ShaderType.Fragment) {
            for (ShaderNodeVariable global : info.getFragmentGlobals()) {
                initVariable(source, global, "vec4(1.0)");
            }
        }
    }

    @Override
    protected void generateEndOfMainSection(StringBuilder source, ShaderGenerationInfo info, Shader.ShaderType type) {
        if (type == Shader.ShaderType.Vertex) {
            appendOutput(source, "gl_Position", info.getVertexGlobal());
        }
        unIndent();
        appendIndent(source);
        source.append("}\n");
    }

    private void generateCompatibilityDefines(StringBuilder source, ShaderType type) {
        //Adding compatibility defines, as it's more efficient than replacing the function calls in the source code
        if (type == ShaderType.Fragment) {
            source.append("#define texture1D texture\n")
                    .append("#define texture2D texture\n")
                    .append("#define texture3D texture\n")
                    .append("#define textureCube texture\n")
                    .append("#define texture2DLod textureLod\n")
                    .append("#define textureCubeLod textureLod\n");
        }
    }

    /**
     * Append a variable initialization to the code
     *
     * @param source the StringBuilder to use
     * @param var the variable to initialize
     * @param initValue the init value to assign to the variable
     */
    protected void initVariable(StringBuilder source, ShaderNodeVariable var, String initValue) {
        appendIndent(source);
        source.append(var.getNameSpace());
        source.append("_");
        source.append(var.getName());
        source.append(" = ");
        source.append(initValue);
        source.append(";\n");
    }
}
