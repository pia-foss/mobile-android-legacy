import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

// The task that this plugin provides can be executed by running ./gradlew :ExpressVPNMobile:licenses
// It resolves all of the dependencies used in the release configurations of the app and outputs
// a json file containing the name, version, url and license that each dependency is using.
class LicensePlugin : Plugin<Project> {
    private val configurationNames = listOf(
        "productionPlaystoreReleaseRuntimeClasspath",
        "productionAmazonstoreReleaseRuntimeClasspath",
    )

    private data class LicenseInfo(
        val moduleName: String?,
        val moduleVersion: String?,
        val moduleUrl: String?,
        val moduleLicense: String?,
        val moduleLicenseUrl: String?
    )

    override fun apply(target: Project) {
        target.tasks.register("licenses") {
            doLast {
                val gson: Gson = GsonBuilder().serializeNulls().create()
                val licenses = target.findLicenses()
                File(target.buildDir, "reports").mkdirs()
                File(target.buildDir, "reports/licenses.json").writeText(gson.toJson(licenses))
            }
        }
    }

    private fun Project.findLicenses(): List<LicenseInfo> {
        val dependencyIds = configurations
            .filter { it.isCanBeResolved }
            .filter { configurationNames.any { name -> it.name == name } }
            .flatMap { it.incoming.resolutionResult.allDependencies }
            .filterIsInstance<ResolvedDependencyResult>()
            .map { it.selected.id }
        return getPoms(dependencyIds)
            .flatMap { readLicenseFromPom(it) }
            .sortedBy { it.moduleName }
    }

    private fun Project.getPoms(ids: List<ComponentIdentifier>) =
        this.dependencies
            .createArtifactResolutionQuery()
            .forComponents(ids)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()
            .resolvedComponents
            .flatMap {
                it.getArtifacts(MavenPomArtifact::class.java)
                    .filterIsInstance<ResolvedArtifactResult>()
            }

    private fun readLicenseFromPom(artifact: ResolvedArtifactResult): List<LicenseInfo> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(artifact.file.absolutePath)

        val projectNode = document.getElementsByTagName("project").asSequence().first()

        val componentIdentifier =
            artifact.id.componentIdentifier as DefaultModuleComponentIdentifier
        val moduleName = componentIdentifier.group + ":" + componentIdentifier.module
        val moduleVersion = componentIdentifier.version

        val moduleUrl = projectNode.findFirstChildNode("url")?.textContent

        val licenses = document.getElementsByTagName("license").asSequence().map {
            val licenseUrl = it.findFirstChildNode("url")?.textContent
            val licenseName = it.findFirstChildNode("name")?.textContent
            LicenseInfo(
                moduleLicenseUrl = licenseUrl,
                moduleLicense = licenseName,
                moduleName = moduleName,
                moduleVersion = moduleVersion,
                moduleUrl = moduleUrl
            )
        }.toList()

        return if (licenses.isEmpty()) {
            listOf(
                LicenseInfo(
                    moduleName = moduleName,
                    moduleVersion = moduleVersion,
                    moduleUrl = moduleUrl,
                    moduleLicense = null,
                    moduleLicenseUrl = null
                )
            )
        } else {
            licenses
        }
    }

    private fun NodeList.asSequence() = NodeListSequence(this)

    private class NodeListSequence(private val nodes: NodeList) : Sequence<Node> {
        override fun iterator() = NodeListIterator(nodes)
    }

    private class NodeListIterator(private val nodes: NodeList) : Iterator<Node> {
        private var i = 0
        override fun hasNext() = nodes.length > i
        override fun next(): Node = nodes.item(i++)
    }

    private fun Node.findFirstChildNode(name: String): Node? {
        return childNodes.asSequence().firstOrNull { it.nodeName == name }
    }
}


