package fury.deep.project_builder.repository;

import fury.deep.project_builder.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    void registerUser(@Param("user") User user, String teamId);

    User findByUsername(String username);

    int countUsersInTeam(@Param("userIds") List<String> userIds, @Param("teamId") String teamId);

    List<User> findByTeamId(String teamId);
}
