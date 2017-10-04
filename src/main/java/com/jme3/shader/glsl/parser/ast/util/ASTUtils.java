package com.jme3.shader.glsl.parser.ast.util;

import com.jme3.shader.glsl.parser.ast.ASTNode;

/**
 * The utility class to work with AST nodes.
 *
 * @author JavaSaBr
 */
public class ASTUtils {

    /**
     * Checks of existing a parent of the type.
     *
     * @param node the node.
     * @param type the type of a parent.
     * @return true if a parent of the type is exists.
     */
    public static boolean hasParentByType(final ASTNode node, final Class<? extends ASTNode> type) {

        ASTNode parent = node.getParent();
        while (parent != null) {
            if (type.isInstance(parent)) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }
}
