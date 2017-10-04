package com.jme3.shader.glsl.parser.ast.declaration;

import com.jme3.shader.glsl.parser.ast.AssignASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.TypeASTNode;

public class VarDeclarationASTNode extends DeclarationASTNode {

    private TypeASTNode type;

    private NameASTNode name;

    private AssignASTNode assign;

    public TypeASTNode getType() {
        return type;
    }

    public void setType(final TypeASTNode type) {
        this.type = type;
    }

    public NameASTNode getName() {
        return name;
    }

    public void setName(final NameASTNode name) {
        this.name = name;
    }

    public AssignASTNode getAssign() {
        return assign;
    }

    public void setAssign(final AssignASTNode assign) {
        this.assign = assign;
    }
}
