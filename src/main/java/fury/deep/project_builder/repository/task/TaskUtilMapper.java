package fury.deep.project_builder.repository.task;

import fury.deep.project_builder.entity.task.TaskDependency;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskUtilMapper {

    void deleteAssigneesByTaskId(String taskId);

    void insertAssignees(String taskId, List<String> assignees, String teamId);

    default void replaceAssignees(String taskId, List<String> assignees, String teamId) {
        deleteAssigneesByTaskId(taskId);
        insertAssignees(taskId, assignees, teamId);
    }

    void deleteDependenciesByTaskId(String taskId);

    void insertDependencies(String taskId, List<String> dependencies, String projectId);

    default void replaceDependencies(String taskId, List<String> dependencies, String projectId) {
        deleteDependenciesByTaskId(taskId);
        insertDependencies(taskId, dependencies, projectId);
    }

    List<TaskDependency> findAllDependenciesByProjectId(String projectId);
}
