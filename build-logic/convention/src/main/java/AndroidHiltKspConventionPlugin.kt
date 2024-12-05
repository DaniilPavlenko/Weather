import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import ru.dpav.build_logic.extension.libs

class AndroidHiltKspConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.google.devtools.ksp")
        }
        dependencies {
            add("ksp", libs.findLibrary("hilt-compiler").get())
        }
    }
}
