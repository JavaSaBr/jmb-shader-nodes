package com.jme3.shader.glsl.parser.ast.declaration;

import com.jme3.shader.glsl.parser.ast.BodyASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.TypeASTNode;

public class MethodDeclarationASTNode extends DeclarationASTNode {

    private NameASTNode name;

    private BodyASTNode body;

    private TypeASTNode returnType;

    public BodyASTNode getBody() {
        return body;
    }

    public void setBody(final BodyASTNode body) {
        this.body = body;
    }

    public TypeASTNode getReturnType() {
        return returnType;
    }

    public void setReturnType(final TypeASTNode returnType) {
        this.returnType = returnType;
    }

    public NameASTNode getName() {
        return name;
    }

    public void setName(final NameASTNode name) {
        this.name = name;
    }
}
