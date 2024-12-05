import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ru.dpav.build_logic.extension.addImplementation
import ru.dpav.build_logic.extension.libs

class AndroidHiltLibConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.google.dagger.hilt.android")
        }
        dependencies {
            addImplementation("hilt-android", libs)
        }
    }
}
