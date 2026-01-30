package fury.deep.project_builder.repository;

import fury.deep.project_builder.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void registerUser(@Param("user") User user, String teamId);

    User findByUsername(String username);
}
