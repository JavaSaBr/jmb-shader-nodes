package com.jme3.shader.glsl.parser.ast.declaration;

import static java.util.Arrays.asList;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.TypeASTNode;

import java.util.HashSet;
import java.util.Set;

public class FieldDeclarationASTNode extends DeclarationASTNode {

    public enum FieldType {
        UNIFORM("uniform"),
        ATTRIBUTE("attribute", "in"),
        VARYING("varying", "out");

        private static final FieldType[] VALUES = values();

        public static FieldType forKeyWord(final String keyword) {
            for (final FieldType fieldType : VALUES) {
                if (fieldType.keywords.contains(keyword)) {
                    return fieldType;
                }
            }

            return null;
        }

        private Set<String> keywords;

        FieldType(final String... keywords) {
            this.keywords = new HashSet<>(asList(keywords));
        }
    }

    private FieldType fieldType;

    private TypeASTNode type;

    private NameASTNode name;

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(final FieldType fieldType) {
        this.fieldType = fieldType;
    }

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
}
