package com.tencent.bk.codecc.task.service.impl

import com.tencent.bk.codecc.task.dao.mongorepository.GongfengPublicProjRepository
import com.tencent.bk.codecc.task.dao.mongorepository.TaskRepository
import com.tencent.devops.common.auth.api.external.AuthTaskService
import com.tencent.devops.common.auth.api.pojo.external.KEY_CREATE_FROM
import com.tencent.devops.common.auth.api.pojo.external.KEY_PIPELINE_ID
import com.tencent.devops.common.auth.api.pojo.external.PREFIX_TASK_INFO
import com.tencent.devops.common.pojo.GongfengBaseInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class AuthTaskServiceImpl @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val gongfengPublicProjRepository: GongfengPublicProjRepository,
        private val redisTemplate: RedisTemplate<String, String>
) : AuthTaskService {

    override fun getGongfengProjInfo(taskId: Long): GongfengBaseInfo? {
        val taskInfoEntity = taskRepository.findByTaskId(taskId) ?: return null
        val gongfengPublicProjEntity = gongfengPublicProjRepository.findById(taskInfoEntity.gongfengProjectId)
        return GongfengBaseInfo(gongfengPublicProjEntity.id, gongfengPublicProjEntity.nameSpace.id, gongfengPublicProjEntity.name)
    }

    /**
     * 查询任务创建来源
     */
    override fun getTaskCreateFrom(
            taskId: Long
    ): String {
        var createFrom = redisTemplate.opsForHash<String, String>().get(PREFIX_TASK_INFO + taskId, KEY_CREATE_FROM)
        if (createFrom.isNullOrEmpty()) {
            val taskInfoEntity = taskRepository.findByTaskId(taskId)
            if (!taskInfoEntity.createFrom.isNullOrEmpty()) {
                createFrom = taskInfoEntity.createFrom
                redisTemplate.opsForHash<String, String>().put(PREFIX_TASK_INFO + taskId, KEY_CREATE_FROM, createFrom)
            }
        }
        return createFrom
    }

    /**
     * 获取任务所属流水线ID
     */
    override fun getTaskPipelineId(
            taskId: Long
    ): String {
        var pipelineId = redisTemplate.opsForHash<String, String>().get(PREFIX_TASK_INFO + taskId, KEY_PIPELINE_ID)
        if (pipelineId.isNullOrEmpty()) {
            val taskInfoEntity = taskRepository.findByTaskId(taskId)
            if (!taskInfoEntity.pipelineId.isNullOrEmpty()) {
                pipelineId = taskInfoEntity.pipelineId
                redisTemplate.opsForHash<String, String>().put(PREFIX_TASK_INFO + taskId, KEY_PIPELINE_ID, pipelineId)
            }
        }
        return pipelineId
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthTaskServiceImpl::class.java)

        @Value("\${common.codecc.env:#{null}}")
        val env: String? = null
    }

}