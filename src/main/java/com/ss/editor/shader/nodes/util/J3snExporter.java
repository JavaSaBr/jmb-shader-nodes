package com.ss.editor.shader.nodes.util;

import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.rlib.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * The exporter a list of shader node definitions to a j3sn file.
 *
 * @author JavaSaBr
 */
public class J3snExporter {

    @NotNull
    private static final J3snExporter INSTANCE = new J3snExporter();

    public static @NotNull J3snExporter getInstance() {
        return INSTANCE;
    }

    /**
     * Export the list of shader node definitions as a j3sn file.
     *
     * @param definitions the definitions.
     * @param out         the output stream.
     */
    public void export(@NotNull final List<ShaderNodeDefinition> definitions, @NotNull final OutputStream out) {

        final StringBuilder builder = new StringBuilder();
        builder.append("ShaderNodeDefinitions {\n");
        definitions.forEach(definition -> write(definition, builder));
        builder.append("}");

        final String result = builder.toString();
        try {
            out.write(result.getBytes("UTF-8"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the definition.
     *
     * @param definition the definition.
     * @param builder    the builder.
     */
    private void write(@NotNull final ShaderNodeDefinition definition, @NotNull final StringBuilder builder) {
        indent(builder, 1);

        builder.append("ShaderNodeDefinition ")
                .append(definition.getName())
                .append(" {\n");

        final Shader.ShaderType type = definition.getType();

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        final String documentation = definition.getDocumentation();

        final List<ShaderNodeVariable> inputs = definition.getInputs();
        final List<ShaderNodeVariable> outputs = definition.getOutputs();

        final List<String> defines = definition.getDefines();
        final List<String> imports = definition.getImports();

        indent(builder, 2);

        builder.append("Type: ")
                .append(type.name())
                .append("\n\n");

        for (int i = 0; i < shadersPath.size(); i++) {

            final String path = shadersPath.get(i);
            final String language = shadersLanguage.get(i);

            indent(builder, 2);

            builder.append("Shader ")
                    .append(language)
                    .append(": ")
                    .append(path)
                    .append('\n');
        }

        if (!StringUtils.isEmpty(documentation)) {
            builder.append('\n');
            indent(builder, 2);
            builder.append("Documentation {");
            indent(builder, 3);
            builder.append(documentation);
            indent(builder, 2);
            builder.append("}\n");
        }

        if (!defines.isEmpty()) {
            builder.append('\n');
            writeStrings(defines, builder, "Defines");
        }

        if (!imports.isEmpty()) {
            builder.append('\n');
            writeStrings(imports, builder, "Imports");
        }

        if (!inputs.isEmpty()) {
            builder.append('\n');
            write(inputs, builder, "Input");
        }

        if (!outputs.isEmpty()) {
            builder.append('\n');
            write(outputs, builder, "Output");
        }

        indent(builder, 1);
        builder.append("}\n");
    }

    /**
     * Write the list of parameters.
     *
     * @param variables the list of parameters.
     * @param builder   the builder.
     * @param name      the name of parameters node.
     */
    private void write(@NotNull final List<ShaderNodeVariable> variables, @NotNull final StringBuilder builder,
                       @NotNull final String name) {

        indent(builder, 2);
        builder.append(name).append(" {\n");

        for (final ShaderNodeVariable variable : variables) {
            indent(builder, 3);

            final String defaultValue = variable.getDefaultValue();

            builder.append(variable.getType())
                    .append(' ')
                    .append(variable.getName());

            if (defaultValue != null && !defaultValue.isEmpty()) {
                builder.append(' ').append(defaultValue);
            }

            builder.append('\n');
        }

        indent(builder, 2);
        builder.append("}\n");
    }

    /**
     * Write the list of string values.
     *
     * @param values  the list of string values.
     * @param builder the builder.
     * @param name    the name of node.
     */
    private void writeStrings(@NotNull final List<String> values, @NotNull final StringBuilder builder,
                              @NotNull final String name) {

        indent(builder, 2);
        builder.append(name).append(" {\n");

        for (final String value : values) {
            indent(builder, 3);
            builder.append(value).append('\n');
        }

        indent(builder, 2);
        builder.append("}\n");
    }

    /**
     * Add an indent to the builder.
     *
     * @param builder the builder.
     * @param level   the level.
     */
    private void indent(@NotNull final StringBuilder builder, final int level) {
        if (level < 1) return;

        int count = level * 4;

        builder.ensureCapacity(builder.length() + count);

        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }
    }
}
