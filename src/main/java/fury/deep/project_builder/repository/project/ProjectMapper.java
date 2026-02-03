package fury.deep.project_builder.repository.project;

import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {

    void insertProject(Project project);

    // Since, no explicit @Param, any name can be used in the query with {}.
    Project findById(String projectId);

    List<Project> findAll(String teamId);

    void updateProjectMetadata(ProjectUpdateRequest request, String updatedBy);

    boolean isOwnerOrManager(@Param("projectId") String projectId, @Param("username") String username);
}
