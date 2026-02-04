package fury.deep.project_builder.controller.task;

import fury.deep.project_builder.dto.task.util.AddAssigneeRequest;
import fury.deep.project_builder.dto.task.util.AddDependenciesRequest;
import fury.deep.project_builder.security.AuthContextHolder;
import fury.deep.project_builder.service.task.TaskUtilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/task-util")
public class TaskUtilController {

    private final TaskUtilService taskUtilService;

    public TaskUtilController(TaskUtilService taskUtilService) {
        this.taskUtilService = taskUtilService;
    }

    @PutMapping("/add-assignees")
    public ResponseEntity<Void> addAssignees(@Valid @RequestBody AddAssigneeRequest addAssigneeRequest) {
        taskUtilService.addAssignees(addAssigneeRequest, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/add-dependencies")
    public ResponseEntity<Void> addDependencies(@Valid @RequestBody AddDependenciesRequest addDependenciesRequest) {
        taskUtilService.addDependencies(addDependenciesRequest, AuthContextHolder.getUser());
        return ResponseEntity.ok().build();
    }
}
