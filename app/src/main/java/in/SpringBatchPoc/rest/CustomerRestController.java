package in.SpringBatchPoc.rest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch")  // Base path for the controller
public class CustomerRestController {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@PostMapping("/importJob")  // Specific endpoint for starting the job
	public ResponseEntity<String> loadDataToDB() {
		try {
			JobParameters jobParams = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis())
					.toJobParameters();

			JobExecution jobExecution = jobLauncher.run(job, jobParams);
			return ResponseEntity.ok("Job executed successfully with ID: " + jobExecution.getId());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Job execution failed: " + e.getMessage());
		}
	}
}
