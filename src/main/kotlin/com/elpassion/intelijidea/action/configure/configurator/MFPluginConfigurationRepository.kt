package com.elpassion.intelijidea.action.configure.configurator

import com.elpassion.intelijidea.common.MFToolConfiguration
import com.elpassion.intelijidea.task.MFBeforeTaskDefaultSettingsProvider
import com.elpassion.intelijidea.task.MFTaskData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

class MFPluginConfigurationRepository(private val project: Project) {

    fun getConfiguration(): MFPluginConfiguration = with(taskData) {
        MFPluginConfiguration(taskName = taskName, buildCommand = buildCommand, remoteName = getRemoteMachineName(), mainframerPath = mainframerPath)
    }

    private fun getRemoteMachineName(): String? {
        return ApplicationManager.getApplication().runReadAction<String> {
            MFToolConfiguration(project.basePath).readRemoteMachineName()
        }
    }

    fun saveConfiguration(configuration: MFPluginConfiguration) {
        saveProviderConfiguration(data = configuration)
        setRemoteMachineName(configuration.remoteName)
    }

    private fun setRemoteMachineName(name: String?) = name?.let {
        ApplicationManager.getApplication().runWriteAction {
            MFToolConfiguration(project.basePath).writeRemoteMachineName(name)
        }
    }

    private fun saveProviderConfiguration(data: MFPluginConfiguration) {
        taskData = taskData.copy(
                buildCommand = data.buildCommand,
                taskName = data.taskName,
                mainframerPath = data.mainframerPath)
    }

    private var taskData: MFTaskData
        get() = MFBeforeTaskDefaultSettingsProvider.INSTANCE.taskData
        set(value) {
            MFBeforeTaskDefaultSettingsProvider.INSTANCE.taskData = value
        }
}