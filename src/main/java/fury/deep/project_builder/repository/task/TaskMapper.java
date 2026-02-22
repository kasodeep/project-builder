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

    Task findTaskById(@Param("taskId") String taskId);

    List<String> findAssigneesByTaskId(@Param("taskId") String taskId);

    List<String> findDependenciesByTaskId(@Param("taskId") String taskId);

    List<Task> findTasksByUserId(@Param("userId") String userId);

    List<Task> findTasksByProjectId(@Param("projectId") String projectId);

    List<Task> findTasksDependingOn(@Param("taskId") String taskId);

    int countTasksInProject(@Param("tasks") List<String> tasks, @Param("projectId") String projectId);

    int countCompletedTasks(@Param("taskIds") List<String> taskIds);
}
