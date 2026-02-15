package fury.deep.project_builder.repository.task;

import fury.deep.project_builder.entity.task.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    void insertTask(@Param("task") Task task);

    void updateTask(@Param("task") Task task);

    void deleteTask(@Param("taskId") String taskId);

    int countTasksInProject(List<String> tasks, String projectId);

    /**
     * Fetch all tasks assigned to a user.
     * Includes:
     * - task metadata
     * - feature
     * - assignees
     * Excludes:
     * - dependencies
     */
    List<Task> findTasksByUserId(@Param("userId") String userId);

    /**
     * Fetch a single task by id.
     * Includes:
     * - metadata
     * - feature
     * - assignees
     * - dependencies
     */
    Task findTaskById(@Param("taskId") String taskId);

    /**
     * Fetch all tasks of a project (lightweight).
     * Includes:
     * - id
     * - name
     * - feature
     * - dependencies
     * Excludes:
     * - assignees
     * - metadata
     */
    List<Task> findTasksByProjectId(@Param("projectId") String projectId);
}
