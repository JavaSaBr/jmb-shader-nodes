package com.jme3.shader.glsl.parser.ast;

public class ValueASTNode extends ASTNode {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
