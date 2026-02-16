package fury.deep.project_builder.repository.task;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskUtilMapper {

    void deleteAssigneesByTaskId(@Param("taskId") String taskId);

    void insertAssignees(
            @Param("taskId") String taskId,
            @Param("assignees") List<String> assignees,
            @Param("teamId") String teamId
    );

    default void replaceAssignees(String taskId, List<String> assignees, String teamId) {
        deleteAssigneesByTaskId(taskId);
        insertAssignees(taskId, assignees, teamId);
    }

    void deleteDependenciesByTaskId(@Param("taskId") String taskId);

    void insertDependencies(
            @Param("taskId") String taskId,
            @Param("dependencies") List<String> dependencies,
            @Param("projectId") String projectId
    );

    default void replaceDependencies(String taskId, List<String> dependencies, String projectId) {
        deleteDependenciesByTaskId(taskId);
        insertDependencies(taskId, dependencies, projectId);
    }

    boolean createsCycle(
            @Param("taskId") String taskId,
            @Param("dependsOnTaskId") String dependsOnTaskId
    );
}
