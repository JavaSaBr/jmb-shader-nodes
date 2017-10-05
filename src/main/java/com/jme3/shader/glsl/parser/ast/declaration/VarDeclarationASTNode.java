package com.jme3.shader.glsl.parser.ast.declaration;

import com.jme3.shader.glsl.parser.ast.AssignASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.TypeASTNode;

/**
 * The node to present a variable declaration in the code.
 *
 * @author JavaSaBr
 */
public class VarDeclarationASTNode extends DeclarationASTNode {

    /**
     * The type.
     */
    private TypeASTNode type;

    /**
     * The name.
     */
    private NameASTNode name;

    /**
     * The assign.
     */
    private AssignASTNode assign;

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public TypeASTNode getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type.
     */
    public void setType(final TypeASTNode type) {
        this.type = type;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public NameASTNode getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name.
     */
    public void setName(final NameASTNode name) {
        this.name = name;
    }

    /**
     * Gets the assign.
     *
     * @return the assign.
     */
    public AssignASTNode getAssign() {
        return assign;
    }

    /**
     * Sets the assign.
     *
     * @param assign the assign.
     */
    public void setAssign(final AssignASTNode assign) {
        this.assign = assign;
    }
}
