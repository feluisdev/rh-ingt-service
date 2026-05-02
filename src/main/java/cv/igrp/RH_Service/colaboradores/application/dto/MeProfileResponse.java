package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MeProfileResponse {
    private String id;
    private String fullName;
    private String nif;
    private String email;
    private String phone;
    private String workerState;
    private LocalDate admissionDate;
    private UnitRef currentUnit;
    private JobRef currentJob;
    private CareerRef career;
    private CategoryRef category;
    private GradeRef grade;

    @Getter @Setter @NoArgsConstructor
    public static class UnitRef {
        private String id;
        private String name;
        public UnitRef(String id, String name) { this.id = id; this.name = name; }
    }

    @Getter @Setter @NoArgsConstructor
    public static class JobRef {
        private String id;
        private String name;
        public JobRef(String id, String name) { this.id = id; this.name = name; }
    }

    @Getter @Setter @NoArgsConstructor
    public static class CareerRef {
        private String id;
        private String name;
        public CareerRef(String id, String name) { this.id = id; this.name = name; }
    }

    @Getter @Setter @NoArgsConstructor
    public static class CategoryRef {
        private String id;
        private String name;
        public CategoryRef(String id, String name) { this.id = id; this.name = name; }
    }

    @Getter @Setter @NoArgsConstructor
    public static class GradeRef {
        private String id;
        private Integer gradeNumber;
        public GradeRef(String id, Integer gradeNumber) { this.id = id; this.gradeNumber = gradeNumber; }
    }
}
