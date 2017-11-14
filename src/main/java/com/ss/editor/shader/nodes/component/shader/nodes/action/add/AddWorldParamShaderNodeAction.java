package com.ss.editor.shader.nodes.component.shader.nodes.action.add;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.add.AddWorldParameterOperation;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to add a new world parameter
 *
 * @author JavaSaBr
 */
public class AddWorldParamShaderNodeAction extends AddTechniqueDefParameterShaderNodeAction {

    @NotNull
    private static final Array<String> WORLD_VARIABLES = ArrayFactory.newArray(String.class);

    static {
        for (final UniformBinding binding : UniformBinding.values()) {
            WORLD_VARIABLES.add(binding.name());
        }
    }

    public AddWorldParamShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                         @NotNull final TechniqueDef techniqueDef, @NotNull final Vector2f location) {
        super(container, techniqueDef, location);

        final List<UniformBinding> worldBindings = techniqueDef.getWorldBindings();

        WORLD_VARIABLES.stream()
                .filter(name -> !worldBindings.contains(UniformBinding.valueOf(name)))
                .forEach(getAvailable()::add);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return PluginMessages.WORLD_PARAMETER;
    }

    @Override
    @FXThread
    protected @NotNull String getDialogTitle() {
        return PluginMessages.ACTION_ADD_WORLD_PARAMETER_TITLE;
    }

    @Override
    @FXThread
    protected void addParameter(@NotNull final VarTable vars) {

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        final TechniqueDef techniqueDef = getObject();
        final String name = vars.getString(PROP_NAME);

        changeConsumer.execute(new AddWorldParameterOperation(techniqueDef, UniformBinding.valueOf(name), getLocation()));
    }
}
