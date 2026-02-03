package fury.deep.project_builder.repository.project;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectAuthMapper {

    void deleteManagersByProjectId(String projectId);

    void insertManagers(String projectId, List<String> usernames, String teamId);

    default void replaceManagers(String projectId, List<String> usernames, String teamId) {
        deleteManagersByProjectId(projectId);
        insertManagers(projectId, usernames, teamId);
    }
}
