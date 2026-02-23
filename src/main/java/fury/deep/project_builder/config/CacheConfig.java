package fury.deep.project_builder.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String PROJECTS_BY_TEAM   = "projects-by-team";
    public static final String TASK_BY_ID         = "task-by-id";
    public static final String TASKS_BY_PROJECT   = "tasks-by-project";
    public static final String TASKS_FOR_USER     = "tasks-for-user";
}
