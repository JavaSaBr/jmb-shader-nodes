package com.ss.editor.shader.nodes.tree.property;

import static com.ss.editor.extension.property.EditablePropertyType.READ_ONLY_STRING;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.extension.property.EditableProperty;
import com.ss.editor.extension.property.SimpleProperty;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.ui.control.property.builder.PropertyBuilder;
import com.ss.editor.ui.control.property.builder.impl.EditableObjectPropertyBuilder;
import com.ss.editor.util.GLSLType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The builder to build properties for shader nodes things.
 *
 * @author JavaSaBr
 */
public class ShaderNodesPropertyBuilder extends EditableObjectPropertyBuilder<ChangeConsumer> {

    /**
     * The single instance.
     */
    @NotNull
    private static final PropertyBuilder INSTANCE = new ShaderNodesPropertyBuilder();

    /**
     * Get the property builder instance.
     *
     * @return the property builder instance.
     */
    @FromAnyThread
    public static @NotNull PropertyBuilder getInstance() {
        return INSTANCE;
    }

    protected ShaderNodesPropertyBuilder() {
        super(ChangeConsumer.class);
    }


    @Override
    protected @Nullable List<EditableProperty<?, ?>> getProperties(@NotNull final Object object) {

        if (!(object instanceof ShaderNodeDefinition || object instanceof ShaderNodeVariable)) {
            return null;
        }

        final List<EditableProperty<?, ?>> result = new ArrayList<>();


        if (object instanceof ShaderNodeDefinition) {

            final ShaderNodeDefinition definition = (ShaderNodeDefinition) object;
            result.add(new SimpleProperty<Object, ShaderNodeDefinition>(READ_ONLY_STRING, "Type", definition,
                    def -> def.getType().name()));

        } else if (object instanceof ShaderNodeVariable) {

            final ShaderNodeVariable variable = (ShaderNodeVariable) object;

            result.add(new SimpleProperty<Object, ShaderNodeVariable>(READ_ONLY_STRING, "Type", variable,
                    var -> GLSLType.ofRawType(var.getType()).getUiName()));
        }

        return result;
    }
}
