package fury.deep.project_builder.repository.project;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectAuthMapper {

    void deleteManagersByProjectId(String projectId);

    void insertManagers(@Param("projectId") String projectId,
                        @Param("usernames") List<String> usernames,
                        @Param("teamId") String teamId);

    default void replaceManagers(String projectId, List<String> usernames, String teamId) {
        deleteManagersByProjectId(projectId);
        insertManagers(projectId, usernames, teamId);
    }
}