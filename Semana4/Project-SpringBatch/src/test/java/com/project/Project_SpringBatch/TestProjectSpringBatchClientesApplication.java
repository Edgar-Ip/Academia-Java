package com.project.Project_SpringBatch;

import org.springframework.boot.SpringApplication;

public class TestProjectSpringBatchClientesApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProjectSpringBatchClientesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
