package com.jme3.shader.glsl.parser.ast.declaration;

import com.jme3.shader.glsl.parser.ast.BodyASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.TypeASTNode;

/**
 * The node to present a method declaration in the code.
 *
 * @author JavaSaBr
 */
public class MethodDeclarationASTNode extends DeclarationASTNode {

    /**
     * The name.
     */
    private NameASTNode name;

    /**
     * The body.
     */
    private BodyASTNode body;

    /**
     * The return type.
     */
    private TypeASTNode returnType;

    /**
     * Gets the body.
     *
     * @return the body.
     */
    public BodyASTNode getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body the body.
     */
    public void setBody(final BodyASTNode body) {
        this.body = body;
    }

    /**
     * Gets the return type.
     *
     * @return the return type.
     */
    public TypeASTNode getReturnType() {
        return returnType;
    }

    /**
     * Sets the return type.
     *
     * @param returnType the return type.
     */
    public void setReturnType(final TypeASTNode returnType) {
        this.returnType = returnType;
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
}
