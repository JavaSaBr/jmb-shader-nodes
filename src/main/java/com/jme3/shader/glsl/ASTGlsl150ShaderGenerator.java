package com.jme3.shader.glsl;

import com.jme3.asset.AssetManager;
import com.jme3.shader.Shader.ShaderType;

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
    protected int getVersion(final ShaderType type) {
        return 150;
    }
}
