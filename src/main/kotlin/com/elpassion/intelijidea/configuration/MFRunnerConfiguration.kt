package com.elpassion.intelijidea.configuration

import com.elpassion.intelijidea.common.MFCommandLineState
import com.elpassion.intelijidea.common.MFDownloader
import com.elpassion.intelijidea.util.mfFilename
import com.elpassion.intelijidea.util.mfScriptDownloadUrl
import com.elpassion.intelijidea.util.showError
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import java.io.File
import javax.swing.event.HyperlinkEvent

class MFRunnerConfiguration(project: Project, configurationFactory: ConfigurationFactory, name: String)
    : LocatableConfigurationBase(project, configurationFactory, name) {

    var buildCommand: String? = null
    var taskName: String? = null
    var mainframerPath: String? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return MFSettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return MFCommandLineState(environment, mainframerPath!!, buildCommand!!, taskName!!)
    }

    override fun checkConfiguration() {
        when {
            buildCommand.isNullOrBlank() -> throw RuntimeConfigurationError("Build command cannot be empty")
            taskName.isNullOrBlank() -> throw RuntimeConfigurationError("Taskname cannot be empty")
            !isMfFileAvailable() -> {
                showScriptNotFoundError()
                throw RuntimeConfigurationError("Mainframer script cannot be found")
            }
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        taskName = element.getAttributeValue(CONFIGURATION_ATTR_TASK_NAME)
        mainframerPath = element.getAttributeValue(CONFIGURATION_ATTR_MAINFRAMER_PATH)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        taskName?.let { element.setAttribute(CONFIGURATION_ATTR_TASK_NAME, it) }
        mainframerPath?.let { element.setAttribute(CONFIGURATION_ATTR_MAINFRAMER_PATH, it) }
    }

    override fun isCompileBeforeLaunchAddedByDefault(): Boolean = false

    private fun isMfFileAvailable() = mainframerPath?.let { File(it, mfFilename).exists() } ?: false

    private fun showScriptNotFoundError() {
        showError(project, errorMessage) {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                MFDownloader.downloadFileToProject(it.url.toString(), project, mfFilename)
            }
        }
    }

    private val errorMessage: String
        get() = "Cannot find <b>$mfFilename</b> in the following path:\n\"$mainframerPath\"\n\n" +
                "<a href=\"$mfScriptDownloadUrl\">Download latest script file</a>"

    companion object {
        private val CONFIGURATION_ATTR_TASK_NAME = "MFRunner.taskName"
        private val CONFIGURATION_ATTR_MAINFRAMER_PATH = "MFRunner.mainframerPath"
    }
}