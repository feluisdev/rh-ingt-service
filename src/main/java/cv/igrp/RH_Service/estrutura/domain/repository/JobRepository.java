package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.filter.JobFilter;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;

import java.util.List;
import java.util.Optional;

public interface JobRepository {
    Job save(Job job);
    Optional<Job> findById(JobId id);
    Optional<Job> findByCode(String code);
    List<Job> findAll(JobFilter filter);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, JobId id);
}
