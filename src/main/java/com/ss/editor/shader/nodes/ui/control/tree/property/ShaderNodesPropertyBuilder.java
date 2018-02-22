package com.ss.editor.shader.nodes.ui.control.tree.property;

import static com.ss.editor.extension.property.EditablePropertyType.*;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.extension.property.EditableProperty;
import com.ss.editor.extension.property.SimpleProperty;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.ui.control.property.builder.PropertyBuilder;
import com.ss.editor.ui.control.property.builder.impl.EditableObjectPropertyBuilder;
import com.ss.editor.util.GlslType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The builder to build properties for shader nodes things.
 *
 * @author JavaSaBr
 */
public class ShaderNodesPropertyBuilder extends EditableObjectPropertyBuilder<ChangeConsumer> {

    @NotNull
    private static final EnumSet<GlslType> NOT_AVAILABLE_DEFAULT_VALUE = EnumSet.of(
            GlslType.SAMPLER_2D,
            GlslType.SAMPLER_CUBE
    );

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
    @FxThread
    protected @Nullable List<EditableProperty<?, ?>> getProperties(@NotNull final Object object) {

        if (!(object instanceof ShaderNodeDefinition || object instanceof ShaderNodeVariable)) {
            return null;
        }

        final List<EditableProperty<?, ?>> result = new ArrayList<>();


        if (object instanceof ShaderNodeDefinition) {

            final ShaderNodeDefinition definition = (ShaderNodeDefinition) object;
            result.add(new SimpleProperty<Object, ShaderNodeDefinition>(READ_ONLY_STRING, Messages.MODEL_PROPERTY_TYPE, definition,
                    def -> def.getType().name()));

        } else if (object instanceof ShaderNodeVariable) {

            final ShaderNodeVariable variable = (ShaderNodeVariable) object;
            final GlslType glslType = GlslType.ofRawType(variable.getType());

            result.add(new SimpleProperty<>(ENUM, Messages.MODEL_PROPERTY_TYPE, variable,
                    var -> GlslType.ofRawType(var.getType()),
                    (var, type) -> var.setType(type.getRawType())));

            if (!NOT_AVAILABLE_DEFAULT_VALUE.contains(glslType)) {
                result.add(new SimpleProperty<>(STRING, Messages.MODEL_PROPERTY_DEFAULT_VALUE, variable,
                        ShaderNodeVariable::getDefaultValue,
                        ShaderNodeVariable::setDefaultValue));
            }
        }

        return result;
    }
}
