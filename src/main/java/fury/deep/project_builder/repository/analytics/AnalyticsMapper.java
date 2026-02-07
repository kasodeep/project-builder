package fury.deep.project_builder.repository.analytics;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AnalyticsMapper {

    /* ---------- ENSURE ROWS ---------- */
    void ensureProject(String projectId);

    void ensureFeature(String featureId);

    void ensureUserRisk(String userId);

    /* ---------- PROJECT ---------- */
    void incTotalTasks(String projectId);

    void incCompletedTasks(String projectId);

    void decCompletedTasks(String projectId);

    void updateBlockedTasks(String projectId, int blocked);

    void incStatus(String projectId, String status);

    void decStatus(String projectId, String status);

    /* ---------- FEATURE ---------- */
    void incFeatureTotal(String featureId);

    void updateFeatureBlocked(String featureId, int blocked);

    /* ---------- USER RISK ---------- */
    void updateUserRisk(String userId, int blocking, int blockedUsers);

    /* ---------- LOOKUPS ---------- */
    String findFeatureIdByTask(String taskId);

    List<String> findUsersAssignedToTask(String taskId);

    List<String> findUsersInProject(String projectId);

    int countBlockedTasks(String projectId);

    int countBlockedTasksByFeature(String featureId);

    int countBlockingTasksForUser(String userId);

    int countBlockedUsersForUser(String userId);
}

