package in.SpringBatchPoc.config;

import in.SpringBatchPoc.entity.Customer;
import in.SpringBatchPoc.repo.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;



@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

  private final CustomerRepository customerRepository;

  @Bean
  public FlatFileItemReader<Customer> customerReader() {
    FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
    itemReader.setResource(new FileSystemResource(
        "src/main/resources/customers.csv"));  // Consider externalizing this path
    itemReader.setName("csv-reader");
    itemReader.setLinesToSkip(1);
    itemReader.setLineMapper(lineMapper());
    return itemReader;
  }

  private LineMapper<Customer> lineMapper() {
    DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setStrict(false);
    lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country",
        "dob");

    BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(Customer.class);

    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSetMapper);
    return lineMapper;
  }

  @Bean
  public CustomerProcessor customerProcessor() {
    return new CustomerProcessor();  // Ensure this class is implemented with your processing logic
  }

  @Bean
  public RepositoryItemWriter<Customer> customerWriter() {
    RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
    writer.setRepository(customerRepository);
    writer.setMethodName("save");
    return writer;
  }

  @Bean
  public Step customerProcessingStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new StepBuilder("customerProcessingStep", jobRepository).<Customer, Customer>chunk(100,
            transactionManager).reader(customerReader()).processor(customerProcessor())
        .writer(customerWriter()).taskExecutor(taskExecutor()).build();
  }

  @Bean(name = "importJob")
  public Job customerImportJob(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new JobBuilder("customerImportJob", jobRepository).incrementer(new RunIdIncrementer())
        .start(customerProcessingStep(jobRepository, transactionManager)).build();
  }

  @Bean
  public TaskExecutor taskExecutor() {
    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    taskExecutor.setConcurrencyLimit(10);
    return taskExecutor;
  }
}
