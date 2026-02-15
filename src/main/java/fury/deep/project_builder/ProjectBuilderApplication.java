package fury.deep.project_builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProjectBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectBuilderApplication.class, args);
	}

}
