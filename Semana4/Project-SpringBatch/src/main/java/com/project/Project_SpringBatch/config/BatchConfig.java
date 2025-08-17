package com.project.Project_SpringBatch.config;


import com.project.Project_SpringBatch.domain.Customer;
import com.project.Project_SpringBatch.domain.CustomerDocument;
import com.project.Project_SpringBatch.processor.CustomerItemProcessor;
import com.project.Project_SpringBatch.reader.CustomerItemReader;
import com.project.Project_SpringBatch.writer.CustomerItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración principal del Job de Spring Batch para la migración de customers
 * Define el Job, Step, Reader, Processor y Writer para el proceso de migración.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    //Inyección de dependencias
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CustomerItemReader customerItemReader;
    private final CustomerItemProcessor customerItemProcessor;
    private final CustomerItemWriter customerItemWriter;

    /**
     * Tamaño del chunk (número de elementos procesados en cada transacción)
     */
    private static final int CHUNK_SIZE = 10;

    /**
     * Bean que define el Reader para leer customers desde MySQL
     * @return ItemReader configurado para leer desde MySQL
     */


    @Bean
    public FlatFileItemReader<Customer> reader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(new ClassPathResource("customers_seed.csv"))
                .delimited()
                .names("name, lastName, email, country, registered_at")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Customer.class);
                }})
                .build();
    }

    /**
     * Bean que define el Processor para transformar Customer a CustomerDocument
     * @return ItemProcessor configurado para la transformación
     */
    @Bean
    public ItemProcessor<Customer, CustomerDocument> processor(){
        log.info("Configurando ItemProcessor para transformación de datos");
        return customerItemProcessor;
    }

    /**
     * Bean que define el Writer para escribir en MongoDB
     * @return ItemWriter configurado para escribir en MongoDB
     */
    @Bean
    public ItemWriter<CustomerDocument> writer(){
        log.info("Configurando ItemWriter para MongoDB");
        return customerItemWriter;
    }


    /**
     * Bean que define el Step el principal del Job
     * Un Step es una fase independiente de un Job que encapsula y controla
     * todos los detalles del procesamiento
     * @return Step configurando reader, processor y writer.
     */
    @Bean
    public Step customerMigrationStep(){
        log.info("Configurando Step de migración de customers");
        return new StepBuilder("customerMigrationStep", jobRepository)
                .<Customer, CustomerDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader()) //Lee los customers desde MySQL
                .processor(processor())//Transforma Customer a CustomerDocument
                .writer(writer())//Escribe CustomerDocument en MongoDB
                .allowStartIfComplete(true) //Permite reiniciar el step si ya se completo
                .build();
    }

    @Bean
    public Job customerMigrationJob (){
        log.info("Configurando Job de migración de customers");
        return new JobBuilder("customerMigrationJob", jobRepository)
                .start(customerMigrationStep()) //Define el step inicial
                .build();
    }

}
