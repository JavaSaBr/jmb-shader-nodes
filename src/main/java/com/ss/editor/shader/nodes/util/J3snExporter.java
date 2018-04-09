package com.ss.editor.shader.nodes.util;

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
    public void export(@NotNull List<ShaderNodeDefinition> definitions, @NotNull OutputStream out) {

        var builder = new StringBuilder();
        builder.append("ShaderNodeDefinitions {\n");
        definitions.forEach(definition -> write(definition, builder));
        builder.append("}");

        var result = builder.toString();
        try {
            out.write(result.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the definition.
     *
     * @param definition the definition.
     * @param builder    the builder.
     */
    private void write(@NotNull ShaderNodeDefinition definition, @NotNull StringBuilder builder) {
        indent(builder, 1);

        builder.append("ShaderNodeDefinition ")
                .append(definition.getName())
                .append(" {\n");

        var type = definition.getType();

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        var documentation = definition.getDocumentation();

        var inputs = definition.getInputs();
        var outputs = definition.getOutputs();

        indent(builder, 2);

        builder.append("Type: ")
                .append(type.name())
                .append("\n\n");

        for (var i = 0; i < shadersPath.size(); i++) {

            var path = shadersPath.get(i);
            var language = shadersLanguage.get(i);

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
            builder.append("Documentation {").append('\n');
            indent(builder, 3);
            builder.append(documentation).append('\n');
            indent(builder, 2);
            builder.append("}\n");
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
    private void write(
            @NotNull List<ShaderNodeVariable> variables,
            @NotNull StringBuilder builder,
            @NotNull String name
    ) {

        indent(builder, 2);
        builder.append(name).append(" {\n");

        for (var variable : variables) {
            indent(builder, 3);

            var defaultValue = variable.getDefaultValue();

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
    private void writeStrings(
            @NotNull List<String> values,
            @NotNull StringBuilder builder,
            @NotNull String name
    ) {

        indent(builder, 2);
        builder.append(name).append(" {\n");

        for (var value : values) {
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
    private void indent(@NotNull StringBuilder builder, int level) {

        if (level < 1) {
            return;
        }

        var count = level * 4;

        builder.ensureCapacity(builder.length() + count);

        for (var i = 0; i < count; i++) {
            builder.append(' ');
        }
    }
}
