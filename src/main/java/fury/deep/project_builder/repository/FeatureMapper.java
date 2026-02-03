package fury.deep.project_builder.repository;

import fury.deep.project_builder.entity.task.Feature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeatureMapper {

    List<Feature> findAll();

    boolean existsById(@Param("id") String id);

    Feature findById(@Param("id") String id);
}
