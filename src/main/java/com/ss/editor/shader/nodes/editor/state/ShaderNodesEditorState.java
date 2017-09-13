package com.ss.editor.shader.nodes.editor.state;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesFileEditor;
import com.ss.editor.ui.component.editor.state.impl.EditorMaterialEditorState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The implementation of a state container for the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditorState extends EditorMaterialEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 5;

    @NotNull
    private List<TechniqueDefState> techniqueDefStates;

    public ShaderNodesEditorState() {
        this.techniqueDefStates = new ArrayList<>();
    }

    @Override
    @FXThread
    public void setChangeHandler(@NotNull final Runnable changeHandler) {
        super.setChangeHandler(changeHandler);
        techniqueDefStates.forEach(techniqueDefState -> techniqueDefState.setChangeHandler(changeHandler));
    }

    /**
     * Get the state of the technique definition.
     *
     * @param techniqueDefName the name of a technique definition.
     * @return the state.
     */
    @FXThread
    public @NotNull TechniqueDefState getState(@NotNull final String techniqueDefName) {

        final Optional<TechniqueDefState> result = techniqueDefStates.stream()
                .filter(state -> state.getName().equals(techniqueDefName))
                .findAny();

        if(result.isPresent()) {
            return result.get();
        }

        final TechniqueDefState newState = new TechniqueDefState(techniqueDefName);
        newState.setChangeHandler(notNull(getChangeHandler()));

        techniqueDefStates.add(newState);
        notifyChange();

        return newState;
    }

    /**
     * Remove all not exists states.
     *
     * @param materialDef the material definition.
     */
    @FXThread
    public void cleanUp(@NotNull final MaterialDef materialDef) {

        final Collection<String> defsNames = materialDef.getTechniqueDefsNames();

        for (Iterator<TechniqueDefState> iterator = techniqueDefStates.iterator(); iterator.hasNext(); ) {

            final TechniqueDefState state = iterator.next();

            if (!defsNames.contains(state.getName())) {
                iterator.remove();
                notifyChange();
                continue;
            }

            final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(state.getName());
            final TechniqueDef techniqueDef = techniqueDefs.get(0);

            state.cleanUp(materialDef, techniqueDef);
        }
    }
}
