package com.jme3.shader.glsl.parser.ast.declaration;

/**
 * The node to present an import declaration in the code.
 *
 * @author JavaSaBr
 */
public class ImportDeclarationASTNode extends DeclarationASTNode {

    @Override
    protected String getStringAttributes() {
        return "#import";
    }
}
