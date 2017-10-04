package com.jme3.shader.glsl.parser.ast.declaration;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

public class ExternalFieldDeclarationASTNode extends FieldDeclarationASTNode {

    public enum ExternalFieldType {
        UNIFORM("uniform"),
        ATTRIBUTE("attribute", "in"),
        VARYING("varying", "out");

        private static final ExternalFieldType[] VALUES = values();

        public static ExternalFieldType forKeyWord(final String keyword) {
            for (final ExternalFieldType fieldType : VALUES) {
                if (fieldType.keywords.contains(keyword)) {
                    return fieldType;
                }
            }

            return null;
        }

        private Set<String> keywords;

        ExternalFieldType(final String... keywords) {
            this.keywords = new HashSet<>(asList(keywords));
        }
    }

    private ExternalFieldType fieldType;

    public ExternalFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(final ExternalFieldType fieldType) {
        this.fieldType = fieldType;
    }
}
