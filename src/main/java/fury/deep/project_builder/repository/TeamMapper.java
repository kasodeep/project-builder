package fury.deep.project_builder.repository;

import fury.deep.project_builder.entity.team.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeamMapper {

    List<Team> findAll();

    boolean existsById(@Param("id") String id);
}
