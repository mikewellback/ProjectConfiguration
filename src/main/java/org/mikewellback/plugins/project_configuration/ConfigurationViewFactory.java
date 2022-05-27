package org.mikewellback.plugins.project_configuration;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectType;
import com.intellij.openapi.project.ProjectTypeService;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ConfigurationViewFactory implements ToolWindowFactory, DumbAware {
    public static final ProjectType FLUTTER_PROJECT_TYPE = new ProjectType("io.flutter");
    public static final ProjectType ANDROID_PROJECT_TYPE = new ProjectType("Android");

    public static final String ANDROID_STUDIO_IDE = "Android Studio";
    public static final String INTELLIJ_IDEA_IDE = "IntelliJ IDEA";

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        ConfigurationView view = new ConfigurationView(project.getBasePath(), toolWindow);
        Content content = contentFactory.createContent(view.initView(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void init(@NotNull ToolWindow window) {
        window.getContentManager();
    }

    public boolean shouldBeAvailable(@NotNull Project project) {
        return ANDROID_STUDIO_IDE.equals(ApplicationInfo.getInstance().getVersionName()) &&
        ANDROID_PROJECT_TYPE.equals(ProjectTypeService.getProjectType(project));
    }

    public boolean isDoNotActivateOnStart() {
        return false;
    }

}
