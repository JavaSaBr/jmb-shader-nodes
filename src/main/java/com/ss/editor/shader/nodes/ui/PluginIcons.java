package com.ss.editor.shader.nodes.ui;

import com.ss.editor.manager.FileIconManager;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

/**
 * The collection of plugin icons.
 *
 * @author JavaSaBr
 */
public interface PluginIcons {

    @NotNull FileIconManager ICON_MANAGER = FileIconManager.getInstance();

    @NotNull Image ARROW_LEFT_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/left-arrow.svg");
    @NotNull Image ARROW_RIGHT_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/arrow-pointing-to-right.svg");
    @NotNull Image LIST_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/list.svg");
    @NotNull Image CODE_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/code.svg");
    @NotNull Image FRAGMENT_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/grid-pixel.svg");
    @NotNull Image VARIABLE_16 = ICON_MANAGER.getImage("/com/ss/editor/shader/nodes/icons/square-root-of-x-math-formula.svg");
}
