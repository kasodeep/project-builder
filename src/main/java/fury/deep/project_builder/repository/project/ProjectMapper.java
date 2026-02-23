package fury.deep.project_builder.repository.project;

import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {

    void insertProject(@Param("project") Project project,
                       @Param("teamId") String teamId);

    /**
     * Updates project fields that are non-null in the request.
     * Uses optimistic locking via the {@code version} column.
     *
     * @return number of rows updated — 0 means version mismatch (concurrent edit)
     */
    int updateProjectMetadata(@Param("request") ProjectUpdateRequest request,
                              @Param("updatedBy") String updatedBy);

    List<Project> findAll(@Param("teamId") String teamId);

    boolean isOwnerOrManager(@Param("projectId") String projectId,
                             @Param("username") String username);

    void recomputeProjectProgress(String projectId);
}