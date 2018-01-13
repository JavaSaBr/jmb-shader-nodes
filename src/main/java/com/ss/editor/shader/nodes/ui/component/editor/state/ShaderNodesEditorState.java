package com.ss.editor.shader.nodes.ui.component.editor.state;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesFileEditor;
import com.ss.editor.ui.component.editor.state.impl.EditorMaterialEditorState;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The implementation of a state container for the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditorState extends EditorMaterialEditorState {

    @NotNull
    private static final Logger LOGGER = LoggerManager.getLogger(ShaderNodesEditorState.class);

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 7;

    /**
     * The state of technique definitions.
     */
    @NotNull
    private List<TechniqueDefState> techniqueDefStates;

    public ShaderNodesEditorState() {
        this.techniqueDefStates = new ArrayList<>();
    }

    /**
     * Get the states of technique definitions.
     *
     * @return the states of technique definitions.
     */
    @FxThread
    public @NotNull List<TechniqueDefState> getTechniqueDefStates() {
        return techniqueDefStates;
    }

    @Override
    @FxThread
    public void setChangeHandler(@NotNull final Runnable changeHandler) {
        super.setChangeHandler(changeHandler);
        techniqueDefStates.forEach(state -> state.setChangeHandler(changeHandler));
    }

    /**
     * Get the state of the technique definition.
     *
     * @param techniqueDefName the name of a technique definition.
     * @return the state.
     */
    @FxThread
    public @NotNull TechniqueDefState getState(@NotNull final String techniqueDefName) {

        final Optional<TechniqueDefState> result = techniqueDefStates.stream()
                .filter(state -> state.getName().equals(techniqueDefName))
                .findAny();

        if (result.isPresent()) {
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
    @FxThread
    public void cleanUp(@NotNull final MaterialDef materialDef) {
        LOGGER.debug(this, editorState -> "The state before cleanup: " + editorState);

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

        LOGGER.debug(this, editorState -> "The state after cleanup: " + editorState);
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("ShaderNodesEditorState:\n");

        for (final TechniqueDefState defState : techniqueDefStates) {
            builder.append("\t").append(defState).append('\n');
        }

        return builder.toString();
    }
}
