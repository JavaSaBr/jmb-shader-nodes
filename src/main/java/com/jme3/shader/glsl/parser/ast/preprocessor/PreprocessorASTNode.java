package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.ast.ASTNode;

public class PreprocessorASTNode extends ASTNode {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
