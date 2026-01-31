package fury.deep.project_builder.repository;

import fury.deep.project_builder.dto.project.ProjectUpdateRequest;
import fury.deep.project_builder.entity.project.Project;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {

    void insertProject(Project project);

    Project findById(String projectId);

    List<Project> findAll(String teamId);

    void updateProjectMetadata(ProjectUpdateRequest request, String updatedBy);
}
