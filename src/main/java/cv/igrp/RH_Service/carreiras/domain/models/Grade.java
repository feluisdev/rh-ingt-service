package cv.igrp.RH_Service.carreiras.domain.models;

import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Grade {

    private GradeId id;
    private CategoryId categoryId;
    private Integer gradeNumber;
    private String name;
    private BigDecimal salaryIndex;
    private BigDecimal salaryBase;
    private Boolean isActive;

    private Grade() {}

    public static Grade criar(CategoryId categoryId, Integer gradeNumber, String name,
                              BigDecimal salaryIndex, BigDecimal salaryBase) {
        if (gradeNumber == null || gradeNumber < 1) {
            throw new IllegalArgumentException("gradeNumber deve ser >= 1");
        }
        Grade grade = new Grade();
        grade.id = GradeId.gerarNovo();
        grade.categoryId = categoryId;
        grade.gradeNumber = gradeNumber;
        grade.name = name;
        grade.salaryIndex = salaryIndex;
        grade.salaryBase = salaryBase;
        grade.isActive = true;
        return grade;
    }

    public static Grade reconstituir(GradeId id, CategoryId categoryId, Integer gradeNumber,
                                     String name, BigDecimal salaryIndex, BigDecimal salaryBase,
                                     Boolean isActive) {
        Grade grade = new Grade();
        grade.id = id;
        grade.categoryId = categoryId;
        grade.gradeNumber = gradeNumber;
        grade.name = name;
        grade.salaryIndex = salaryIndex;
        grade.salaryBase = salaryBase;
        grade.isActive = isActive;
        return grade;
    }

    public void atualizar(String name, BigDecimal salaryIndex, BigDecimal salaryBase) {
        this.name = name;
        this.salaryIndex = salaryIndex;
        this.salaryBase = salaryBase;
    }

    public void desativar() {
        if (Boolean.FALSE.equals(this.isActive)) {
            throw new IllegalStateException("Escalão já está inativo");
        }
        this.isActive = false;
    }

    public void reativar() {
        if (Boolean.TRUE.equals(this.isActive)) {
            throw new IllegalStateException("Escalão já está ativo");
        }
        this.isActive = true;
    }
}
