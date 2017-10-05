package com.jme3.shader.glsl.parser.ast;

/**
 * The node to present a symbol.
 *
 * @author JavaSaBr
 */
public class SymbolASTNode extends ASTNode {

    @Override
    protected String getStringAttributes() {
        return getText();
    }
}
